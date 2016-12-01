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
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.uri.HttpParser;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.http.Header;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class TcpMonWsdlMonitorMessageExchange extends WsdlMonitorMessageExchange {
    private URL targetUrl;
    private StringToStringsMap responseHeaders;
    private long timeTaken;
    private long timestamp;
    private StringToStringsMap requestHeaders;
    private String requestContent;
    private String responseContent;
    private int responseContentLength;
    private int requestContentLength;
    private String requestHost;
    private WsdlProject project;
    private WsdlOperation operation;
    private byte[] capturedRequestData;
    private byte[] capturedResponseData;
    private String responseContentType;
    private MultipartMessageSupport responseMmSupport;

    private static final String HTTP_ELEMENT_CHARSET = "US-ASCII";
    private SoapVersion soapVersion;
    private MultipartMessageSupport requestMmSupport;
    private String requestContentType;
    private boolean discarded;
    private Vector requestWssResult;
    private Vector responseWssResult;

    public TcpMonWsdlMonitorMessageExchange(WsdlProject project) {
        super(null);
        this.project = project;
        responseHeaders = new StringToStringsMap();
        requestHeaders = new StringToStringsMap();
        timestamp = System.currentTimeMillis();
    }

    public String getEndpoint() {
        return targetUrl == null ? null : targetUrl.toString();
    }

    @Override
    public WsdlOperation getOperation() {
        return operation;
    }

    @Override
    public Response getResponse() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Operation getModelItem() {
        return operation;
    }

    public Attachment[] getRequestAttachments() {
        return requestMmSupport == null ? new Attachment[0] : requestMmSupport.getAttachments();
    }

    public String getRequestContent() {
        return requestMmSupport == null ? requestContent : requestMmSupport.getContentAsString();
    }

    public StringToStringsMap getRequestHeaders() {
        return requestHeaders;
    }

    public Attachment[] getResponseAttachments() {
        return responseMmSupport == null ? new Attachment[0] : responseMmSupport.getAttachments();
    }

    public String getResponseContent() {
        return responseMmSupport == null ? responseContent : responseMmSupport.getContentAsString();
    }

    public StringToStringsMap getResponseHeaders() {
        return responseHeaders;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTargetUrl(URL targetHost) {
        this.targetUrl = targetHost;
    }

    public boolean isActive() {
        return false;
    }

    public long getRequestContentLength() {
        return requestContentLength;
    }

    public long getResponseContentLength() {
        return responseContentLength;
    }

    public String getRequestHost() {
        return requestHost;
    }

    public URL getTargetUrl() {
        return targetUrl;
    }

    public byte[] getRawRequestData() {
        return capturedRequestData;
    }

    public byte[] getRawResponseData() {
        return capturedResponseData;
    }

    @Override
    public boolean hasRawData() {
        return true;
    }

    public void finish(byte[] capturedRequestData, byte[] capturedResponseData) {
        this.capturedRequestData = capturedRequestData;
        this.capturedResponseData = capturedResponseData;

        if (timeTaken == 0) {
            timeTaken = System.currentTimeMillis() - timestamp;
        }
    }

    public void prepare(IncomingWss requestWss, IncomingWss responseWss) {
        parseRequestData(capturedRequestData, requestWss);
        parseReponseData(capturedResponseData, responseWss);
    }

    private void parseReponseData(byte[] capturedResponseData, IncomingWss responseWss) {
        responseContentLength = capturedResponseData.length;
        ByteArrayInputStream in = new ByteArrayInputStream(capturedResponseData);
        try {

            String line = null;
            do {
                line = HttpParser.readLine(in, HTTP_ELEMENT_CHARSET);
            }
            while (line != null && line.length() == 0);

            if (line == null) {
                throw new Exception("Missing request status line");
            }

            Header[] headers = HttpParser.parseHeaders(in, HTTP_ELEMENT_CHARSET);
            if (headers != null) {
                for (Header header : headers) {
                    responseHeaders.put(header.getName(), header.getValue());
                }
            }

            responseContentType = responseHeaders.get("Content-Type", "");
            if (responseContentType != null && responseContentType.toUpperCase().startsWith("MULTIPART")) {
                StringToStringMap values = StringToStringMap.fromHttpHeader(responseContentType);
                responseMmSupport = new MultipartMessageSupport(new MonitorMessageExchangeDataSource("monitor response",
                        in, responseContentType), values.get("start"), null, true, SoapUI.getSettings().getBoolean(
                        WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES));
                responseContentType = responseMmSupport.getRootPart().getContentType();
            } else {
                this.responseContent = XmlUtils.prettyPrintXml(Tools.readAll(in, 0).toString());
            }

            processResponseWss(responseWss);
        } catch (Exception e) {
            try {
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void processResponseWss(IncomingWss responseWss) throws IOException {
        if (responseWss != null) {
            Document dom = XmlUtils.parseXml(responseContent);
            try {
                responseWssResult = responseWss.processIncoming(dom, new DefaultPropertyExpansionContext(project));
                if (responseWssResult != null && responseWssResult.size() > 0) {
                    StringWriter writer = new StringWriter();
                    XmlUtils.serialize(dom, writer);
                    responseContent = writer.toString();
                }
            } catch (Exception e) {
                if (responseWssResult == null) {
                    responseWssResult = new Vector();
                }
                responseWssResult.add(e);
            }
        }
    }

    private void parseRequestData(byte[] capturedRequestData, IncomingWss requestWss) {
        requestContentLength = capturedRequestData.length;
        ByteArrayInputStream in = new ByteArrayInputStream(capturedRequestData);
        try {

            String line = null;
            do {
                line = HttpParser.readLine(in, HTTP_ELEMENT_CHARSET);
            }
            while (line != null && line.length() == 0);

            if (line == null) {
                throw new Exception("Missing request status line");
            }

            Header[] headers = HttpParser.parseHeaders(in, HTTP_ELEMENT_CHARSET);
            if (headers != null) {
                for (Header header : headers) {
                    requestHeaders.put(header.getName(), header.getValue());
                }
            }

            requestContentType = requestHeaders.get("Content-Type", "");
            if (requestContentType != null && requestContentType.toUpperCase().startsWith("MULTIPART")) {
                StringToStringMap values = StringToStringMap.fromHttpHeader(requestContentType);
                requestMmSupport = new MultipartMessageSupport(new MonitorMessageExchangeDataSource("monitor request",
                        in, requestContentType), values.get("start"), null, true, SoapUI.getSettings().getBoolean(
                        WsdlSettings.PRETTY_PRINT_RESPONSE_MESSAGES));
                requestContentType = requestMmSupport.getRootPart().getContentType();
            } else {
                this.requestContent = XmlUtils.prettyPrintXml(Tools.readAll(in, 0).toString());
            }

            processRequestWss(requestWss);

            operation = findOperation();
        } catch (Exception e) {
            try {
                in.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void processRequestWss(IncomingWss requestWss) throws IOException {
        if (requestWss != null) {
            Document dom = XmlUtils.parseXml(requestContent);
            try {
                requestWssResult = requestWss.processIncoming(dom, new DefaultPropertyExpansionContext(project));
                if (requestWssResult != null && requestWssResult.size() > 0) {
                    StringWriter writer = new StringWriter();
                    XmlUtils.serialize(dom, writer);
                    requestContent = writer.toString();
                }
            } catch (Exception e) {
                if (requestWssResult == null) {
                    requestWssResult = new Vector();
                }
                requestWssResult.add(e);
            }
        }
    }

    private WsdlOperation findOperation() throws Exception {
        soapVersion = SoapUtils.deduceSoapVersion(requestContentType, getRequestContent());
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

    public void setRequestHost(String requestHost) {
        this.requestHost = requestHost;
    }

    public SoapVersion getSoapVersion() {
        if (soapVersion == null) {
            soapVersion = SoapUtils.deduceSoapVersion(requestHeaders.get("Content-Type", ""), getRequestContent());
        }

        return soapVersion;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public void discard() {
        operation = null;
        project = null;

        requestContent = null;
        requestHeaders = null;

        responseContent = null;
        responseHeaders = null;

        requestMmSupport = null;

        discarded = true;
    }

    public boolean isDiscarded() {
        return discarded;
    }

    public Vector getRequestWssResult() {
        return requestWssResult;
    }

    public Vector getResponseWssResult() {
        return responseWssResult;
    }

    public int getResponseStatusCode() {
        return 0; // To change body of implemented methods use File | Settings |
        // File Templates.
    }

    public String getResponseContentType() {
        return null; // To change body of implemented methods use File | Settings
        // | File Templates.
    }

    @Override
    public String getRequestMethod() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, String> getHttpRequestParameters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getQueryParameters() {
        return "";
    }

}
