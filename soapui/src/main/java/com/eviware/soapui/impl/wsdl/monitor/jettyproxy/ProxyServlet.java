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

package com.eviware.soapui.impl.wsdl.monitor.jettyproxy;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction;
import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction.LaunchForm;
import com.eviware.soapui.impl.wsdl.monitor.ContentTypes;
import com.eviware.soapui.impl.wsdl.monitor.JProxyServletWsdlMonitorMessageExchange;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitorListenerCallBack;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedCopyMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedDeleteMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedGenericMethod;
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
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.support.http.ProxyUtils;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.types.StringToStringsMap;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpVersion;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.mortbay.util.IO;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ProxyServlet implements Servlet {
    protected ServletConfig config;
    protected ServletContext context;
    protected WsdlProject project;
    protected HttpContext httpState = new BasicHttpContext();
    protected Settings settings;
    protected final SoapMonitorListenerCallBack listenerCallBack;
    private ContentTypes includedContentTypes = SoapMonitorAction.defaultContentTypes();
    static HashSet<String> dontProxyHeaders = new HashSet<String>();

    static {
        dontProxyHeaders.add("proxy-connection");
        dontProxyHeaders.add("connection");
        dontProxyHeaders.add("keep-alive");
        dontProxyHeaders.add("transfer-encoding");
        dontProxyHeaders.add("te");
        dontProxyHeaders.add("trailer");
        dontProxyHeaders.add("proxy-authorization");
        dontProxyHeaders.add("proxy-authenticate");
        dontProxyHeaders.add("upgrade");
        dontProxyHeaders.add("content-length");
    }

    public ProxyServlet(final WsdlProject project, final SoapMonitorListenerCallBack listenerCallBack) {
        this.listenerCallBack = listenerCallBack;
        this.project = project;
        settings = project.getSettings();
    }

    public void destroy() {
    }

    public ServletConfig getServletConfig() {
        return config;
    }

    public String getServletInfo() {
        return "SoapUI Monitor";
    }

    public void setIncludedContentTypes(ContentTypes includedContentTypes) {
        this.includedContentTypes = includedContentTypes != null
                ? includedContentTypes
                : SoapMonitorAction.defaultContentTypes();
    }

    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        this.context = config.getServletContext();
    }

    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        listenerCallBack.fireOnRequest(project, request, response);
        if (response.isCommitted()) {
            return;
        }

        ExtendedHttpMethod method;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (httpRequest.getMethod().equals("GET")) {
            method = new ExtendedGetMethod();
        } else if (httpRequest.getMethod().equals("POST")) {
            method = new ExtendedPostMethod();
        } else if (httpRequest.getMethod().equals("PUT")) {
            method = new ExtendedPutMethod();
        } else if (httpRequest.getMethod().equals("DELETE")) {
            method = new ExtendedDeleteMethod();
        } else if (httpRequest.getMethod().equals("HEAD")) {
            method = new ExtendedHeadMethod();
        } else if (httpRequest.getMethod().equals("OPTIONS")) {
            method = new ExtendedOptionsMethod();
        } else if (httpRequest.getMethod().equals("TRACE")) {
            method = new ExtendedTraceMethod();
        } else if (httpRequest.getMethod().equals("PATCH")) {
            method = new ExtendedPatchMethod();
        } else if (httpRequest.getMethod().equals("PROPFIND")) {
            method = new ExtendedPropFindMethod();
        } else if (httpRequest.getMethod().equals("LOCK")) {
            method = new ExtendedLockMethod();
        } else if (httpRequest.getMethod().equals("UNLOCK")) {
            method = new ExtendedUnlockMethod();
        } else if (httpRequest.getMethod().equals("COPY")) {
            method = new ExtendedCopyMethod();
        } else if (httpRequest.getMethod().equals("PURGE")) {
            method = new ExtendedPurgeMethod();
        } else {
            method = new ExtendedGenericMethod(httpRequest.getMethod());
        }

        method.setDecompress(false);

        ByteArrayOutputStream requestBody = null;
        if (method instanceof HttpEntityEnclosingRequest) {
            requestBody = Tools.readAll(request.getInputStream(), 0);
            ByteArrayEntity entity = new ByteArrayEntity(requestBody.toByteArray());
            entity.setContentType(request.getContentType());
            ((HttpEntityEnclosingRequest) method).setEntity(entity);
        }

        // for this create ui server and port, properties.
        JProxyServletWsdlMonitorMessageExchange capturedData = new JProxyServletWsdlMonitorMessageExchange(project);
        capturedData.setRequestHost(httpRequest.getServerName());
        capturedData.setRequestMethod(httpRequest.getMethod());
        capturedData.setRequestHeader(httpRequest);
        capturedData.setHttpRequestParameters(httpRequest);
        capturedData.setQueryParameters(httpRequest.getQueryString());
        capturedData.setTargetURL(httpRequest.getRequestURL().toString());

        //		CaptureInputStream capture = new CaptureInputStream( httpRequest.getInputStream() );

        // check connection header
        String connectionHeader = httpRequest.getHeader("Connection");
        if (connectionHeader != null) {
            connectionHeader = connectionHeader.toLowerCase();
            if (!connectionHeader.contains("keep-alive") && !connectionHeader.contains("close")) {
                connectionHeader = null;
            }
        }

        // copy headers
        boolean xForwardedFor = false;
        @SuppressWarnings("unused")
        Enumeration<?> headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String hdr = (String) headerNames.nextElement();
            String lhdr = hdr.toLowerCase();

            if (dontProxyHeaders.contains(lhdr)) {
                continue;
            }
            if (connectionHeader != null && connectionHeader.contains(lhdr)) {
                continue;
            }

            Enumeration<?> vals = httpRequest.getHeaders(hdr);
            while (vals.hasMoreElements()) {
                String val = (String) vals.nextElement();
                if (val != null) {
                    method.setHeader(lhdr, val);
                    xForwardedFor |= "X-Forwarded-For".equalsIgnoreCase(hdr);
                }
            }
        }

        // Proxy headers
        method.setHeader("Via", "SoapUI Monitor");
        if (!xForwardedFor) {
            method.addHeader("X-Forwarded-For", request.getRemoteAddr());
        }

        StringBuffer url = new StringBuffer("http://");
        url.append(httpRequest.getServerName());
        if (httpRequest.getServerPort() != 80) {
            url.append(":" + httpRequest.getServerPort());
        }

        if (httpRequest.getServletPath() != null) {
            url.append(httpRequest.getServletPath());
            try {
                method.setURI(new java.net.URI(url.toString().replaceAll(" ", "%20")));
            } catch (URISyntaxException e) {
                SoapUI.logError(e);
            }

            if (httpRequest.getQueryString() != null) {
                url.append("?" + httpRequest.getQueryString());
                try {
                    method.setURI(new java.net.URI(url.toString()));
                } catch (URISyntaxException e) {
                    SoapUI.logError(e);
                }
            }
        }

        method.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
        setProtocolversion(method, request.getProtocol());
        ProxyUtils.setForceDirectConnection(method.getParams());
        listenerCallBack.fireBeforeProxy(project, request, response, method);

        if (settings.getBoolean(LaunchForm.SSLTUNNEL_REUSESTATE)) {
            if (httpState == null) {
                httpState = new BasicHttpContext();
            }
            HttpClientSupport.execute(method, httpState);
        } else {
            HttpClientSupport.execute(method);
        }

        // wait for transaction to end and store it.
        capturedData.stopCapture();

        capturedData.setRequest(requestBody == null ? null : requestBody.toByteArray());
        capturedData.setRawResponseBody(method.getResponseBody());
        capturedData.setResponseHeader(method.getHttpResponse());
        capturedData.setRawRequestData(getRequestToBytes(request.toString(), requestBody));
        capturedData.setRawResponseData(getResponseToBytes(method, capturedData.getRawResponseBody()));
        byte[] decompressedResponseBody = method.getDecompressedResponseBody();
        capturedData.setResponseContent(decompressedResponseBody != null ? new String(decompressedResponseBody) : "");
        capturedData.setResponseStatusCode(method.hasHttpResponse() ? method.getHttpResponse().getStatusLine()
                .getStatusCode() : null);
        capturedData.setResponseStatusLine(method.hasHttpResponse() ? method.getHttpResponse().getStatusLine()
                .toString() : null);

        listenerCallBack.fireAfterProxy(project, request, response, method, capturedData);

        ((HttpServletResponse) response).setStatus(method.hasHttpResponse() ? method.getHttpResponse()
                .getStatusLine().getStatusCode() : null);

        if (!response.isCommitted()) {
            StringToStringsMap responseHeaders = capturedData.getResponseHeaders();
            // capturedData = null;

            // copy headers to response
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            for (Map.Entry<String, List<String>> headerEntry : responseHeaders.entrySet()) {
                for (String header : headerEntry.getValue()) {
                    httpServletResponse.addHeader(headerEntry.getKey(), header);
                }
            }

            if (capturedData.getRawResponseBody() != null) {
                IO.copy(new ByteArrayInputStream(capturedData.getRawResponseBody()), httpServletResponse.getOutputStream());
            }
        }

        synchronized (this) {
            if (contentTypeMatches(method)) {
                listenerCallBack.fireAddMessageExchange(capturedData);
            }
        }
    }

    protected boolean contentTypeMatches(ExtendedHttpMethod method) {
        if (method.hasHttpResponse()) {
            Header[] headers = method.getHttpResponse().getHeaders("Content-Type");
            if (headers.length == 0) {
                return true;
            }

            for (Header header : headers) {
                if (includedContentTypes.matches(header.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    private byte[] getResponseToBytes(ExtendedHttpMethod method, byte[] res) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StringBuilder response = new StringBuilder();

        if (method.hasHttpResponse()) {
            response.append(method.getHttpResponse().getStatusLine().toString());
            response.append("\r\n");

            Header[] headers = method.getHttpResponse().getAllHeaders();
            for (Header header : headers) {
                response.append(header.toString().trim()).append("\r\n");
            }
            response.append("\r\n");

            try {
                out.write(response.toString().getBytes());
                if (res != null) {
                    out.write(res);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return out.toByteArray();
    }

    private byte[] getRequestToBytes(String footer, ByteArrayOutputStream requestBody) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            out.write(footer.trim().getBytes());
            out.write("\r\n\r\n".getBytes());
            if (requestBody != null) {
                out.write(requestBody.toByteArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    protected void setProtocolversion(ExtendedHttpMethod postMethod, String protocolVersion) {
        if (protocolVersion.equals(HttpVersion.HTTP_1_1.toString())) {
            postMethod.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        } else if (protocolVersion.equals(HttpVersion.HTTP_1_0.toString())) {
            postMethod.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_0);
        }
    }

}
