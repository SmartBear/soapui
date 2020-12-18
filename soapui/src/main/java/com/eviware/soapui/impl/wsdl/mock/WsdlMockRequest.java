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

package com.eviware.soapui.impl.wsdl.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractMockRequest;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Vector;

/**
 * Request-class created when receiving an external request to a WsdlMockService
 *
 * @author ole.matzura
 */

public class WsdlMockRequest extends AbstractMockRequest {
    private SoapVersion soapVersion;
    private String soapAction;
    private Vector<Object> wssResult;

    public WsdlMockRequest(HttpServletRequest request, HttpServletResponse response, WsdlMockRunContext context)
            throws Exception {

        super(request, response, context);

    }


    public SoapVersion getSoapVersion() {
        return soapVersion;
    }

    public String getProtocol() {
        return super.getProtocol();
    }

    public Vector<?> getWssResult() {
        return wssResult;
    }


    public void setRequestContent(String requestContent) {
        super.setRequestContent(requestContent);
        setRequestXmlObject(null);

        try {
            soapVersion = SoapUtils.deduceSoapVersion(getRequest().getContentType(), getRequestXmlObject());
        } catch (XmlException e) {
            SoapUI.logError(e);
        }

        if (soapVersion == null) {
            soapVersion = SoapVersion.Soap11;
        }
    }


    public XmlObject getContentElement() throws XmlException {
        return SoapUtils.getContentElement(getRequestXmlObject(), soapVersion);
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    @Override
    protected void initProtocolSpecificPostContent(WsdlMockRunContext context, String contentType) throws IOException {
        if (!isMultiPart(contentType)) {
            addWSSResult(context, getRequestContent());
        }
        initSoapVersion(contentType);

        soapAction = SoapUtils.getSoapAction(soapVersion, getRequestHeaders());
    }

    private void addWSSResult(WsdlMockRunContext context, String requestContent) throws IOException {
        WsdlMockService mockService = (WsdlMockService) context.getMockService();
        if (StringUtils.hasContent(mockService.getIncomingWss())) {
            IncomingWss incoming = mockService.getProject().getWssContainer()
                    .getIncomingWssByName(mockService.getIncomingWss());
            if (incoming != null) {
                Document dom = XmlUtils.parseXml(requestContent);
                try {
                    wssResult = incoming.processIncoming(dom, context);
                    if (wssResult != null && wssResult.size() > 0) {
                        StringWriter writer = new StringWriter();
                        XmlUtils.serialize(dom, writer);
                        setActualRequestContent(requestContent);
                        setRequestContent(writer.toString());
                    }
                } catch (Exception e) {
                    if (wssResult == null) {
                        wssResult = new Vector<Object>();
                    }
                    wssResult.add(e);
                }
            }
        }
    }

    private void initSoapVersion(String contentType) {
        try {
            soapVersion = SoapUtils.deduceSoapVersion(contentType, getRequestXmlObject());
        } catch (Exception e) {
            // ignore non xml requests
        }

        if (soapVersion == null) {
            soapVersion = SoapVersion.Soap11;
        }
    }


}
