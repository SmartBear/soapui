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

package com.eviware.soapui.impl.wsdl.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MockOperationConfig;
import com.eviware.soapui.config.MockOperationDocumentConfig;
import com.eviware.soapui.config.MockServiceConfig;
import com.eviware.soapui.config.TestCaseConfig;
import com.eviware.soapui.impl.support.AbstractMockService;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockDispatcher;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockRunListener;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.settings.SSLSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A MockService for simulation WsdlInterfaces and their operations
 *
 * @author ole.matzura
 */

public class WsdlMockService extends AbstractMockService<WsdlMockOperation, MockServiceConfig> {
    private static final String REQUIRE_SOAP_VERSION = WsdlMockService.class.getName() + "@require-soap-version";
    private static final String REQUIRE_SOAP_ACTION = WsdlMockService.class.getName() + "@require-soap-action";

    public static final String INCOMING_WSS = WsdlMockService.class.getName() + "@incoming-wss";
    public static final String OUTGOING_WSS = WsdlMockService.class.getName() + "@outgoing-wss";

    private WsdlMockOperation faultMockOperation;
    private String mockServiceEndpoint;
    public static final String ICON_NAME = "/soap_virt.png";
    public static final String STRING_ID = "MOCK";

    public String getStringID() {
        return STRING_ID;
    }

    public WsdlMockService(Project project, MockServiceConfig config) {
        super(config, project, ICON_NAME);

        List<MockOperationConfig> testStepConfigs = config.getMockOperationList();
        for (MockOperationConfig tsc : testStepConfigs) {
            WsdlMockOperation testStep = new WsdlMockOperation(this, tsc);
            addMockOperation(testStep);
        }

        if (!getSettings().isSet(REQUIRE_SOAP_ACTION)) {
            setRequireSoapAction(false);
        }

        for (MockRunListener listener : SoapUI.getListenerRegistry().getListeners(MockRunListener.class)) {
            addMockRunListener(listener);
        }

        if (!getConfig().isSetProperties()) {
            getConfig().addNewProperties();
        }

        setPropertiesConfig(getConfig().getProperties());

        if (getConfig().isSetFaultMockOperation()) {
            faultMockOperation = (WsdlMockOperation) getMockOperationByName(getConfig().getFaultMockOperation());
        }
    }


    public WsdlMockOperation getMockOperation(Operation operation) {
        for (int c = 0; c < getMockOperationCount(); c++) {
            WsdlMockOperation mockOperation = getMockOperationAt(c);
            if (mockOperation.getOperation() == operation) {
                return mockOperation;
            }
        }

        return null;
    }

    @Override
    public MockOperation addNewMockOperation(Operation operation) {
        if (getMockOperation(operation) != null) {
            return null;
        }

        MockOperationConfig config = getConfig().addNewMockOperation();
        config.setName(operation.getName());
        WsdlMockOperation mockOperation = new WsdlMockOperation(this, config, (WsdlOperation) operation);

        addMockOperation(mockOperation);
        fireMockOperationAdded(mockOperation);

        return mockOperation;
    }

    public void setPort(int port) {
        String oldEndpoint = getLocalEndpoint();

        int oldPort = getPort();
        if (port != oldPort) {
            super.setPort(port);

            for (WsdlInterface iface : getMockedInterfaces()) {
                if (Arrays.asList(iface.getEndpoints()).contains(oldEndpoint)) {
                    iface.changeEndpoint(oldEndpoint, getLocalEndpoint());
                }
            }
        }
    }

    public WsdlInterface[] getMockedInterfaces() {
        Set<WsdlInterface> result = new HashSet<WsdlInterface>();

        for (MockOperation mockOperation : getMockOperationList()) {
            WsdlOperation operation = (WsdlOperation) mockOperation.getOperation();
            if (operation != null) {
                result.add(operation.getInterface());
            }
        }

        return result.toArray(new WsdlInterface[result.size()]);
    }

    @Override
    public void release() {
        super.release();

        for (MockOperation operation : getMockOperationList()) {
            operation.release();
        }
    }

    @Override
    public String getIconName() {
        return ICON_NAME;
    }

    @Override
    public MockDispatcher createDispatcher(WsdlMockRunContext mockContext) {
        return new WsdlMockDispatcher(this, mockContext);
    }

    public void setPath(String path) {
        String oldEndpoint = getLocalEndpoint();

        String oldPath = getPath();
        if (!path.equals(oldPath)) {
            super.setPath(path);

            for (WsdlInterface iface : getMockedInterfaces()) {
                if (Arrays.asList(iface.getEndpoints()).contains(oldEndpoint)) {
                    iface.changeEndpoint(oldEndpoint, getLocalEndpoint());
                }
            }
        }
    }

    public WsdlMockOperation getFaultMockOperation() {
        return faultMockOperation;
    }

    public void setFaultMockOperation(WsdlMockOperation mockOperation) {
        faultMockOperation = mockOperation;
        if (faultMockOperation == null) {
            if (getConfig().isSetFaultMockOperation()) {
                getConfig().unsetFaultMockOperation();
            }
        } else {
            getConfig().setFaultMockOperation(faultMockOperation.getName());
        }
    }


    public String getHost() {
        return getConfig().getHost();
    }

    public boolean isRequireSoapVersion() {
        return getSettings().getBoolean(REQUIRE_SOAP_VERSION);
    }

    public void setRequireSoapVersion(boolean requireSoapVersion) {
        getSettings().setBoolean(REQUIRE_SOAP_VERSION, requireSoapVersion);
    }

    public boolean isRequireSoapAction() {
        return getSettings().getBoolean(REQUIRE_SOAP_ACTION);
    }

    public void setRequireSoapAction(boolean requireSoapAction) {
        getSettings().setBoolean(REQUIRE_SOAP_ACTION, requireSoapAction);
    }

    public boolean hasMockOperation(Operation operation) {
        return getMockOperation(operation) != null;
    }

    public List<? extends ModelItem> getChildren() {
        return mockOperations;
    }

    public String getIncomingWss() {
        return getConfig().getIncomingWss();
    }

    public void setIncomingWss(String incomingWss) {
        String old = getIncomingWss();
        getConfig().setIncomingWss(incomingWss);
        notifyPropertyChanged(INCOMING_WSS, old, incomingWss);
    }

    public String getOutgoingWss() {
        return getConfig().getOutgoingWss();
    }

    public void setOutgoingWss(String outgoingWss) {
        String old = getOutgoingWss();
        getConfig().setOutgoingWss(outgoingWss);
        notifyPropertyChanged(OUTGOING_WSS, old, outgoingWss);
    }

    public List<WsdlOperation> getMockedOperations() {
        List<WsdlOperation> result = new ArrayList<WsdlOperation>();

        for (MockOperation mockOperation : mockOperations) {
            result.add((WsdlOperation) mockOperation.getOperation());
        }


        return result;
    }

    public void replace(WsdlMockOperation mockOperation, MockOperationConfig reloadedMockOperation) {
        int ix = mockOperations.indexOf(mockOperation);
        if (ix == -1) {
            throw new RuntimeException("Unkonws MockOperation specified to removeMockOperation");
        }

        mockOperations.remove(ix);
        fireMockOperationRemoved(mockOperation);
        mockOperation.release();
        getConfig().removeMockOperation(ix);

        MockOperationConfig newConfig = (MockOperationConfig) getConfig().insertNewMockOperation(ix)
                .set(reloadedMockOperation).changeType(MockOperationConfig.type);
        WsdlMockOperation newOperation = new WsdlMockOperation(this, newConfig);
        mockOperations.add(ix, newOperation);
        newOperation.afterLoad();
        fireMockOperationAdded(newOperation);
    }

    public void export(File file) {
        try {
            this.getConfig().newCursor().save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void importMockOperation(File file) {
        MockOperationConfig mockOperationNewConfig = null;

        if (!file.exists()) {
            UISupport.showErrorMessage("Error loading mock operation.");
            return;
        }

        try {
            mockOperationNewConfig = MockOperationDocumentConfig.Factory.parse(file).getMockOperation();
        } catch (Exception e) {
            SoapUI.logError(e);
        }

        if (mockOperationNewConfig != null) {
            MockOperationConfig newConfig = (MockOperationConfig) getConfig().addNewMockOperation()
                    .set(mockOperationNewConfig).changeType(TestCaseConfig.type);
            WsdlMockOperation newMockOperation = new WsdlMockOperation(this, newConfig);
            ModelSupport.createNewIds(newMockOperation);
            newMockOperation.afterLoad();
            mockOperations.add(newMockOperation);
            fireMockOperationAdded(newMockOperation);

            resolveImportedMockOperation(newMockOperation);

        } else {
            UISupport.showErrorMessage("Not valid mock operation xml");
        }
    }

    private void resolveImportedMockOperation(WsdlMockOperation mockOperation) {
        ResolveDialog resolver = new ResolveDialog("Validate MockOperation", "Checks MockOperation for inconsistencies",
                null);
        resolver.setShowOkMessage(false);
        resolver.resolve(mockOperation);
    }

    public String toString() {
        return getName();
    }

    public String getMockServiceEndpoint() {
        return mockServiceEndpoint;
    }

    public void setMockServiceEndpoint(String mockServiceEndpoint) {
        this.mockServiceEndpoint = mockServiceEndpoint;
    }

    public String getLocalMockServiceEndpoint() {
        if (mockServiceEndpoint != null) {
            return mockServiceEndpoint + getPath();
        }

        String host = getHost();
        if (StringUtils.isNullOrEmpty(host)) {
            host = "127.0.0.1";
        }

        int port = (int) (getSettings().getBoolean(SSLSettings.ENABLE_MOCK_SSL) ? getSettings().getLong(
                SSLSettings.MOCK_PORT, 443) : getPort());

        return getProtocol() + host + ":" + port + getPath();
    }

    public boolean canIAddAMockOperation(WsdlMockOperation mockOperation) {
        return this.getConfig().getMockOperationList().contains(mockOperation.getConfig());
    }

    @Override
    public String getHelpUrl() {
        return HelpUrls.MOCKSERVICE_HELP_URL;
    }
}
