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

package com.eviware.soapui.impl.wsdl.submit.filters;

import com.eviware.soapui.config.EndpointConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.endpoint.DefaultEndpointStrategy;
import com.eviware.soapui.impl.wsdl.endpoint.DefaultEndpointStrategy.EndpointDefaults;
import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainer;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;
import org.w3c.dom.Document;

public class WssRequestFilter extends AbstractWssRequestFilter implements RequestFilter {
    public final static String INCOMING_WSS_PROPERTY = "WssRequestFilter#IncomingWss";

    public void filterWsdlRequest(SubmitContext context, WsdlRequest wsdlRequest) {
        WssContainer wssContainer = wsdlRequest.getOperation().getInterface().getProject().getWssContainer();
        if (wssContainer == null) {
            return;
        }

        OutgoingWss outgoingWss = wssContainer.getOutgoingWssByName(wsdlRequest.getOutgoingWss());

        DefaultEndpointStrategy des = (DefaultEndpointStrategy) wsdlRequest.getOperation().getInterface().getProject()
                .getEndpointStrategy();
        EndpointDefaults endpointDefaults = des.getEndpointDefaults(wsdlRequest.getEndpoint());
        if (StringUtils.hasContent(endpointDefaults.getOutgoingWss())
                && (outgoingWss == null || endpointDefaults.getMode() != EndpointConfig.Mode.COMPLEMENT)) {
            outgoingWss = wssContainer.getOutgoingWssByName(endpointDefaults.getOutgoingWss());
        }

        if (outgoingWss != null) {
            try {
                Document wssDocument = getWssDocument(context);
                if (!"true".equals(System.getProperty("soapui.savewss"))) {
                    context.setProperty("PreWssProcessedDocument", XmlUtils.serialize(wssDocument));
                }

                outgoingWss.processOutgoing(wssDocument, context);
                updateWssDocument(context, wssDocument);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        IncomingWss incomingWss = wssContainer.getIncomingWssByName(wsdlRequest.getIncomingWss());

        if (StringUtils.hasContent(endpointDefaults.getIncomingWss())
                && (incomingWss == null || endpointDefaults.getMode() != EndpointConfig.Mode.COMPLEMENT)) {
            incomingWss = wssContainer.getIncomingWssByName(endpointDefaults.getIncomingWss());
        }

        if (incomingWss != null) {
            context.setProperty(INCOMING_WSS_PROPERTY, incomingWss);
        }
    }
}
