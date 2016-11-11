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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.submit.AbstractMessageExchange;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.types.StringToStringsMap;

import java.util.ArrayList;
import java.util.List;

public class HttpResponseMessageExchange extends AbstractMessageExchange<HttpRequestInterface<?>> {
    private HttpResponse response;
    private String requestContent;

    public HttpResponseMessageExchange(HttpRequestInterface<?> request) {
        super(request);

        response = (isDiscarded() == true) ? null : request.getResponse();
        if (response != null) {
            for (String key : response.getPropertyNames()) {
                addProperty(key, response.getProperty(key));
            }
        }
    }

    public String getEndpoint() {
        return response == null ? null : response.getURL().toString();
    }

    public String getRequestContent() {
        if (requestContent != null) {
            return requestContent;
        }

        if (response == null) {
            response = getModelItem().getResponse();
        }

        return response == null ? getModelItem().getRequestContent() : response.getRequestContent();
    }

    @Override
    public String getResponseContentAsXml() {
        if (response == null) {
            response = getModelItem().getResponse();
        }

        return response.getContentAsXml();
    }

    public StringToStringsMap getRequestHeaders() {
        return response == null ? getModelItem().getRequestHeaders() : response.getRequestHeaders();
    }

    public Attachment[] getRequestAttachments() {
        return getModelItem().getAttachments();
    }

    public Attachment[] getResponseAttachments() {
        if (response == null) {
            response = getModelItem().getResponse();
        }

        return response == null ? null : response.getAttachments();
    }

    public String getResponseContent() {
        if (response == null) {
            response = getModelItem().getResponse();
        }

        return response == null ? null : response.getContentAsString();
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    public StringToStringsMap getResponseHeaders() {
        if (response == null) {
            response = getModelItem().getResponse();
        }

        return response == null ? new StringToStringsMap() : response.getResponseHeaders();
    }

    public long getTimeTaken() {
        if (response == null) {
            response = getModelItem().getResponse();
        }

        return response == null ? 0 : response.getTimeTaken();
    }

    public long getTimestamp() {
        if (response == null) {
            response = getModelItem().getResponse();
        }

        return response == null ? 0 : response.getTimestamp();
    }

    public boolean isDiscarded() {
        return discardResponse;
    }

    public Operation getOperation() {
        return null;
    }

    public int getResponseStatusCode() {
        return response == null ? 0 : response.getStatusCode();
    }

    public String getResponseContentType() {
        return response == null ? null : response.getContentType();
    }

    public boolean hasRawData() {
        return response != null;
    }

    public byte[] getRawRequestData() {
        return response == null ? null : response.getRawRequestData();
    }

    public byte[] getRawResponseData() {
        return response == null ? null : response.getRawResponseData();
    }

    public Attachment[] getResponseAttachmentsForPart(String name) {
        List<Attachment> result = new ArrayList<Attachment>();

        if (getResponseAttachments() != null) {
            for (Attachment attachment : getResponseAttachments()) {
                if (attachment.getPart().equals(name)) {
                    result.add(attachment);
                }
            }
        }

        return result.toArray(new Attachment[result.size()]);
    }

    public Attachment[] getRequestAttachmentsForPart(String name) {
        List<Attachment> result = new ArrayList<Attachment>();

        for (Attachment attachment : getRequestAttachments()) {
            if (attachment.getPart().equals(name)) {
                result.add(attachment);
            }
        }

        return result.toArray(new Attachment[result.size()]);
    }

    public boolean hasRequest(boolean ignoreEmpty) {
        String requestContent = getRequestContent();
        return !(requestContent == null || (ignoreEmpty && requestContent.trim().length() == 0));
    }

    public boolean hasResponse() {
        String responseContent = getResponseContent();
        return responseContent != null && responseContent.trim().length() > 0;
    }
}
