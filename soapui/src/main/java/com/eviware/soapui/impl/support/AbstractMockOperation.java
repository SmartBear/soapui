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

import com.eviware.soapui.config.BaseMockOperationConfig;
import com.eviware.soapui.config.MockOperationDispatchStyleConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.mock.dispatch.MockOperationDispatchRegistry;
import com.eviware.soapui.impl.wsdl.mock.dispatch.MockOperationDispatcher;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockService;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMockOperation
        <BaseMockOperationConfigType extends BaseMockOperationConfig, MockResponseType extends MockResponse>
        extends AbstractWsdlModelItem<BaseMockOperationConfigType>
        implements MockOperation, PropertyChangeListener {
    public final static String DISPATCH_PATH_PROPERTY = MockOperation.class.getName() + "@dispatchpath";
    public final static String DISPATCH_STYLE_PROPERTY = MockOperation.class.getName() + "@dispatchstyle";

    private MockOperationDispatcher dispatcher;

    private List<MockResponseType> responses = new ArrayList<MockResponseType>();

    protected AbstractMockOperation(BaseMockOperationConfigType config, MockService parent, String icon) {
        super(config, parent, icon);
    }

    protected void setupConfig(BaseMockOperationConfigType config) {
        Operation operation = getOperation();
        if (!config.isSetName()) {
            config.setName(operation == null ? "<missing operation>" : operation.getName());
        }

        if (!config.isSetDefaultResponse() && getMockResponseCount() > 0) {
            setDefaultResponse(getMockResponseAt(0).getName());
        }

        if (!config.isSetDispatchStyle()) {
            config.setDispatchStyle(MockOperationDispatchStyleConfig.SEQUENCE);
            setExampleScript();

        }

        dispatcher = MockOperationDispatchRegistry.buildDispatcher(getConfig().getDispatchStyle().toString(), this);
    }

    public void addMockResponse(MockResponseType response) {
        responses.add(response);
    }

    public List<MockResponse> getMockResponses() {
        return new ArrayList<MockResponse>(responses);
    }

    public MockResponseType getMockResponseAt(int index) {
        return responses.get(index);
    }

    public int getMockResponseCount() {
        return responses.size();
    }

    public MockResponseType getMockResponseByName(String name) {
        return (MockResponseType) getWsdlModelItemByName(getMockResponses(), name);
    }

    public MockResult getLastMockResult() {
        MockResult result = null;

        for (MockResponse response : getMockResponses()) {
            MockResult mockResult = response.getMockResult();
            if (mockResult != null) {
                if (result == null || result.getTimestamp() > mockResult.getTimestamp()) {
                    result = mockResult;
                }
            }
        }

        return result;
    }

    public void removeMockResponse(MockResponse mockResponse) {
        int ix = responses.indexOf(mockResponse);
        responses.remove(ix);
        mockResponse.removePropertyChangeListener(this);

        try {
            (getMockService()).fireMockResponseRemoved(mockResponse);
        } finally {
            mockResponse.release();
            removeResponseFromConfig(ix);
        }
    }

    public abstract void removeResponseFromConfig(int index);

    public String getDefaultResponse() {
        return getConfig().getDefaultResponse();
    }

    public void setDefaultResponse(String defaultResponse) {
        String old = getDefaultResponse();
        getConfig().setDefaultResponse(defaultResponse);
        // noone is listening? notifyPropertyChanged( WsdlMockOperation.DEFAULT_RESPONSE_PROPERTY, old, defaultResponse );
    }

    @Override
    public String getScript() {
        return getConfig().getDispatchPath();
    }

    @Override
    public void setScript(String dispatchPath) {
        String old = getScript();
        getConfig().setDispatchPath(dispatchPath);
        notifyPropertyChanged(DISPATCH_PATH_PROPERTY, old, dispatchPath);
    }

    // Hook for subclasses
    public void setExampleScript() {
    }

    public MockOperationDispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(MockOperationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public String getDispatchStyle() {
        return String.valueOf(getConfig().isSetDispatchStyle() ? getConfig().getDispatchStyle()
                : MockOperationDispatchStyleConfig.SEQUENCE);
    }

    public MockOperationDispatcher setDispatchStyle(String dispatchStyle) {
        String old = getDispatchStyle();
        MockOperationDispatcher dispatcher = getDispatcher();
        if (dispatcher != null && dispatchStyle.equals(old)) {
            return dispatcher;
        }

        getConfig().setDispatchStyle(MockOperationDispatchStyleConfig.Enum.forString(dispatchStyle));

        if (dispatcher != null) {
            dispatcher.release();
        }

        setDispatcher(MockOperationDispatchRegistry.buildDispatcher(dispatchStyle, this));

        notifyPropertyChanged(DISPATCH_STYLE_PROPERTY, old, dispatchStyle);

        return dispatcher;
    }

}
