/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * RequestFilter for removing empty elements/attributes
 *
 * @author Ole.Matzura
 */

public class RemoveEmptyContentRequestFilter extends AbstractRequestFilter {
    @SuppressWarnings("unused")
    private final static Logger log = LogManager.getLogger(RemoveEmptyContentRequestFilter.class);

    public void filterAbstractHttpRequest(SubmitContext context, AbstractHttpRequest<?> wsdlRequest) {
        if (wsdlRequest != null && !wsdlRequest.isRemoveEmptyContent()) {
            return;
        }

        String content = (String) context.getProperty(BaseHttpRequestTransport.REQUEST_CONTENT);
        if (!StringUtils.hasContent(content)) {
            return;
        }

        String soapNamespace = null;
        String newContent = null;

        if (wsdlRequest instanceof WsdlRequest) {
            soapNamespace = ((WsdlRequest) wsdlRequest).getOperation().getInterface().getSoapVersion()
                    .getEnvelopeNamespace();
        }

        while (!content.equals(newContent)) {
            if (newContent != null) {
                content = newContent;
            }

            newContent = removeEmptyContent(content, soapNamespace, context.hasProperty("RemoveEmptyXsiNil"));
            if (!context.hasProperty("RemoveEmptyRecursive")) {
                break;
            }
        }

        if (newContent != null) {
            context.setProperty(BaseHttpRequestTransport.REQUEST_CONTENT, newContent);
        }
    }

    public static String removeEmptyContent(String content, String soapNamespace, boolean removeXsiNil) {
        XmlCursor cursor = null;

        try {
            // XmlObject xmlObject = XmlObject.Factory.parse( content );
            XmlObject xmlObject = XmlUtils.createXmlObject(content);
            cursor = xmlObject.newCursor();

            cursor.toNextToken();

            // skip root element
            cursor.toNextToken();
            boolean removed = false;

            while (!cursor.isEnddoc()) {
                boolean flag = false;
                if (cursor.isContainer()
                        && (soapNamespace == null || !soapNamespace.equals(cursor.getName().getNamespaceURI()))) {
                    Element elm = (Element) cursor.getDomNode();
                    NamedNodeMap attributes = elm.getAttributes();
                    if (attributes != null && attributes.getLength() > 0) {
                        for (int c = 0; c < attributes.getLength(); c++) {
                            Node node = attributes.item(c);
                            if (node.getNodeValue() == null || node.getNodeValue().trim().length() == 0) {
                                cursor.removeAttribute(XmlUtils.getQName(node));
                                removed = true;
                            }
                        }
                    }

                    if (removeXsiNil && attributes.getNamedItem("xsi:nil") != null) {
                        if (attributes.getLength() == 1
                                || (attributes.getLength() == 2 && attributes.getNamedItem("xmlns:xsi") != null)) {
                            attributes.removeNamedItem("xsi:nil");
                            attributes.removeNamedItem("xmlns:xsi");
                            removed = true;
                        }
                    }

                    if (attributes.getLength() == 0
                            && (cursor.getTextValue() == null || cursor.getTextValue().trim().length() == 0)
                            && XmlUtils.getFirstChildElement(elm) == null) {
                        if (cursor.removeXml()) {
                            removed = true;
                            flag = true;
                        }
                    }
                }

                if (!flag) {
                    cursor.toNextToken();
                }
            }

            if (removed) {
                return xmlObject.xmlText();
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        } finally {
            if (cursor != null) {
                cursor.dispose();
            }
        }

        return content;
    }
}
