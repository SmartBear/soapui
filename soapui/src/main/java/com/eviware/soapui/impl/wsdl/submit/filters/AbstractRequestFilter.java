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

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public abstract class AbstractRequestFilter implements RequestFilter {
    public void filterRequest(SubmitContext context, Request request) {
        if (request instanceof AbstractHttpRequestInterface<?>) {
            filterAbstractHttpRequest(context, (AbstractHttpRequest<?>) request);
        }
    }

    public void filterAbstractHttpRequest(SubmitContext context, AbstractHttpRequest<?> request) {
        if (request instanceof WsdlRequest) {
            filterWsdlRequest(context, (WsdlRequest) request);
        } else if (request instanceof RestRequestInterface) {
            filterRestRequest(context, (RestRequestInterface) request);
        } else if (request instanceof HttpRequestInterface<?>) {
            filterHttpRequest(context, (HttpRequestInterface<?>) request);
        }
    }

    public void filterWsdlRequest(SubmitContext context, WsdlRequest request) {
    }

    public void filterRestRequest(SubmitContext context, RestRequestInterface request) {
    }

    public void filterHttpRequest(SubmitContext context, HttpRequestInterface<?> request) {
    }

    public void afterRequest(SubmitContext context, Request request) {
        // do this for backwards compatibility
        Response response = (Response) context.getProperty(BaseHttpRequestTransport.RESPONSE);
        if (response != null) {
            afterRequest(context, response);
        }

        if (request instanceof AbstractHttpRequestInterface<?>) {
            afterAbstractHttpResponse(context, (AbstractHttpRequestInterface<?>) request);
        }
    }

    public void afterAbstractHttpResponse(SubmitContext context, AbstractHttpRequestInterface<?> request) {
        if (request instanceof WsdlRequest) {
            afterWsdlRequest(context, (WsdlRequest) request);
        } else if (request instanceof RestRequestInterface) {
            afterRestRequest(context, (RestRequestInterface) request);
        } else if (request instanceof HttpRequestInterface<?>) {
            afterHttpRequest(context, (HttpRequestInterface<?>) request);
        }
    }

    public void afterWsdlRequest(SubmitContext context, WsdlRequest request) {
    }

    public void afterRestRequest(SubmitContext context, RestRequestInterface request) {
    }

    public void afterHttpRequest(SubmitContext context, HttpRequestInterface<?> request) {
    }

    public void afterRequest(SubmitContext context, Response response) {
    }
}
