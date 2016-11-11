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

package com.eviware.soapui.impl.wsdl.panels.teststeps.amf;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.support.AbstractResponse;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.MessageHeader;
import org.apache.http.Header;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;

public class AMFResponse extends AbstractResponse<AMFRequest> {

    public static final String AMF_POST_METHOD = "AMF_POST_METHOD";
    public static final String AMF_RESPONSE_HEADERS = "responseHeaders";
    public static final String AMF_RESPONSE_ACTION_MESSAGE = "AMF_RESPONSE_ACTION_MESSAGE";
    public static final String AMF_RAW_RESPONSE_BODY = "AMF_RAW_RESPONSE_BODY";

    private String responseContentXML = "";
    private long timeTaken;
    private long timestamp;
    private AMFRequest request;
    private StringToStringsMap requestHeaders;
    private StringToStringsMap responseHeaders;
    private StringToStringMap responseAMFHeaders = new StringToStringMap();
    private byte[] rawRequestData;
    private byte[] rawResponseData;
    private ActionMessage actionMessage;
    private byte[] rawResponseBody;

    public AMFResponse(AMFRequest request, SubmitContext submitContext, Object responseContent) throws SQLException,
            ParserConfigurationException, TransformerConfigurationException, TransformerException {
        super(request);

        this.request = request;
        if (responseContent != null) {
            setResponseContentXML(new com.thoughtworks.xstream.XStream().toXML(responseContent));
        }
        this.actionMessage = (ActionMessage) submitContext.getProperty(AMF_RESPONSE_ACTION_MESSAGE);
        this.rawResponseBody = (byte[]) submitContext.getProperty(AMF_RAW_RESPONSE_BODY);
        initHeaders((ExtendedPostMethod) submitContext.getProperty(AMF_POST_METHOD));

    }

    public String getContentAsString() {
        return getResponseContentXML();
    }

    public String getContentType() {
        return "text/xml";
    }

    public long getContentLength() {
        return rawResponseData != null ? rawResponseData.length : 0;
    }

    public String getRequestContent() {
        return request.toString();
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setResponseContentXML(String responseContentXML) {
        this.responseContentXML = responseContentXML;
    }

    public String getResponseContentXML() {
        return responseContentXML;
    }

    protected void initHeaders(ExtendedPostMethod postMethod) {
        requestHeaders = new StringToStringsMap();
        responseHeaders = new StringToStringsMap();

        if (postMethod == null) {
            return;
        }

        try {
            ByteArrayOutputStream rawResponse = new ByteArrayOutputStream();
            ByteArrayOutputStream rawRequest = new ByteArrayOutputStream();

            if (!postMethod.isFailed() && postMethod.hasHttpResponse()) {
                rawResponse.write(String.valueOf(postMethod.getHttpResponse().getStatusLine()).getBytes());
                rawResponse.write("\r\n".getBytes());
            }

            rawRequest.write((postMethod.getMethod() + " " + postMethod.getURI().toString() + " "
                    + postMethod.getProtocolVersion().toString() + "\r\n").getBytes());

            Header[] headers = postMethod.getAllHeaders();
            for (Header header : headers) {
                requestHeaders.add(header.getName(), header.getValue());
                rawRequest.write(toExternalForm(header).getBytes());
            }

            if (!postMethod.isFailed() && postMethod.hasHttpResponse()) {
                headers = postMethod.getHttpResponse().getAllHeaders();
                for (Header header : headers) {
                    responseHeaders.add(header.getName(), header.getValue());
                    rawResponse.write(toExternalForm(header).getBytes());
                }

                responseHeaders.add("#status#", String.valueOf(postMethod.getHttpResponse().getStatusLine()));
            }

            if (postMethod.getRequestEntity() != null) {
                rawRequest.write("\r\n".getBytes());
                if (postMethod.getRequestEntity().isRepeatable() && postMethod.getEntity() != null) {
                    postMethod.getEntity().writeTo(rawRequest);
                } else {
                    rawRequest.write("<request data not available>".getBytes());
                }
            }

            if (!postMethod.isFailed()) {
                rawResponse.write("\r\n".getBytes());
                rawResponse.write(rawResponseBody);
            }

            rawResponseData = rawResponse.toByteArray();
            rawRequestData = rawRequest.toByteArray();

            initAMFHeaders(postMethod);

        } catch (Throwable e) {
            SoapUI.logError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void initAMFHeaders(ExtendedPostMethod postMethod) {
        if (!postMethod.isFailed() && actionMessage != null) {
            ArrayList<MessageHeader> amfHeaders = actionMessage.getHeaders();

            for (MessageHeader header : amfHeaders) {
                responseAMFHeaders.put(header.getName(), header.getData().toString());
            }
        }
    }

    public byte[] getRawRequestData() {
        return rawRequestData;
    }

    public byte[] getRawResponseData() {
        return rawResponseData;
    }

    public StringToStringsMap getRequestHeaders() {
        return requestHeaders;
    }

    public StringToStringsMap getResponseHeaders() {
        return responseHeaders;
    }

    public StringToStringMap getResponseAMFHeaders() {
        return responseAMFHeaders;
    }

    /**
     * Returns a {@link String} representation of the header.
     *
     * @return stringHEAD
     */
    public String toExternalForm(Header header) {
        return ((null == header.getName() ? "" : header.getName()) + ": "
                + (null == header.getValue() ? "" : header.getValue()) + "\r\n");
    }

}
