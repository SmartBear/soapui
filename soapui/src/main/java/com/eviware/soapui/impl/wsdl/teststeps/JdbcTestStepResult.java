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

import com.eviware.soapui.impl.wsdl.panels.teststeps.JdbcResponse;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathsContainer;
import com.eviware.soapui.impl.wsdl.teststeps.actions.ShowMessageExchangeAction;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.testsuite.AssertedXPath;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.types.StringToStringsMap;

import java.lang.ref.SoftReference;

public class JdbcTestStepResult extends WsdlTestStepResult implements AssertedXPathsContainer, MessageExchange {
    private JdbcResponse response;
    private SoftReference<JdbcResponse> softResponse;
    private String requestContent;
    private boolean addedAction;

    public JdbcTestStepResult(WsdlTestStep testStep) {
        super(testStep);
    }

    public void setResponse(JdbcResponse response, boolean useSoftReference) {
        if (useSoftReference) {
            this.softResponse = new SoftReference<JdbcResponse>(response);
        } else {
            this.response = response;
        }
    }

    public void setRequestContent(String requestContent) {
        this.requestContent = requestContent;
    }

    public void addAssertedXPath(AssertedXPath assertedXPath) {
    }

    @Override
    public ActionList getActions() {
        if (!addedAction) {
            addAction(new ShowMessageExchangeAction(this, "TestStep"), true);
            addedAction = true;
        }

        return super.getActions();
    }

    public ModelItem getModelItem() {
        return getTestStep();
    }

    public Operation getOperation() {
        return null;
    }

    public StringToStringMap getProperties() {
        return new StringToStringMap();
    }

    public String getProperty(String name) {
        return null;
    }

    public byte[] getRawRequestData() {
        return hasResponse() ? getResponse().getRawRequestData() : null;
    }

    public byte[] getRawResponseData() {
        return getResponseContent().getBytes();
    }

    public Attachment[] getRequestAttachments() {
        return new Attachment[0];
    }

    public Attachment[] getRequestAttachmentsForPart(String partName) {
        return new Attachment[0];
    }

    public String getRequestContent() {
        return requestContent != null ? requestContent : hasResponse() ? getResponse().getRequestContent() : null;
    }

    public JdbcResponse getResponse() {
        return softResponse != null ? softResponse.get() : response;
    }

    public String getRequestContentAsXml() {
        return null;
    }

    public StringToStringsMap getRequestHeaders() {
        return new StringToStringsMap();
    }

    public Attachment[] getResponseAttachments() {
        return new Attachment[0];
    }

    public Attachment[] getResponseAttachmentsForPart(String partName) {
        return new Attachment[0];
    }

    public String getResponseContent() {
        return hasResponse() ? getResponse().getContentAsString() : null;
    }

    public String getResponseContentAsXml() {
        return getResponseContent();
    }

    public StringToStringsMap getResponseHeaders() {
        return new StringToStringsMap();
    }

    public long getTimestamp() {
        return hasResponse() ? getResponse().getTimestamp() : -1;
    }

    public boolean hasRawData() {
        return true;
    }

    public boolean hasRequest(boolean ignoreEmpty) {
        return hasResponse();
    }

    public boolean hasResponse() {
        return getResponse() != null;
    }

    public String getEndpoint() {
        // TODO Auto-generated method stub
        return null;
    }

}
