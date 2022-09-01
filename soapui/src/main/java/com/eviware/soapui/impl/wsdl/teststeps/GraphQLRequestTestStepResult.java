package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.impl.wsdl.WsdlSubmit;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
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

public class GraphQLRequestTestStepResult extends WsdlTestStepResult implements ResponseAssertedMessageExchange, MessageExchangeTestStepResult {
    private String requestContent;
    private HttpResponse response;
    private StringToStringMap properties;
    private boolean addedAction;
    private String endpoint;
    private String encoding;
    private WsdlSubmit submit;

    public GraphQLRequestTestStepResult(HttpTestRequestStepInterface step) {
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

    public void setSubmit(WsdlSubmit submit) {
        this.submit = submit;
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

    @Override
    public void discard() {
        super.discard();

        requestContent = null;
        response = null;
        properties = null;
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
        return new Attachment[0];
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
    public MessageExchange[] getMessageExchanges() {
        return new MessageExchange[]{this};
    }

    @Override
    public byte[] getRawRequestData() {
        if (response == null && submit != null && submit.getResponse() != null) {
            return submit.getResponse().getRawRequestData();
        } else if (response != null) {
            return response.getRawRequestData();
        }
        return null;
    }

    @Override
    public byte[] getRawResponseData() {
        if (response != null) {
            return response.getRawResponseData();
        }
        return null;
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

    @Override
    public AssertedXPath[] getAssertedXPathsForResponse() {
        return new AssertedXPath[0];
    }
}
