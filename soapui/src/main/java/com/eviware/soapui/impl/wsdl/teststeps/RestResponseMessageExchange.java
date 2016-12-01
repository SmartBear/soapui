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

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.wsdl.submit.AbstractRestMessageExchange;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;

public class RestResponseMessageExchange extends AbstractRestMessageExchange<RestRequestInterface> {
    private HttpResponse response;
    private String requestContent;

    public RestResponseMessageExchange(RestRequestInterface request) {
        super(request);

        response = (discardResponse == true) ? null : request.getResponse();
        if (response != null) {
            for (String key : response.getPropertyNames()) {
                addProperty(key, response.getProperty(key));
            }
        }
    }

    public String getEndpoint() {
        return response == null ? null : response.getURL().toString();
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

    public String getRequestContent() {
        if (requestContent != null) {
            return requestContent;
        }

        if (response == null) {
            response = getModelItem().getResponse();
        }

        return response == null ? getModelItem().getRequestContent() : response.getRequestContent();
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

    public String getRequestContentAsXml() {
        String result = getRequestContent();
        return XmlUtils.seemsToBeXml(result) ? result : "<not-xml/>";
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    public String getResponseContentAsXml() {
        if (response == null) {
            response = getModelItem().getResponse();
        }

        return response == null ? null : response.getContentAsXml();
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

    public void setRequestContent(String requestContent) {
        this.requestContent = requestContent;
    }

    public boolean isDiscarded() {
        return discardResponse;
    }

    public RestResource getResource() {
        return getModelItem().getResource();
    }

    public RestRequestInterface getRestRequest() {
        return getModelItem();
    }

    public Operation getOperation() {
        return getResource();
    }

    public int getResponseStatusCode() {
        return response == null ? 0 : response.getStatusCode();
    }

    public String getResponseContentType() {
        return response == null ? null : response.getContentType();
    }
}
