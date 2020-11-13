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

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaUtils;
import com.eviware.soapui.model.iface.SubmitContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * RequestFilter that expands properties in request content
 *
 * @author Ole.Matzura
 */

public class WsaRequestFilter extends AbstractRequestFilter {
    public final static Logger log = LogManager.getLogger(WsaRequestFilter.class);

    public void filterAbstractHttpRequest(SubmitContext context, AbstractHttpRequest<?> wsdlRequest) {
        if (!(wsdlRequest instanceof WsdlRequest) || !((WsdlRequest) wsdlRequest).isWsAddressing()) {
            return;
        }

        String content = (String) context.getProperty(BaseHttpRequestTransport.REQUEST_CONTENT);
        if (content == null) {
            log.warn("Missing request content in context, skipping ws-addressing");
        } else {
            ExtendedHttpMethod httpMethod = (ExtendedHttpMethod) context
                    .getProperty(BaseHttpRequestTransport.HTTP_METHOD);
            WsdlOperation operation = ((WsdlRequest) wsdlRequest).getOperation();
            // TODO check UsingAddressing for particular endpoint when running a
            // request
            // ((WsdlRequest)wsdlRequest).getEndpoint();
            SoapVersion soapVersion = operation.getInterface().getSoapVersion();
            content = new WsaUtils(content, soapVersion, operation, context).addWSAddressingRequest(
                    (WsdlRequest) wsdlRequest, httpMethod);
            if (content != null) {
                context.setProperty(BaseHttpRequestTransport.REQUEST_CONTENT, content);
            }
        }
    }

}
