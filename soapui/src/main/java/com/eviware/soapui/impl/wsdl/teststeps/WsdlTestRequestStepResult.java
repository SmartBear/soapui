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

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathsContainer;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.teststeps.actions.ShowMessageExchangeAction;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.AssertedXPath;
import com.eviware.soapui.model.testsuite.MessageExchangeTestStepResult;
import com.eviware.soapui.model.testsuite.ResponseAssertedMessageExchange;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;

import java.io.PrintWriter;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * TestStepResult for a WsdlTestRequestStep
 *
 * @author ole.matzura
 */

public class WsdlTestRequestStepResult extends WsdlTestStepResult implements ResponseAssertedMessageExchange,
        AssertedXPathsContainer, MessageExchangeTestStepResult, WsdlMessageExchange {
    private SoftReference<String> softRequestContent;
    private SoftReference<WsdlResponse> softResponse;
    private String domain;
    private String username;
    private String endpoint;
    private String encoding;
    private String password;
    private StringToStringMap properties;
    private boolean addedAction;
    private List<AssertedXPath> assertedXPaths;
    private WsdlResponse response;
    private String requestContent;
    private WsdlOperation operation;

    public WsdlTestRequestStepResult(WsdlTestRequestStep step) {
        super(step);

        operation = step.getOperation();
    }

    public WsdlOperation getOperation() {
        return operation;
    }

    public SoapVersion getSoapVersion() {
        return ((WsdlOperation) getOperation()).getInterface().getSoapVersion();
    }

    public ModelItem getModelItem() {
        return getResponse() == null ? null : getResponse().getRequest();
    }

    public String getRequestContent() {
        if (isDiscarded()) {
            return "<discarded>";
        }

        return requestContent != null ? requestContent : softRequestContent == null ? null : softRequestContent.get();
    }

    public void setRequestContent(String requestContent, boolean useSoftReference) {
        if (useSoftReference) {
            this.softRequestContent = new SoftReference<String>(requestContent);
        } else {
            this.requestContent = requestContent;
        }
    }

    public WsdlResponse getResponse() {
        return response != null ? response : softResponse == null ? null : softResponse.get();
    }

    @Override
    public ActionList getActions() {
        if (!addedAction) {
            addAction(new ShowMessageExchangeAction(this, "TestStep"), true);
            addedAction = true;
        }

        return super.getActions();
    }

    public void setResponse(WsdlResponse response, boolean useSoftReference) {
        if (useSoftReference) {
            this.softResponse = new SoftReference<WsdlResponse>(response);
        } else {
            this.response = response;
        }
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
        addProperty("domain", domain);
    }

    private void addProperty(String key, String value) {
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

    public void discard() {
        super.discard();

        softRequestContent = null;
        softResponse = null;
        properties = null;
        assertedXPaths = null;
        response = null;
    }

    public void writeTo(PrintWriter writer) {
        super.writeTo(writer);
        writer.println("\r\n----------------- Properties ------------------------------");
        if (properties != null) {
            for (String key : properties.keySet()) {
                if (properties.get(key) != null) {
                    writer.println(key + ": " + properties.get(key));
                }
            }
        }

        writer.println("\r\n---------------- Request ---------------------------");
        WsdlResponse resp = getResponse();
        if (resp != null) {
            writer.println("Request Headers: " + resp.getRequestHeaders().toString() + "\r\n");
        }

        if (getRequestContent() != null) {
            writer.println(XmlUtils.prettyPrintXml(getRequestContent()));
        } else {
            writer.println("- missing request / garbage collected -");
        }

        writer.println("\r\n---------------- Response --------------------------");
        if (resp != null) {
            writer.println("Response Headers: " + resp.getResponseHeaders().toString() + "\r\n");

            String respContent = resp.getContentAsString();
            if (respContent != null) {
                writer.println(XmlUtils.prettyPrintXml(respContent));
            }
        } else {
            writer.println("- missing response / garbage collected -");
        }
    }

    public StringToStringMap getProperties() {
        return properties;
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    public Attachment[] getRequestAttachments() {
        if (getResponse() == null || getResponse().getRequest() == null) {
            return new Attachment[0];
        }

        return getResponse().getRequest().getAttachments();
    }

    public StringToStringsMap getRequestHeaders() {
        return getResponse() == null ? null : getResponse().getRequestHeaders();
    }

    public Attachment[] getResponseAttachments() {
        return getResponse() == null ? null : getResponse().getAttachments();
    }

    public String getResponseContent() {
        if (isDiscarded()) {
            return "<discarded>";
        }

        if (getResponse() == null) {
            return "<missing response>";
        }

        return getResponse().getContentAsString();
    }

    public String getRequestContentAsXml() {
        return XmlUtils.seemsToBeXml(getRequestContent()) ? getRequestContent() : "<not-xml/>";
    }

    public String getResponseContentAsXml() {
        String responseContent = getResponseContent();
        return XmlUtils.seemsToBeXml(responseContent) ? responseContent : null;
    }

    public StringToStringsMap getResponseHeaders() {
        return getResponse() == null ? new StringToStringsMap() : getResponse().getResponseHeaders();
    }

    public long getTimestamp() {
        WsdlResponse resp = getResponse();
        return resp == null ? 0 : resp.getTimestamp();
    }

    public AssertedXPath[] getAssertedXPathsForResponse() {
        return assertedXPaths == null ? new AssertedXPath[0] : assertedXPaths.toArray(new AssertedXPath[assertedXPaths
                .size()]);
    }

    public void addAssertedXPath(AssertedXPath assertedXPath) {
        if (assertedXPaths == null) {
            assertedXPaths = new ArrayList<AssertedXPath>();
        }

        assertedXPaths.add(assertedXPath);
    }

    public MessageExchange[] getMessageExchanges() {
        return new MessageExchange[]{this};
    }

    public byte[] getRawRequestData() {
        return getResponse() == null ? null : getResponse().getRawRequestData();
    }

    public byte[] getRawResponseData() {
        return getResponse() == null ? null : getResponse().getRawResponseData();
    }

    public Attachment[] getRequestAttachmentsForPart(String partName) {
        return null;
    }

    public Attachment[] getResponseAttachmentsForPart(String partName) {
        return null;
    }

    public boolean hasRawData() {
        return false;
    }

    public boolean hasRequest(boolean b) {
        return true;
    }

    public boolean hasResponse() {
        return getResponse() != null;
    }

    public Vector<?> getRequestWssResult() {
        return null;
    }

    public Vector<?> getResponseWssResult() {
        return getResponse() == null ? null : getResponse().getWssResult();
    }

    public int getResponseStatusCode() {
        WsdlResponse resp = getResponse();
        return resp == null ? 0 : resp.getStatusCode();
    }

    public String getResponseContentType() {
        return getResponse() == null ? null : getResponse().getContentType();
    }
}
