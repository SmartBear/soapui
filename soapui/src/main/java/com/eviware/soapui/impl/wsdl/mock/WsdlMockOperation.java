/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.impl.wsdl.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MockOperationConfig;
import com.eviware.soapui.config.MockResponseConfig;
import com.eviware.soapui.impl.support.AbstractMockOperation;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.dispatch.MockOperationDispatcher;
import com.eviware.soapui.impl.wsdl.support.CompressedStringSupport;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.InterfaceListener;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.project.ProjectListener;
import com.eviware.soapui.model.support.InterfaceListenerAdapter;
import com.eviware.soapui.model.support.ProjectListenerAdapter;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.UISupport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ImageIcon;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A WsdlMockOperation in a WsdlMockService
 *
 * @author ole.matzura
 */

public class WsdlMockOperation extends AbstractMockOperation<MockOperationConfig, WsdlMockResponse> {
    @SuppressWarnings("unused")
    private final static Logger log = LogManager.getLogger(WsdlMockOperation.class);

    public final static String OPERATION_PROPERTY = WsdlMockOperation.class.getName() + "@operation";
    public static final String ICON_NAME = "/mockOperation.gif";

    private WsdlOperation operation;
    private InterfaceListener interfaceListener = new InternalInterfaceListener();
    private ProjectListener projectListener = new InternalProjectListener();
    private ImageIcon oneWayIcon;
    private ImageIcon notificationIcon;
    private ImageIcon solicitResponseIcon;

    public WsdlMockOperation(WsdlMockService mockService, MockOperationConfig config) {
        super(config, mockService, ICON_NAME);

        Interface iface = mockService.getProject().getInterfaceByName(config.getInterface());
        if (iface == null) {
            SoapUI.log.warn("Missing interface [" + config.getInterface() + "] for MockOperation in project");
        } else {
            operation = (WsdlOperation) iface.getOperationByName(config.getOperation());
        }

        List<MockResponseConfig> responseConfigs = config.getResponseList();
        for (MockResponseConfig responseConfig : responseConfigs) {
            WsdlMockResponse wsdlMockResponse = new WsdlMockResponse(this, responseConfig);
            wsdlMockResponse.addPropertyChangeListener(this);
            super.addMockResponse(wsdlMockResponse);
        }

        setupConfig(config);
    }

    public WsdlMockOperation(WsdlMockService mockService, MockOperationConfig config, WsdlOperation operation) {
        super(config, mockService, ICON_NAME);
        this.operation = operation;

        config.setInterface(operation.getInterface().getName());
        config.setOperation(operation.getName());

        setupConfig(config);
    }

    protected void setupConfig(MockOperationConfig config) {
        if (!getConfig().isSetDispatchConfig()) {
            getConfig().addNewDispatchConfig();
        }

        super.setupConfig(config);

        createIcons();
        addListeners();
    }

    private void addListeners() {
        Operation operation = getOperation();
        if (operation != null) {
            operation.getInterface().getProject().addProjectListener(projectListener);
            operation.getInterface().addInterfaceListener(interfaceListener);
            operation.getInterface().addPropertyChangeListener(WsdlInterface.NAME_PROPERTY, this);
        }
    }

    private void createIcons() {
        oneWayIcon = UISupport.createImageIcon("/onewaymockoperation.gif");
        notificationIcon = UISupport.createImageIcon("/mocknotificationoperation.gif");
        solicitResponseIcon = UISupport.createImageIcon("/mocksolicitresponseoperation.gif");
    }

    @Override
    public ImageIcon getIcon() {
        if (operation != null) {
            if (isOneWay()) {
                return oneWayIcon;
            } else if (isNotification()) {
                return notificationIcon;
            } else if (isSolicitResponse()) {
                return solicitResponseIcon;
            }
        }

        return super.getIcon();
    }

    public WsdlMockService getMockService() {
        return (WsdlMockService) getParent();
    }

    public WsdlOperation getOperation() {
        return operation;
    }

    @Override
    public String getScriptHelpUrl() {
        return HelpUrls.MOCKOPERATION_SCRIPTDISPATCH_HELP_URL;
    }

    @Override
    public MockResponse addNewMockResponse(String name) {
        return this.addNewMockResponse(name, true);
    }

    public WsdlMockResponse addNewMockResponse(MockResponseConfig responseConfig) {
        WsdlMockResponse mockResponse = new WsdlMockResponse(this, responseConfig);

        super.addMockResponse(mockResponse);
        if (getMockResponseCount() == 1) {
            setDefaultResponse(mockResponse.getName());
        }

        // add ws-a action
        WsdlUtils.setDefaultWsaAction(mockResponse.getWsaConfig(), true);

        getMockService().fireMockResponseAdded(mockResponse);
        notifyPropertyChanged("mockResponses", null, mockResponse);

        return mockResponse;
    }

    public WsdlMockResponse addNewMockResponse(String name, boolean createResponse) {
        MockResponseConfig responseConfig = getConfig().addNewResponse();
        responseConfig.setName(name);
        responseConfig.addNewResponseContent();

        if (createResponse && getOperation() != null && getOperation().isBidirectional()) {
            boolean createOptional = SoapUI.getSettings().getBoolean(
                    WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS);
            CompressedStringSupport.setString(responseConfig.getResponseContent(),
                    getOperation().createResponse(createOptional));
        }

        return addNewMockResponse(responseConfig);
    }

    public WsdlMockResult dispatchRequest(WsdlMockRequest request) throws DispatchException {
        try {

            request.setOperation(getOperation());
            WsdlMockResult result = new WsdlMockResult(request);

            if (getMockResponseCount() == 0) {
                throw new DispatchException("Missing MockResponse(s) in MockOperation [" + getName() + "]");
            }

            result.setMockOperation(this);
            WsdlMockResponse response = (WsdlMockResponse) getDispatcher().selectMockResponse(request, result);
            if (response == null) {
                response = getMockResponseByName(getDefaultResponse());
            }

            if (response == null) {
                throw new DispatchException("Failed to find MockResponse");
            }

            result.setMockResponse(response);
            response.execute(request, result);

            return result;
        } catch (Throwable e) {
            if (e instanceof DispatchException) {
                throw (DispatchException) e;
            } else {
                throw new DispatchException(e);
            }
        }
    }

    @Override
    public MockOperationDispatcher setDispatchStyle(String dispatchStyle) {
        if (!getConfig().isSetDispatchConfig()) {
            getConfig().addNewDispatchConfig();
        }

        return super.setDispatchStyle(dispatchStyle);
    }

    @Override
    public void release() {
        super.release();

        if (getDispatcher() != null) {
            getDispatcher().release();
        }

        for (MockResponse response : getMockResponses()) {
            response.removePropertyChangeListener(this);
            response.release();
        }

        if (operation != null) {
            operation.getInterface().getProject().removeProjectListener(projectListener);
            operation.getInterface().removeInterfaceListener(interfaceListener);
            operation.getInterface().removePropertyChangeListener(WsdlInterface.NAME_PROPERTY, this);
        }
    }

    // this may seem to be unused but is actually used in the MockOperation Properties view - don't remove it
    public String getWsdlOperationName() {
        return operation.getName();
    }

    public void propertyChange(PropertyChangeEvent arg0) {
        if (arg0.getPropertyName().equals(WsdlMockResponse.NAME_PROPERTY)) {
            if (arg0.getOldValue().equals(getDefaultResponse())) {
                setDefaultResponse(arg0.getNewValue().toString());
            }
        } else if (arg0.getPropertyName().equals(WsdlInterface.NAME_PROPERTY)) {
            getConfig().setInterface(arg0.getNewValue().toString());
        }
    }

    public void setOperation(WsdlOperation operation) {
        WsdlOperation oldOperation = getOperation();

        if (operation == null) {
            getConfig().unsetInterface();
            getConfig().unsetOperation();
        } else {
            getConfig().setInterface(operation.getInterface().getName());
            getConfig().setOperation(operation.getName());
        }

        this.operation = operation;

        notifyPropertyChanged(OPERATION_PROPERTY, oldOperation, operation);
    }

    @Override
    public void removeResponseFromConfig(int index) {
        getConfig().removeResponse(index);
    }

    private class InternalInterfaceListener extends InterfaceListenerAdapter {
        @Override
        public void operationUpdated(Operation operation) {
            // such wow - works? equals?
            if (operation == WsdlMockOperation.this.operation) {
                getConfig().setOperation(operation.getName());
            }
        }

        @Override
        public void operationRemoved(Operation operation) {
            // such wow - works? equals?
            if (operation == WsdlMockOperation.this.operation) {
                getMockService().removeMockOperation(WsdlMockOperation.this);
            }
        }
    }

    private class InternalProjectListener extends ProjectListenerAdapter {
        @Override
        public void interfaceRemoved(Interface iface) {
            if (operation.getInterface() == iface) {
                getMockService().removeMockOperation(WsdlMockOperation.this);
            }
        }

        @Override
        public void interfaceUpdated(Interface iface) {
            if (operation.getInterface() == iface) {
                getConfig().setInterface(iface.getName());
            }
        }
    }

    public boolean isOneWay() {
        return operation == null ? false : operation.isOneWay();
    }

    public boolean isNotification() {
        return operation == null ? false : operation.isNotification();
    }

    public boolean isSolicitResponse() {
        return operation == null ? false : operation.isSolicitResponse();
    }

    public boolean isUnidirectional() {
        return operation == null ? false : operation.isUnidirectional();
    }

    public boolean isBidirectional() {
        return !isUnidirectional();
    }

    public List<? extends ModelItem> getChildren() {
        return getMockResponses();
    }

    public void exportMockOperation(File file) {
        try {
            this.getConfig().newCursor().save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
