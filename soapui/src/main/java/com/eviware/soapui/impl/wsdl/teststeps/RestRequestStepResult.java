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

import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathsContainer;
import com.eviware.soapui.impl.wsdl.teststeps.actions.ShowMessageExchangeAction;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.testsuite.AssertedXPath;
import com.eviware.soapui.model.testsuite.MessageExchangeTestStepResult;
import com.eviware.soapui.model.testsuite.ResponseAssertedMessageExchange;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;

import javax.annotation.CheckForNull;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TestStepResult for a WsdlTestRequestStep
 *
 * @author ole.matzura
 */

public class RestRequestStepResult extends WsdlTestStepResult implements ResponseAssertedMessageExchange,
        AssertedXPathsContainer, MessageExchangeTestStepResult {
    private String requestContent;
    @CheckForNull
    private HttpResponse response;
    private String domain;
    private String username;
    private String endpoint;
    private String encoding;
    private String password;
    private StringToStringMap properties;
    private boolean addedAction;
    private List<AssertedXPath> assertedXPaths;

    public RestRequestStepResult(HttpTestRequestStepInterface step) {
        super((WsdlTestStep) step);
    }

    @Override
    public Operation getOperation() {
        if (response == null) {
            response = null;
        }
        return response == null ? null : response.getRequest().getOperation();
    }

    @Override
    public ModelItem getModelItem() {
        if (response != null) {
            return response.getRequest();
        } else {
            return null;
        }
    }

    @Override
    public String getRequestContent() {
        if (isDiscarded()) {
            return "<discarded>";
        }

        return requestContent;
    }

    public void setRequestContent(String requestContent) {
        this.requestContent = requestContent;
    }

    @Override
    public HttpResponse getResponse() {
        return response;
    }

    @Override
    public ActionList getActions() {
        if (!addedAction) {
            addAction(new ShowMessageExchangeAction(this, "TestStep"), true);
            addedAction = true;
        }

        return super.getActions();
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
        addProperty("Domain", domain);
    }

    public void addProperty(String key, String value) {
        if (properties == null) {
            properties = new StringToStringMap();
        }

        properties.put(key, value);
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
        addProperty("Encoding", encoding);
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        addProperty("Endpoint", endpoint);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        addProperty("Password", password);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        addProperty("Username", username);
    }

    @Override
    public void discard() {
        super.discard();

        requestContent = null;
        response = null;
        properties = null;
        assertedXPaths = null;
    }

    @Override
    public void writeTo(PrintWriter writer) {
        super.writeTo(writer);

        if (response == null) {
            writer.println("\r\n- missing response / garbage collected -");
            return;
        }

        writer.println("\r\n----------------- Properties ------------------------------");
        if (properties != null) {
            for (String key : properties.keySet()) {
                if (properties.get(key) != null) {
                    writer.println(key + ": " + properties.get(key));
                }
            }
        }

        writer.println("\r\n---------------- Request ---------------------------");
        StringToStringsMap headers = response.getRequestHeaders();
        for (Map.Entry<String, List<String>> headerEntry : headers.entrySet()) {
            if (headerEntry.getValue() != null) {
                writer.println(headerEntry.getKey() + ": " + headerEntry.getValue());
            }
        }

        byte[] rawRequestData = response.getRawRequestData();
        if (rawRequestData != null) {
            writer.println("\r\n" + new String(rawRequestData));
        }

        writer.println("\r\n---------------- Response --------------------------");

        headers = response.getResponseHeaders();
        for (String key : headers.keySet()) {
            if (headers.get(key) != null) {
                writer.println(key + ": " + headers.get(key));
            }
        }

        String respContent = response.getContentAsString();
        if (respContent != null) {
            writer.println("\r\n" + respContent);
        }
    }

    @Override
    public StringToStringMap getProperties() {
        return properties;
    }

    @Override
    public String getProperty(String name) {
        return properties == null ? null : properties.get(name);
    }

    @Override
    public Attachment[] getRequestAttachments() {
        if (response == null || response.getRequest() == null) {
            return new Attachment[0];
        }

        return response.getRequest().getAttachments();
    }

    @Override
    public StringToStringsMap getRequestHeaders() {
        if (response == null) {
            return null;
        }

        return response.getRequestHeaders();
    }

    @Override
    public Attachment[] getResponseAttachments() {
        if (response == null) {
            return new Attachment[0];
        }

        return response.getAttachments();
    }

    @Override
    public String getResponseContent() {
        if (isDiscarded()) {
            return "<discarded>";
        }

        if (response == null) {
            return "<missing response>";
        }

        return response.getContentAsString();
    }

    @Override
    public String getRequestContentAsXml() {
        return XmlUtils.seemsToBeXml(requestContent) ? requestContent : "<not-xml/>";
    }

    @Override
    public String getResponseContentAsXml() {
        if (response == null) {
            return null;
        }
        return response.getContentAsXml();
    }

    @Override
    public StringToStringsMap getResponseHeaders() {
        if (response == null) {
            return new StringToStringsMap();
        }

        return response.getResponseHeaders();
    }

    @Override
    public long getTimestamp() {
        if (isDiscarded() || response == null) {
            return -1;
        }

        return response.getTimestamp();
    }

    @Override
    public AssertedXPath[] getAssertedXPathsForResponse() {
        return assertedXPaths == null ? new AssertedXPath[0] : assertedXPaths.toArray(new AssertedXPath[assertedXPaths
                .size()]);
    }

    @Override
    public void addAssertedXPath(AssertedXPath assertedXPath) {
        if (assertedXPaths == null) {
            assertedXPaths = new ArrayList<AssertedXPath>();
        }

        assertedXPaths.add(assertedXPath);
    }

    @Override
    public MessageExchange[] getMessageExchanges() {
        return new MessageExchange[]{this};
    }

    @Override
    public byte[] getRawRequestData() {
        return response.getRawRequestData();
    }

    @Override
    public byte[] getRawResponseData() {
        return response.getRawResponseData();
    }

    @Override
    public Attachment[] getRequestAttachmentsForPart(String partName) {
        return null;
    }

    @Override
    public Attachment[] getResponseAttachmentsForPart(String partName) {
        return null;
    }

    @Override
    public boolean hasRawData() {
        return getRawResponseData() != null || getRawRequestData() != null;
    }

    @Override
    public boolean hasRequest(boolean b) {
        return true;
    }

    @Override
    public boolean hasResponse() {
        return response != null;
    }
}
