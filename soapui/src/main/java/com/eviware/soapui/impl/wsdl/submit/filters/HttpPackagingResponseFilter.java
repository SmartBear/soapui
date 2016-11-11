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

import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.SinglePartHttpResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MimeMessageResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.WsdlMimeMessageResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.WsdlSinglePartHttpResponse;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import org.apache.http.Header;

public class HttpPackagingResponseFilter extends AbstractRequestFilter {
    @Override
    public void afterAbstractHttpResponse(SubmitContext context, AbstractHttpRequestInterface<?> request) {
        ExtendedHttpMethod httpMethod = (ExtendedHttpMethod) context.getProperty(BaseHttpRequestTransport.HTTP_METHOD);
        String requestContent = (String) context.getProperty(BaseHttpRequestTransport.REQUEST_CONTENT);

        // check content-type for multipart
        Header responseContentTypeHeader = null;
        if (httpMethod.hasHttpResponse()) {
            Header[] headers = httpMethod.getHttpResponse().getHeaders("Content-Type");
            if (headers != null && headers.length > 0) {
                responseContentTypeHeader = headers[0];
            }
        }
        Response response = null;
        if (request instanceof WsdlRequest) {
            response = wsdlRequest(context, (WsdlRequest) request, httpMethod, responseContentTypeHeader, requestContent);
        } else if (request instanceof HttpRequestInterface<?>) {
            response = httpRequest(context, (HttpRequestInterface<?>) request, httpMethod, responseContentTypeHeader,
                    requestContent);
        }

        context.setProperty(BaseHttpRequestTransport.RESPONSE, response);

    }

    private Response wsdlRequest(SubmitContext context, WsdlRequest request, ExtendedHttpMethod httpMethod,
                                 Header responseContentTypeHeader, String requestContent) {
        if (context.hasProperty("PreWssProcessedDocument")) {
            requestContent = String.valueOf(context.getProperty("PreWssProcessedDocument"));
        }

        XmlBeansSettingsImpl settings = request.getSettings();
        if (!settings.getBoolean(WsdlRequest.INLINE_RESPONSE_ATTACHMENTS) && responseContentTypeHeader != null
                && responseContentTypeHeader.getValue().toUpperCase().startsWith("MULTIPART")) {
            return new WsdlMimeMessageResponse(request, httpMethod, requestContent, context);
        } else {
            return new WsdlSinglePartHttpResponse(request, httpMethod, requestContent, context);
        }
    }

    private Response httpRequest(SubmitContext context, HttpRequestInterface<?> request, ExtendedHttpMethod httpMethod,
                                 Header responseContentTypeHeader, String requestContent) {
        if (responseContentTypeHeader != null
                && responseContentTypeHeader.getValue().toUpperCase().startsWith("MULTIPART")) {
            return new MimeMessageResponse(request, httpMethod, requestContent, context);
        } else {
            return new SinglePartHttpResponse(request, httpMethod, requestContent, context);
        }
    }
}
