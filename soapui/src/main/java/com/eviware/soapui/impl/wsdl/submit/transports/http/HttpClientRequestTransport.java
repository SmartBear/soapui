/*
 * SoapUI, Copyright (C) 2004-2018 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.HttpUtils;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MimeMessageResponse;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedCopyMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedDeleteMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedGetMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedHeadMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedLockMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedOptionsMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPatchMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPropFindMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPurgeMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPutMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedTraceMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedUnlockMethod;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.impl.wsdl.support.http.HeaderRequestInterceptor;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.support.http.SoapUIHttpRoute;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.settings.HttpSettings;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;
import org.apache.commons.httpclient.URI;
import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP transport that uses HttpClient to send/receive SOAP messages
 *
 * @author Ole.Matzura
 */

public class HttpClientRequestTransport implements BaseHttpRequestTransport {
    private List<RequestFilter> filters = new ArrayList<RequestFilter>();

    public HttpClientRequestTransport() {
    }

    public void addRequestFilter(RequestFilter filter) {
        filters.add(filter);
    }

    public void removeRequestFilter(RequestFilter filterToRemove) {
        if (!filters.remove(filterToRemove)) {
            for (RequestFilter requestFilter : filters) {
                if (requestFilter.getClass().equals(filterToRemove.getClass())) {
                    filters.remove(requestFilter);
                    break;
                }
            }
        }
    }

    @Override
    public void insertRequestFilter(RequestFilter filter, RequestFilter refFilter) {
        int ix = filters.indexOf( refFilter );
        if( ix == -1 )
            filters.add( filter );
        else
            filters.add( ix, filter );
    }

    public <T> void removeRequestFilter(Class<T> filterClass) {
        RequestFilter filter = findFilterByType(filterClass);

        if (filter != null) {
            removeRequestFilter(filter);
        }
    }

    public <T> void replaceRequestFilter(Class<T> filterClass, RequestFilter newFilter) {
        RequestFilter filter = findFilterByType(filterClass);

        if (filter != null) {
            for (int i = 0; i < filters.size(); i++) {
                RequestFilter oldFilter = filters.get(i);
                if (oldFilter == filter) {
                    filters.remove(i);
                    filters.add(i, newFilter);
                    break;
                }
            }
        }
    }

    @CheckForNull
    public <T extends Object> RequestFilter findFilterByType(Class<T> filterType) {
        for (int i = 0; i < filters.size(); i++) {
            RequestFilter filter = filters.get(i);
            if (filter.getClass() == filterType) {
                return filter;
            }
        }
        return null;
    }

    public void abortRequest(SubmitContext submitContext) {
        HttpRequestBase postMethod = (HttpRequestBase) submitContext.getProperty(HTTP_METHOD);
        if (postMethod != null) {
            postMethod.abort();
        }
    }

    public Response sendRequest(SubmitContext submitContext, Request request) throws Exception {
        AbstractHttpRequestInterface<?> httpRequest = (AbstractHttpRequestInterface<?>) request;

        HttpClientSupport.SoapUIHttpClient httpClient = getSoapUIHttpClient();
        ExtendedHttpMethod httpMethod = createHttpMethod(httpRequest);

        boolean createdContext = false;
        HttpContext httpContext = (HttpContext) submitContext.getProperty(SubmitContext.HTTP_STATE_PROPERTY);
        if (httpContext == null) {
            httpContext = HttpClientSupport.createEmptyContext();
            submitContext.setProperty(SubmitContext.HTTP_STATE_PROPERTY, httpContext);
            createdContext = true;
        }

        String localAddress = System.getProperty("soapui.bind.address", httpRequest.getBindAddress());
        if (localAddress == null || localAddress.trim().length() == 0) {
            localAddress = SoapUI.getSettings().getString(HttpSettings.BIND_ADDRESS, null);
        }

        org.apache.http.HttpResponse httpResponse;
        if (localAddress != null && localAddress.trim().length() > 0) {
            try {
                httpMethod.getParams().setParameter(ConnRoutePNames.LOCAL_ADDRESS, InetAddress.getByName(localAddress));
            } catch (Exception e) {
                SoapUI.logError(e, "Failed to set localAddress to [" + localAddress + "]");
            }
        }

        submitContext.removeProperty(RESPONSE);
        submitContext.setProperty(HTTP_METHOD, httpMethod);
        submitContext.setProperty(POST_METHOD, httpMethod);
        submitContext.setProperty(HTTP_CLIENT, httpClient);
        submitContext.setProperty(REQUEST_CONTENT, httpRequest.getRequestContent());
        submitContext.setProperty(WSDL_REQUEST, httpRequest);
        submitContext.setProperty(RESPONSE_PROPERTIES, new StringToStringMap());

        filterRequest(submitContext, httpRequest);

        try {
            Settings settings = httpRequest.getSettings();

            // custom http headers last so they can be overridden
            StringToStringsMap headers = httpRequest.getRequestHeaders();

            // clear headers specified in GUI, and re-add them, with property expansion
            for (String headerName : headers.keySet()) {
                String expandedHeaderName = PropertyExpander.expandProperties(submitContext, headerName);
                httpMethod.removeHeaders(expandedHeaderName);
                for (String headerValue : headers.get(headerName)) {
                    headerValue = PropertyExpander.expandProperties(submitContext, headerValue);
                    httpMethod.addHeader(expandedHeaderName, headerValue);
                }
            }

            // do request
            WsdlProject project = (WsdlProject) ModelSupport.getModelItemProject(httpRequest);
            WssCrypto crypto = null;
            if (project != null && project.getWssContainer() != null) {
                crypto = project.getWssContainer().getCryptoByName(
                        PropertyExpander.expandProperties(submitContext, httpRequest.getSslKeystore()));
            }

            if (crypto != null && WssCrypto.STATUS_OK.equals(crypto.getStatus())) {
                httpMethod.getParams().setParameter(SoapUIHttpRoute.SOAPUI_SSL_CONFIG,
                        crypto.getSource() + " " + crypto.getPassword());
            }

            // dump file?
            httpMethod.setDumpFile(PathUtils.expandPath(httpRequest.getDumpFile(),
                    (AbstractWsdlModelItem<?>) httpRequest, submitContext));

            // include request time?
            if (settings.getBoolean(HttpSettings.INCLUDE_REQUEST_IN_TIME_TAKEN)) {
                httpMethod.initStartTime();
            }

            if (httpMethod.getMetrics() != null) {
                httpMethod.getMetrics().setHttpMethod(httpMethod.getMethod());
                captureMetrics(httpMethod, httpClient);
                httpMethod.getMetrics().getTotalTimer().start();
            }

            // submit!
            httpResponse = submitRequest(httpMethod, httpContext);

            // save request headers captured by interceptor
            saveRequestHeaders(httpMethod, httpContext);

            if (httpMethod.getMetrics() != null) {
                httpMethod.getMetrics().getReadTimer().stop();
                httpMethod.getMetrics().getTotalTimer().stop();
            }

            if (isRedirectResponse(httpResponse.getStatusLine().getStatusCode()) && httpRequest.isFollowRedirects()) {
                if (httpResponse.getEntity() != null) {
                    EntityUtils.consume(httpResponse.getEntity());
                }

                httpMethod = followRedirects(httpClient, 0, httpMethod, httpResponse, httpContext, submitContext);
                submitContext.setProperty(HTTP_METHOD, httpMethod);
            }
        } catch (Throwable t) {
            httpMethod.setFailed(t);

            if (t instanceof Exception) {
                throw (Exception) t;
            }

            SoapUI.logError(t);
            throw new Exception(t);
        } finally {
            if (!httpMethod.isFailed()) {
                if (httpMethod.getMetrics() != null) {
                    if (httpMethod.getMetrics().getReadTimer().getStop() == 0) {
                        httpMethod.getMetrics().getReadTimer().stop();
                    }
                    if (httpMethod.getMetrics().getTotalTimer().getStop() == 0) {
                        httpMethod.getMetrics().getTotalTimer().stop();
                    }
                }
            } else {
                httpMethod.getMetrics().reset();
                httpMethod.getMetrics().setTimestamp(System.currentTimeMillis());
                captureMetrics(httpMethod, httpClient);
            }

            for (int c = filters.size() - 1; c >= 0; c--) {
                RequestFilter filter = filters.get(c);
                filter.afterRequest(submitContext, httpRequest);
            }

            if (!submitContext.hasProperty(RESPONSE)) {
                createDefaultResponse(submitContext, httpRequest, httpMethod);
            }

            Response response = (Response) submitContext.getProperty(BaseHttpRequestTransport.RESPONSE);
            StringToStringMap responseProperties = (StringToStringMap) submitContext
                    .getProperty(BaseHttpRequestTransport.RESPONSE_PROPERTIES);

            for (String key : responseProperties.keySet()) {
                response.setProperty(key, responseProperties.get(key));
            }

            if (createdContext) {
                submitContext.setProperty(SubmitContext.HTTP_STATE_PROPERTY, null);
            }
        }


        return (Response) submitContext.getProperty(BaseHttpRequestTransport.RESPONSE);
    }

    protected org.apache.http.HttpResponse submitRequest(ExtendedHttpMethod httpMethod, HttpContext httpContext) throws IOException {
        return HttpClientSupport.execute(httpMethod, httpContext);
    }

    protected HttpClientSupport.SoapUIHttpClient getSoapUIHttpClient() {
        return HttpClientSupport.getHttpClient();
    }

    private boolean isRedirectResponse(int statusCode) {
        switch (statusCode) {
            case 301:
            case 302:
            case 303:
            case 307:
                return true;
        }

        return false;
    }
    
    private void filterRequest(SubmitContext submitContext, AbstractHttpRequestInterface<?> httpRequest) {
		for(RequestFilter filter: filters) {
			filter.filterRequest(submitContext, httpRequest);
		}
	}
    
    private boolean isPostMethod(ExtendedHttpMethod httpMethod, org.apache.http.HttpResponse httpResponse) {
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		return (statusCode != HttpServletResponse.SC_SEE_OTHER && 
				httpMethod != null &&
				httpMethod.getMethod()
				.equals(RestRequestInterface.HttpMethod.POST.toString()));
	}

    private ExtendedHttpMethod followRedirects(HttpClient httpClient, int redirectCount, ExtendedHttpMethod httpMethod,
    										   org.apache.http.HttpResponse httpResponse, HttpContext httpContext, SubmitContext submitContext) throws Exception {
		ExtendedHttpMethod getMethod;
		if(isPostMethod(httpMethod, httpResponse))
			getMethod = new ExtendedPostMethod();
		else {
			getMethod = new ExtendedGetMethod();
		}

		submitContext.setProperty("httpMethod", getMethod);
		AbstractHttpRequestInterface<?> httpRequest = (AbstractHttpRequestInterface<?>)submitContext.getProperty(WSDL_REQUEST);
		filterRequest(submitContext, httpRequest);

        getMethod
                .getMetrics()
                .getTotalTimer()
                .set(httpMethod.getMetrics().getTotalTimer().getStart(), httpMethod.getMetrics().getTotalTimer().getStop());
        getMethod.getMetrics().setHttpMethod(httpMethod.getMethod());
        captureMetrics(httpMethod, httpClient);

        String location = httpResponse.getFirstHeader("Location").getValue();
        URI uri = new URI(new URI(httpMethod.getURI().toString(), true), location, true);
        java.net.URI newUri = HttpUtils.createUri(uri.getScheme(), uri.getEscapedUserinfo(), uri.getHost(), uri.getPort(),
                uri.getEscapedPath(), uri.getEscapedQuery(), uri.getEscapedFragment());
        getMethod.setURI(newUri);

        // Thijs Brentjens: if the location contains a different Host, due to the redirect, then use that instead of the already existing Host-header. So just set the Host header to the new host of the URI
        
        getMethod.setHeader("Host",uri.getHost());

        org.apache.http.HttpResponse response = submitRequest(getMethod, httpContext);

        if (isRedirectResponse(response.getStatusLine().getStatusCode())) {
            if (redirectCount == 10) {
                throw new Exception("Maximum number of Redirects reached [10]");
            }

            try {
                getMethod = followRedirects(httpClient, redirectCount + 1, getMethod, response, httpContext, 
								submitContext);
            } finally {
                //TODO: check if this is necessary!
                //getMethod.releaseConnection();
            }
        }

        for (Header header : httpMethod.getAllHeaders()) {
            getMethod.addHeader(header);
        }

        return getMethod;
    }

    private void createDefaultResponse(SubmitContext submitContext, AbstractHttpRequestInterface<?> httpRequest,
                                       ExtendedHttpMethod httpMethod) {
        String requestContent = (String) submitContext.getProperty(BaseHttpRequestTransport.REQUEST_CONTENT);

        // check content-type for multipart
        String responseContentTypeHeader = null;
        if (httpMethod.hasHttpResponse() && httpMethod.getHttpResponse().getEntity() != null) {
            Header h = httpMethod.getHttpResponse().getEntity().getContentType();
            responseContentTypeHeader = h.toString();
        }

        Response response;
        if (responseContentTypeHeader != null && responseContentTypeHeader.toUpperCase().startsWith("MULTIPART")) {
            response = new MimeMessageResponse(httpRequest, httpMethod, requestContent, submitContext);
        } else {
            response = new SinglePartHttpResponse(httpRequest, httpMethod, requestContent, submitContext);
        }

        submitContext.setProperty(BaseHttpRequestTransport.RESPONSE, response);
    }

    private ExtendedHttpMethod createHttpMethod(AbstractHttpRequestInterface<?> httpRequest) {
        if (httpRequest instanceof HttpRequestInterface<?>) {
            HttpRequestInterface<?> restRequest = (HttpRequestInterface<?>) httpRequest;
            switch (restRequest.getMethod()) {
                case GET:
                    return new ExtendedGetMethod();
                case HEAD:
                    return new ExtendedHeadMethod();
                case DELETE:
                    return new ExtendedDeleteMethod();
                case PUT:
                    return new ExtendedPutMethod();
                case OPTIONS:
                    return new ExtendedOptionsMethod();
                case TRACE:
                    return new ExtendedTraceMethod();
                case PATCH:
                    return new ExtendedPatchMethod();
                case PROPFIND:
                    return new ExtendedPropFindMethod();
                case LOCK:
                    return new ExtendedLockMethod();
                case UNLOCK:
                    return new ExtendedUnlockMethod();
                case COPY:
                    return new ExtendedCopyMethod();
                case PURGE:
                    return new ExtendedPurgeMethod();

            }
        }

        ExtendedPostMethod extendedPostMethod = new ExtendedPostMethod();

        extendedPostMethod.setAfterRequestInjection(httpRequest.getAfterRequestInjection());
        return extendedPostMethod;
    }

    private void captureMetrics(ExtendedHttpMethod httpMethod, HttpClient httpClient) {
        try {
            httpMethod.getMetrics().setIpAddress(InetAddress.getByName(httpMethod.getURI().getHost()).getHostAddress());
            httpMethod.getMetrics().setPort(
                    httpMethod.getURI().getPort(),
                    getDefaultHttpPort(httpMethod, httpClient));
        } catch (UnknownHostException uhe) {
            /* ignore */
        } catch (IllegalStateException ise) {
            /* ignore */
        }
    }

    protected int getDefaultHttpPort(ExtendedHttpMethod httpMethod, HttpClient httpClient) {
        return httpClient.getConnectionManager().getSchemeRegistry().getScheme(httpMethod.getURI().getScheme())
                .getDefaultPort();
    }

    private void saveRequestHeaders(ExtendedHttpMethod httpMethod, HttpContext httpContext) {
        List<Header> requestHeaders = (List<Header>) httpContext
                .getAttribute(HeaderRequestInterceptor.SOAPUI_REQUEST_HEADERS);

        if (requestHeaders != null) {
            for (Header header : requestHeaders) {
                Header[] existingHeaders = httpMethod.getHeaders(header.getName());

                int c = 0;
                for (; c < existingHeaders.length; c++) {
                    if (existingHeaders[c].getValue().equals(header.getValue())) {
                        break;
                    }
                }

                if (c == existingHeaders.length) {
                    httpMethod.addHeader(header);
                }
            }
        }
    }

}
