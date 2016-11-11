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

package com.eviware.soapui.impl.wsdl.monitor;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MultipartMessageSupport;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class JProxyServletWsdlMonitorMessageExchange extends WsdlMonitorMessageExchange {

    private WsdlOperation operation;
    private WsdlProject project;
    private String requestContent;
    private StringToStringsMap requestHeaders;
    private String responseContent;
    private StringToStringsMap responseHeaders;
    private MultipartMessageSupport requestMmSupport;
    private boolean discarded;
    private long timestampStart;
    private byte[] request;
    private byte[] response;
    private String requestHost;
    private URL targetURL;
    private String requestContentType;
    private Vector<Object> requestWssResult;
    private SoapVersion soapVersion;
    private String responseContentType;
    private MultipartMessageSupport responseMmSupport;
    private Vector<Object> responseWssResult;
    private long timestampEnd;
    private boolean capture;
    private byte[] requestRaw = null;
    private byte[] responseRaw = null;
    private String requestMethod = null;
    private Map<String, String> httpRequestParameters;
    private int statusCode;
    private String responseStatusLine;
    private String queryParameters;

    public JProxyServletWsdlMonitorMessageExchange(WsdlProject project) {
        super(null);
        responseHeaders = new StringToStringsMap();
        requestHeaders = new StringToStringsMap();
        timestampStart = System.currentTimeMillis();
        this.project = project;
        capture = true;
    }

    public String getEndpoint() {
        return targetURL == null ? null : targetURL.toString();
    }

    @Override
    public void discard() {
        operation = null;
        project = null;

        requestContent = null;
        requestHeaders = null;

        responseContent = null;
        responseHeaders = null;

        requestMmSupport = null;

        response = null;
        request = null;
        capture = false;
        queryParameters = null;

        discarded = true;
    }

    @Override
    public Response getResponse() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getRequestContentLength() {
        return request == null ? -1 : request.length;
    }

    @Override
    public String getRequestHost() {
        return requestHost;
    }

    @Override
    public long getResponseContentLength() {
        return response == null ? -1 : response.length;
    }

    @Override
    public URL getTargetUrl() {
        return this.targetURL;
    }

    @Override
    public void prepare(IncomingWss incomingRequestWss, IncomingWss incomingResponseWss) {
        parseRequestData(incomingRequestWss);
        parseResponseData(incomingResponseWss);
    }

    private void parseResponseData(IncomingWss incomingResponseWss) {
        ByteArrayInputStream in = new ByteArrayInputStream(response == null ? new byte[0] : response);
        try {
            responseContentType = responseHeaders.get("Content-Type", "");

            if (responseContent == null) {
                if (responseContentType != null && responseContentType.toUpperCase().startsWith("MULTIPART")) {
                    StringToStringMap values = StringToStringMap.fromHttpHeader(responseContentType);
                    responseMmSupport = new MultipartMessageSupport(new MonitorMessageExchangeDataSource(
                            "monitor response", in, responseContentType), values.get("start"), null, true, false);
                    responseContentType = responseMmSupport.getRootPart().getContentType();
                } else {
                    String charset = getCharset(responseHeaders);
                    this.responseContent = charset == null ? Tools.readAll(in, 0).toString() : Tools.readAll(in, 0)
                            .toString(charset);
                }
            }

            processResponseWss(incomingResponseWss);
        } catch (Exception e) {
            SoapUI.logError(e);
        } finally {
            try {
                in.close();
            } catch (IOException e1) {
                SoapUI.logError(e1);
            }
        }
    }

    @Override
    public Operation getModelItem() {
        return operation;
    }

    public void setResponseContent(String content) throws IOException {
        this.responseContent = content;
    }

    private void processResponseWss(IncomingWss incomingResponseWss) throws IOException {
        if (incomingResponseWss != null) {
            Document dom = XmlUtils.parseXml(responseContent);
            try {
                responseWssResult = incomingResponseWss
                        .processIncoming(dom, new DefaultPropertyExpansionContext(project));
                if (responseWssResult != null && responseWssResult.size() > 0) {
                    StringWriter writer = new StringWriter();
                    XmlUtils.serialize(dom, writer);
                    responseContent = writer.toString();
                }
            } catch (Exception e) {
                if (responseWssResult == null) {
                    responseWssResult = new Vector<Object>();
                }
                responseWssResult.add(e);
            }
        }

    }

    private void parseRequestData(IncomingWss incomingRequestWss) {
        ByteArrayInputStream in = request == null ? new ByteArrayInputStream(new byte[0]) : new ByteArrayInputStream(
                request);
        try {
            requestContentType = requestHeaders.get("Content-Type", "");
            if (requestContentType != null && requestContentType.toUpperCase().startsWith("MULTIPART")) {
                StringToStringMap values = StringToStringMap.fromHttpHeader(requestContentType);
                requestMmSupport = new MultipartMessageSupport(new MonitorMessageExchangeDataSource("monitor request",
                        in, requestContentType), values.get("start"), null, true, false);
                requestContentType = requestMmSupport.getRootPart() != null ? requestMmSupport.getRootPart()
                        .getContentType() : null;
            } else {
                String charset = getCharset(requestHeaders);
                this.requestContent = charset == null ? Tools.readAll(in, 0).toString() : Tools.readAll(in, 0)
                        .toString(charset);
            }

            processRequestWss(incomingRequestWss);

            if (checkParse()) {
                operation = findOperation();
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        } finally {
            try {
                in.close();
            } catch (IOException e1) {
                SoapUI.logError(e1);
            }
        }
    }

    private boolean checkParse() {
        try {
            // XmlObject.Factory.parse( getRequestContent() );
            XmlUtils.createXmlObject(getRequestContent());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static String getCharset(StringToStringsMap headers) {
        String requestContentType = headers.get("Content-Type", "");
        if (requestContentType != null) {
            StringToStringMap values = StringToStringMap.fromHttpHeader(requestContentType);
            if (values.containsKey("charset")) {
                return values.get("charset");
            }
        }

        String contentEncodingHeader = headers.get("Content-Encoding", "");
        if (contentEncodingHeader != null) {
            try {
                if (CompressionSupport.getAvailableAlgorithm(contentEncodingHeader) == null) {
                    "".getBytes(contentEncodingHeader);
                    return contentEncodingHeader;
                }
            } catch (Exception e) {
            }
        }

        return null;
    }

    private WsdlOperation findOperation() throws Exception {
        // soapVersion = SoapUtils.deduceSoapVersion( requestContentType,
        // XmlObject.Factory.parse( getRequestContent() ) );
        soapVersion = SoapUtils.deduceSoapVersion(requestContentType, XmlUtils.createXmlObject(getRequestContent()));
        if (soapVersion == null) {
            throw new Exception("Unrecognized SOAP Version");
        }

        String soapAction = SoapUtils.getSoapAction(soapVersion, requestHeaders);

        List<WsdlOperation> operations = new ArrayList<WsdlOperation>();
        for (WsdlInterface iface : ModelSupport.getChildren(project, WsdlInterface.class)) {
            for (Operation operation : iface.getOperationList()) {
                operations.add((WsdlOperation) operation);
            }
        }

        // return SoapUtils.findOperationForRequest( soapVersion, soapAction,
        // XmlObject.Factory.parse( getRequestContent() ), operations, true,
        // false, getRequestAttachments() );
        return SoapUtils.findOperationForRequest(soapVersion, soapAction,
                XmlUtils.createXmlObject(getRequestContent()), operations, true, false, getRequestAttachments());
    }

    private void processRequestWss(IncomingWss incomingRequestWss) throws IOException {

        if (incomingRequestWss != null) {
            Document dom = XmlUtils.parseXml(requestContent);
            try {
                requestWssResult = incomingRequestWss.processIncoming(dom, new DefaultPropertyExpansionContext(project));
                if (requestWssResult != null && requestWssResult.size() > 0) {
                    StringWriter writer = new StringWriter();
                    XmlUtils.serialize(dom, writer);
                    requestContent = writer.toString();
                }
            } catch (Exception e) {
                if (requestWssResult == null) {
                    requestWssResult = new Vector<Object>();
                }
                requestWssResult.add(e);
            }
        }

    }

    public WsdlOperation getOperation() {
        return operation;
    }

    public Vector<?> getRequestWssResult() {
        return requestWssResult;
    }

    public Vector<?> getResponseWssResult() {
        return responseWssResult;
    }

    public Attachment[] getRequestAttachments() {
        return requestMmSupport == null ? new Attachment[0] : requestMmSupport.getAttachments();
    }

    public String getRequestContent() {
        return requestMmSupport == null ? requestContent : requestMmSupport.getContentAsString();
    }

    public byte[] getRawRequestData() {
        if (requestRaw != null) {
            return requestRaw;
        } else {
            return request;
        }
    }

    public void setRawRequestData(byte[] data) {
        requestRaw = data;
    }

    public byte[] getRawResponseData() {
        if (responseRaw == null) {
            return response;
        } else {
            return responseRaw;
        }
    }

    public void setRawResponseData(byte[] data) {
        responseRaw = data;
    }

    public StringToStringsMap getRequestHeaders() {
        return requestHeaders;
    }

    public Attachment[] getResponseAttachments() {
        return requestMmSupport == null ? new Attachment[0] : requestMmSupport.getAttachments();
    }

    public String getResponseContent() {
        return responseContent;
    }

    public StringToStringsMap getResponseHeaders() {
        return responseHeaders;
    }

    public long getTimeTaken() {
        return timestampEnd - timestampStart;
    }

    public long getTimestamp() {
        return timestampStart;
    }

    public boolean isDiscarded() {
        return discarded;
    }

    public void stopCapture() {
        timestampEnd = System.currentTimeMillis();
        capture = false;
    }

    public boolean isStopCapture() {
        return capture;
    }

    public byte[] getRawRequestBody() {
        return request;
    }

    public void setRequest(byte[] request) {
        this.request = request;
    }

    public byte[] getRawResponseBody() {
        return response;
    }

    public void setRawResponseBody(byte[] response) {
        this.response = response;
    }

    public void setResponseHeader(String name, String value) {
        responseHeaders.put(name, value);
    }

    public void setRequestHost(String serverName) {
        requestHost = serverName;
    }

    public void setTargetHost(String remoteHost) {
    }

    @SuppressWarnings("unchecked")
    public void setRequestHeader(HttpServletRequest httpRequest) {
        Enumeration<String> headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            Enumeration<String> header = httpRequest.getHeaders(name);
            while (header.hasMoreElements()) {
                String value = header.nextElement();
                if (value != null) {
                    requestHeaders.put(name, value);
                }
            }
        }
    }

    public void setTargetURL(String url) {
        try {
            this.targetURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public int getResponseStatusCode() {
        return statusCode;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public void setResponseHeader(HttpResponse response) {
        Header[] headers = response.getAllHeaders();
        for (Header header : headers) {
            String name = header.getName();
            String value = header.getValue();
            if (value != null) {
                responseHeaders.put(name, value);
            }
        }
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setHttpRequestParameters(HttpServletRequest httpRequest) {
        Enumeration<String> parameterNames = httpRequest.getParameterNames();
        Map<String, String> parameterMap = new HashMap<String, String>();
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            parameterMap.put(name, httpRequest.getParameter(name));
        }

        this.httpRequestParameters = parameterMap;
    }

    public Map<String, String> getHttpRequestParameters() {
        return httpRequestParameters;
    }

    @Override
    public String getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(String queryParameters) {
        this.queryParameters = queryParameters;
    }

    public void setResponseStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setResponseStatusLine(String responseStatusLine) {
        this.responseStatusLine = responseStatusLine;
    }

    public WsdlProject getProject() {
        return project;
    }
}
