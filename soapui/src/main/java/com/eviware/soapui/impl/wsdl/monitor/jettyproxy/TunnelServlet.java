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

package com.eviware.soapui.impl.wsdl.monitor.jettyproxy;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction.LaunchForm;
import com.eviware.soapui.impl.wsdl.actions.monitor.SoapMonitorAction.SecurityTabForm;
import com.eviware.soapui.impl.wsdl.monitor.CaptureInputStream;
import com.eviware.soapui.impl.wsdl.monitor.JProxyServletWsdlMonitorMessageExchange;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitorListenerCallBack;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedGetMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.impl.wsdl.support.http.SoapUIHttpRoute;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.http.Header;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.mortbay.util.IO;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class TunnelServlet extends ProxyServlet {
    private String sslEndPoint;
    private int sslPort = 443;
    private String prot = "https://";

    public TunnelServlet(WsdlProject project, String sslEndpoint, SoapMonitorListenerCallBack listenerCallBack) {
        super(project, listenerCallBack);

        if (!sslEndpoint.startsWith("https")) {
            this.prot = "http://";
        }
        int prefix = sslEndpoint.indexOf("://");
        int c = sslEndpoint.indexOf(prefix, ':');
        if (c > 0) {
            this.sslPort = Integer.parseInt(sslEndpoint.substring(c + 1));
            this.sslEndPoint = sslEndpoint.substring(prefix, c);
        } else {
            if (prefix > 0) {
                this.sslEndPoint = sslEndpoint.substring(prefix + 3);
            }
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        this.context = config.getServletContext();
    }

    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        listenerCallBack.fireOnRequest(project, request, response);
        if (response.isCommitted()) {
            return;
        }

        ExtendedHttpMethod postMethod;

        // for this create ui server and port, properties.
        InetSocketAddress inetAddress = new InetSocketAddress(sslEndPoint, sslPort);
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (httpRequest.getMethod().equals("GET")) {
            postMethod = new ExtendedGetMethod();
        } else {
            postMethod = new ExtendedPostMethod();
        }

        JProxyServletWsdlMonitorMessageExchange capturedData = new JProxyServletWsdlMonitorMessageExchange(project);
        capturedData.setRequestHost(httpRequest.getRemoteHost());
        capturedData.setRequestHeader(httpRequest);
        capturedData.setHttpRequestParameters(httpRequest);
        capturedData.setTargetURL(this.prot + inetAddress.getHostName());

        CaptureInputStream capture = new CaptureInputStream(httpRequest.getInputStream());

        long contentLength = -1;
        // copy headers
        Enumeration<?> headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String hdr = (String) headerNames.nextElement();
            String lhdr = hdr.toLowerCase();

            if ("content-length".equals(lhdr)) {
                String val = httpRequest.getHeader(hdr);
                contentLength = Long.parseLong(val);
                continue;
            }

            if ("transfer-encoding".equals(lhdr)) {
                continue;
            }

            if ("host".equals(lhdr)) {
                Enumeration<?> vals = httpRequest.getHeaders(hdr);
                while (vals.hasMoreElements()) {
                    String val = (String) vals.nextElement();
                    if (val.startsWith("127.0.0.1")) {
                        postMethod.addHeader(hdr, sslEndPoint);
                    }
                }
                continue;
            }

            Enumeration<?> vals = httpRequest.getHeaders(hdr);
            while (vals.hasMoreElements()) {
                String val = (String) vals.nextElement();
                if (val != null) {
                    postMethod.addHeader(hdr, val);
                }
            }
        }

        if (postMethod instanceof ExtendedPostMethod) {
            InputStreamEntity entity = new InputStreamEntity(capture, contentLength);
            entity.setContentType(request.getContentType());
            ((ExtendedPostMethod) postMethod).setEntity(entity);
        }

        java.net.URI uri = null;
        try {
            uri = new java.net.URI(this.prot + sslEndPoint);
        } catch (URISyntaxException e) {
            SoapUI.logError(e);
        }

        postMethod.getParams().setParameter(
                SoapUIHttpRoute.SOAPUI_SSL_CONFIG,
                settings.getString(SecurityTabForm.SSLTUNNEL_KEYSTOREPATH, "") + " "
                        + settings.getString(SecurityTabForm.SSLTUNNEL_KEYSTOREPASSWORD, ""));

        setProtocolversion(postMethod, request.getProtocol());

        String path = null;
        if (!sslEndPoint.contains("/")) {
            path = "/";
        } else {
            path = sslEndPoint.substring(sslEndPoint.indexOf("/"), sslEndPoint.length());
        }

        if (uri != null) {
            try {
                postMethod.setURI(URIUtils.createURI(uri.getScheme(), uri.getHost(), uri.getPort(), path, uri.getQuery(),
                        uri.getFragment()));
            } catch (URISyntaxException e) {
                SoapUI.logError(e);
            }
        }

        listenerCallBack.fireBeforeProxy(project, request, response, postMethod);

        if (settings.getBoolean(LaunchForm.SSLTUNNEL_REUSESTATE)) {
            if (httpState == null) {
                httpState = new BasicHttpContext();
            }
            HttpClientSupport.execute(postMethod, httpState);
        } else {
            HttpClientSupport.execute(postMethod);
        }
        capturedData.stopCapture();

        capturedData.setRequest(capture.getCapturedData());
        capturedData.setRawResponseBody(postMethod.getResponseBody());
        capturedData.setResponseHeader(postMethod.getHttpResponse());
        capturedData.setRawRequestData(getRequestToBytes(request.toString(), postMethod, capture));
        capturedData.setRawResponseData(getResponseToBytes(response.toString(), postMethod,
                capturedData.getRawResponseBody()));

        listenerCallBack.fireAfterProxy(project, request, response, postMethod, capturedData);

        StringToStringsMap responseHeaders = capturedData.getResponseHeaders();
        // copy headers to response
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        for (Map.Entry<String, List<String>> headerEntry : responseHeaders.entrySet()) {
            for (String header : headerEntry.getValue()) {
                httpServletResponse.addHeader(headerEntry.getKey(), header);
            }

        }

        IO.copy(new ByteArrayInputStream(capturedData.getRawResponseBody()), httpServletResponse.getOutputStream());

        synchronized (this) {
            listenerCallBack.fireAddMessageExchange(capturedData);
        }

    }


    private byte[] getResponseToBytes(String footer, ExtendedHttpMethod postMethod, byte[] res) {
        String response = footer;

        if (postMethod.hasHttpResponse()) {
            Header[] headers = postMethod.getHttpResponse().getAllHeaders();
            for (Header header : headers) {
                response += header.toString().trim() + "\n";
            }
            response += "\n";
            response += XmlUtils.prettyPrintXml(new String(res));
        }
        return response.getBytes();
    }

    private byte[] getRequestToBytes(String footer, ExtendedHttpMethod postMethod, CaptureInputStream capture) {
        String request = footer;

        // Header[] headers = postMethod.getRequestHeaders();
        // for (Header header : headers)
        // {
        // request += header.toString();
        // }
        request += "\n";
        request += XmlUtils.prettyPrintXml(new String(capture.getCapturedData()));

        return request.getBytes();
    }

}
