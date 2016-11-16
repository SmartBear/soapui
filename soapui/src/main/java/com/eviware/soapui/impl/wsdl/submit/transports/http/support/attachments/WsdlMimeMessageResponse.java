/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.filters.WssRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlHexBinary;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Vector;

public class WsdlMimeMessageResponse extends MimeMessageResponse implements WsdlResponse {
    private Vector<Object> wssResult;

    public WsdlMimeMessageResponse(WsdlRequest httpRequest, ExtendedHttpMethod httpMethod, String requestContent,
                                   PropertyExpansionContext context) {
        super(httpRequest, httpMethod, requestContent, context);

        WsdlRequest wsdlRequest = (WsdlRequest) httpRequest;
        processIncomingWss(wsdlRequest, context);

        String multipartType = null;

        Header h = null;
        if (httpMethod.hasHttpResponse() && httpMethod.getHttpResponse().getEntity() != null) {
            h = httpMethod.getHttpResponse().getEntity().getContentType();
        }

        if (h != null) {
            HeaderElement[] elements = h.getElements();

            for (HeaderElement element : elements) {
                String name = element.getName().toUpperCase();
                if (name.startsWith("MULTIPART/")) {
                    NameValuePair parameter = element.getParameterByName("type");
                    if (parameter != null) {
                        multipartType = parameter.getValue();
                    }
                }
            }
        }

        if (wsdlRequest.isExpandMtomResponseAttachments() && "application/xop+xml".equals(multipartType)) {
            expandMtomAttachments(wsdlRequest);
        }
    }

    private void processIncomingWss(AbstractHttpRequestInterface<?> wsdlRequest, PropertyExpansionContext context) {
        IncomingWss incomingWss = (IncomingWss) context.getProperty(WssRequestFilter.INCOMING_WSS_PROPERTY);
        if (incomingWss != null) {
            try {
                Document document = XmlUtils.parseXml(getMmSupport().getResponseContent());
                wssResult = incomingWss.processIncoming(document, context);
                if (wssResult != null && wssResult.size() > 0) {
                    StringWriter writer = new StringWriter();
                    XmlUtils.serializePretty(document, writer);
                    getMmSupport().setResponseContent(writer.toString());
                }
            } catch (Exception e) {
                if (wssResult == null) {
                    wssResult = new Vector<Object>();
                }
                wssResult.add(e);
            }
        }
    }

    private void expandMtomAttachments(WsdlRequest wsdlRequest) {
        try {
            // XmlObject xmlObject = XmlObject.Factory.parse( getContentAsString()
            // );
            XmlObject xmlObject = XmlUtils.createXmlObject(getContentAsString());
            XmlObject[] includes = xmlObject
                    .selectPath("declare namespace xop='http://www.w3.org/2004/08/xop/include'; //xop:Include");

            for (XmlObject include : includes) {
                Element elm = (Element) include.getDomNode();
                String href = elm.getAttribute("href");
                // substing(4) - removing the "cid:" prefix
                Attachment attachment = getMmSupport().getAttachmentWithContentId("<" + URLDecoder.decode(href.substring(4), "UTF-8") + ">");
                
                if (attachment != null) {
                    ByteArrayOutputStream data = Tools.readAll(attachment.getInputStream(), 0);
                    byte[] byteArray = data.toByteArray();

                    XmlCursor cursor = include.newCursor();
                    cursor.toParent();
                    XmlObject parentXmlObject = cursor.getObject();
                    cursor.dispose();

                    SchemaType schemaType = parentXmlObject.schemaType();
                    Node parentNode = elm.getParentNode();

                    if (schemaType.isNoType()) {
                        SchemaTypeSystem typeSystem = wsdlRequest.getOperation().getInterface().getWsdlContext()
                                .getSchemaTypeSystem();
                        SchemaGlobalElement schemaElement = typeSystem.findElement(new QName(parentNode.getNamespaceURI(),
                                parentNode.getLocalName()));
                        if (schemaElement != null) {
                            schemaType = schemaElement.getType();
                        }
                    }

                    String txt = null;

                    if (SchemaUtils.isInstanceOf(schemaType, XmlHexBinary.type)) {
                        txt = new String(Hex.encodeHex(byteArray));
                    } else {
                        txt = new String(Base64.encodeBase64(byteArray));
                    }

                    parentNode.replaceChild(elm.getOwnerDocument().createTextNode(txt), elm);
                }
            }

            getMmSupport().setResponseContent(xmlObject.toString());
        } catch (Exception e) {
            SoapUI.logError(e);
        }
    }

    @Override
    public WsdlRequest getRequest() {
        return (WsdlRequest) super.getRequest();
    }

    public Vector<?> getWssResult() {
        return wssResult;
    }

}
