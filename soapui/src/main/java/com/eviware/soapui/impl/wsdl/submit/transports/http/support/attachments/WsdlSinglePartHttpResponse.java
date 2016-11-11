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

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.filters.WssRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SinglePartHttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.xml.XmlUtils;
import org.w3c.dom.Document;

import java.io.StringWriter;
import java.util.Vector;

public class WsdlSinglePartHttpResponse extends SinglePartHttpResponse implements WsdlResponse {
    private Vector<Object> wssResult;

    public WsdlSinglePartHttpResponse(WsdlRequest wsdlRequest, ExtendedHttpMethod postMethod, String requestContent,
                                      PropertyExpansionContext context) {
        super(wsdlRequest, postMethod, requestContent, context);

        processIncomingWss(wsdlRequest, context);
    }

    private void processIncomingWss(WsdlRequest wsdlRequest, PropertyExpansionContext context) {
        IncomingWss incomingWss = (IncomingWss) context.getProperty(WssRequestFilter.INCOMING_WSS_PROPERTY);
        if (incomingWss != null) {
            try {
                Document document = XmlUtils.parseXml(getResponseContent());
                wssResult = incomingWss.processIncoming(document, context);
                if (wssResult != null && wssResult.size() > 0) {
                    StringWriter writer = new StringWriter();
                    XmlUtils.serialize(document, writer);
                    setResponseContent(writer.toString());
                }
            } catch (Exception e) {
                if (wssResult == null) {
                    wssResult = new Vector<Object>();
                }
                wssResult.add(e);
            }
        }
    }

    public Vector<?> getWssResult() {
        return wssResult;
    }

    @Override
    public WsdlRequest getRequest() {
        return (WsdlRequest) super.getRequest();
    }
}
