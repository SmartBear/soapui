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

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.model.iface.SubmitContext;
import org.apache.http.HttpRequest;

/**
 * RequestFilter that adds SOAP specific headers
 *
 * @author Ole.Matzura
 */

public class SoapHeadersRequestFilter extends AbstractRequestFilter {
    public void filterWsdlRequest(SubmitContext context, WsdlRequest wsdlRequest) {
        HttpRequest postMethod = (HttpRequest) context.getProperty(BaseHttpRequestTransport.HTTP_METHOD);

        WsdlInterface wsdlInterface = (WsdlInterface) wsdlRequest.getOperation().getInterface();

        // init content-type and encoding
        String encoding = System.getProperty("soapui.request.encoding", wsdlRequest.getEncoding());

        SoapVersion soapVersion = wsdlInterface.getSoapVersion();
        String soapAction = wsdlRequest.isSkipSoapAction() ? null : wsdlRequest.getAction();

        postMethod.setHeader("Content-Type", soapVersion.getContentTypeHttpHeader(encoding, soapAction));

        if (!wsdlRequest.isSkipSoapAction()) {
            String soapActionHeader = soapVersion.getSoapActionHeader(soapAction);
            if (soapActionHeader != null) {
                postMethod.setHeader("SOAPAction", soapActionHeader);
            }
        }
    }
}
