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

package com.eviware.soapui.impl.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunContext;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MockRequestDataSource;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MultipartMessageSupport;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import javax.mail.MessagingException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;

public abstract class AbstractMockRequest implements MockRequest {
    private StringToStringsMap requestHeaders;
    private String requestContent;
    private MultipartMessageSupport multipartMessageSupport;
    private final HttpServletResponse response;
    private String protocol;
    private String path;
    private final WsdlMockRunContext context;
    private final WsdlMockRunContext requestContext;
    private final HttpServletRequest request;
    private MockRequestDataSource mockRequestDataSource;
    private String actualRequestContent;
    private boolean responseMessage;
    private XmlObject requestXmlObject;

    public AbstractMockRequest(HttpServletRequest request, HttpServletResponse response, WsdlMockRunContext context) throws Exception {
        this.request = request;
        this.response = response;
        this.context = context;

        requestContext = new WsdlMockRunContext(context.getMockService(), null);

        requestHeaders = new StringToStringsMap();
        for (Enumeration<?> e = request.getHeaderNames(); e.hasMoreElements(); ) {
            String header = (String) e.nextElement();
            String lcHeader = header.toLowerCase();
            if (lcHeader.equals("soapaction")) {
                requestHeaders.put("SOAPAction", request.getHeader(header));
            } else if (lcHeader.equals("content-type")) {
                requestHeaders.put("Content-Type", request.getHeader(header));
            } else if (lcHeader.equals("content-length")) {
                requestHeaders.put("Content-Length", request.getHeader(header));
            } else if (lcHeader.equals("content-encoding")) {
                requestHeaders.put("Content-Encoding", request.getHeader(header));
            } else {
                requestHeaders.put(header, request.getHeader(header));
            }
        }

        protocol = request.getProtocol();
        path = request.getPathInfo();
        if (path == null) {
            path = "";
        }

        if ("POST".equals(request.getMethod())) {
            initPostRequest(request, context);
        }

    }

    protected void initPostRequest(HttpServletRequest request, WsdlMockRunContext context) throws Exception {
        String contentType = request.getContentType();

        if (isMultiPart(contentType)) {
            readMultipartRequest(request);
            contentType = getMultiPartContentType(contentType);
        } else {
            String requestContent = readRequestContent(request);
            setRequestContent(requestContent);
        }

        initProtocolSpecificPostContent(context, contentType);
    }

    protected void initProtocolSpecificPostContent(WsdlMockRunContext context, String contentType) throws IOException {
        //Implemented by sub classes
    }

    protected boolean isMultiPart(String contentType) {
        return contentType != null && contentType.toUpperCase().startsWith("MULTIPART");
    }

    private String getMultiPartContentType(String contentType) {
        MultipartMessageSupport multipartMessageSupport = getMultipartMessageSupport();
        if (multipartMessageSupport != null && multipartMessageSupport.getRootPart() != null) {
            contentType = multipartMessageSupport.getRootPart().getContentType();
        }
        return contentType;
    }

    private void readMultipartRequest(HttpServletRequest request) throws MessagingException {
        StringToStringMap values = StringToStringMap.fromHttpHeader(request.getContentType());
        MockRequestDataSource mockRequestDataSource = new MockRequestDataSource(request);
        setMockRequestDataSource(mockRequestDataSource);
        Settings settings = getRequestContext().getMockService().getSettings();
        boolean isPrettyPrint = settings.getBoolean(WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES);
        MultipartMessageSupport mmSupport = new MultipartMessageSupport(mockRequestDataSource, values.get("start"), null, true, isPrettyPrint);
        setMultipartMessageSupport(mmSupport);
    }

    private String readRequestContent(HttpServletRequest request) throws Exception {
        String messageContent = null;
        String encoding = request.getCharacterEncoding();
        if (encoding != null) {
            encoding = StringUtils.unquote(encoding);
        }

        ServletInputStream is = request.getInputStream();
        if (is.markSupported() && request.getContentLength() > 0) {
            is.mark(request.getContentLength());
        }

        ByteArrayOutputStream out = Tools.readAll(is, Tools.READ_ALL);
        byte[] data = out.toByteArray();

        if (is.markSupported() && request.getContentLength() > 0) {
            try {
                is.reset();
            } catch (IOException e) {
                SoapUI.logError(e);
            }
        }

        // decompress
        String compressionAlg = HttpClientSupport.getCompressionType(request.getContentType(),
                getRequestHeaders().get("Content-Encoding", (String) null));

        if (compressionAlg != null) {
            try {
                data = CompressionSupport.decompress(compressionAlg, data);
            } catch (Exception e) {
                IOException ioe = new IOException("Decompression of response failed");
                ioe.initCause(e);
                throw ioe;
            }
        }

        int contentOffset = 0;

        String contentType = request.getContentType();
        if (contentType != null && data.length > 0) {
            if (contentType.toLowerCase().endsWith("xml")) {
                if (data.length > 3 && data[0] == (byte) 239 && data[1] == (byte) 187 && data[2] == (byte) 191) {
                    encoding = "UTF-8";
                    contentOffset = 3;
                }
            }

            encoding = StringUtils.unquote(encoding);

            messageContent = encoding == null ? new String(data) : new String(data, contentOffset,
                    (int) (data.length - contentOffset), encoding);
        }

        if (encoding == null) {
            encoding = "UTF-8";
        }

        if (messageContent == null) {
            messageContent = new String(data, encoding);
        }

        return messageContent;
    }


    public String getProtocol() {
        return protocol;
    }


    public Attachment[] getRequestAttachments() {
        return multipartMessageSupport == null ? new Attachment[0] : multipartMessageSupport.getAttachments();
    }

    public String getRequestContent() {
        return multipartMessageSupport == null ? requestContent : multipartMessageSupport.getContentAsString();
    }

    public StringToStringsMap getRequestHeaders() {
        return requestHeaders;
    }


    public HttpServletResponse getHttpResponse() {
        return response;
    }

    public HttpServletRequest getHttpRequest() {
        return request;
    }

    public RestRequestInterface.HttpMethod getMethod() {
        return RestRequestInterface.HttpMethod.valueOf(request.getMethod());
    }

    public String getPath() {
        return path;
    }

    public WsdlMockRunContext getContext() {
        return context;
    }

    public void setOperation(WsdlOperation operation) {
        if (multipartMessageSupport != null) {
            multipartMessageSupport.setOperation(operation);
        }
    }

    public WsdlMockRunContext getRequestContext() {
        return requestContext;
    }

    public byte[] getRawRequestData() {
        return mockRequestDataSource == null ? actualRequestContent == null ? requestContent.getBytes()
                : actualRequestContent.getBytes() : mockRequestDataSource.getData();
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setActualRequestContent(String actualRequestContent) {
        this.actualRequestContent = actualRequestContent;
    }

    public void setMultipartMessageSupport(MultipartMessageSupport multipartMessageSupport) {
        this.multipartMessageSupport = multipartMessageSupport;
    }

    public MultipartMessageSupport getMultipartMessageSupport() {
        return multipartMessageSupport;
    }

    public void setRequestContent(String requestContent) {
        this.requestContent = requestContent;
    }

    public void setMockRequestDataSource(MockRequestDataSource mockRequestDataSource) {
        this.mockRequestDataSource = mockRequestDataSource;
    }

    public void setResponseMessage(boolean responseMessage) {
        this.responseMessage = responseMessage;
    }

    public boolean isResponseMessage() {
        return responseMessage;
    }

    public void setRequestXmlObject(XmlObject requestXmlObject) {
        this.requestXmlObject = requestXmlObject;
    }

    public XmlObject getRequestXmlObject() throws XmlException {
        if (requestXmlObject == null && StringUtils.hasContent(getRequestContent()))
        // requestXmlObject = XmlObject.Factory.parse( getRequestContent() );
        {
            requestXmlObject = XmlUtils.createXmlObject(getRequestContent(), XmlUtils.createDefaultXmlOptions());
        }

        return requestXmlObject;
    }
}
