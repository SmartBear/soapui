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

package com.eviware.soapui.impl.wsdl.actions.iface;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.panels.mock.WsdlMockServiceDesktopPanel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormOptionsField;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

import java.util.List;

/**
 * Generates a MockService for a specified Interface
 *
 * @author ole.matzura
 */

public class GenerateMockServiceAction extends AbstractSoapUIAction<WsdlInterface> {
    private static final String CREATE_MOCKSUITE_OPTION = "<create>";

    public GenerateMockServiceAction() {
        super("Generate SOAP Mock Service", "Generates a SOAP mock service containing all Operations in this Interface");
    }

    public void perform(WsdlInterface iface, Object param) {
        generateMockService(iface, false);
    }

    public void generateMockService(WsdlInterface iface, boolean atCreation) {
        XFormDialog dialog = ADialogBuilder.buildDialog(Form.class);
        dialog.setBooleanValue(Form.ADD_ENDPOINT, true);
        String[] names = ModelSupport.getNames(iface.getOperationList());
        dialog.setOptions(Form.OPERATIONS, names);
        XFormOptionsField operationsFormField = (XFormOptionsField) dialog.getFormField(Form.OPERATIONS);
        operationsFormField.setSelectedOptions(names);

        dialog.getFormField(Form.START_MOCKSERVICE).setEnabled(!atCreation);

        WsdlProject project = iface.getProject();
        String[] mockServices = ModelSupport.getNames(new String[]{CREATE_MOCKSUITE_OPTION},
                project.getMockServiceList());
        dialog.setOptions(Form.MOCKSERVICE, mockServices);

        dialog.setValue(Form.PATH, "/mock" + iface.getName());
        dialog.setValue(Form.PORT, "8088");

        if (dialog.show()) {
            List<String> operations = StringUtils.toStringList(operationsFormField.getSelectedOptions());
            if (operations.size() == 0) {
                UISupport.showErrorMessage("No Operations selected..");
                return;
            }

            String mockServiceName = dialog.getValue(Form.MOCKSERVICE);
            MockService mockService = getMockService(iface, project, mockServiceName);
            if (mockService == null) {
                return;
            }

            mockService.setPath(dialog.getValue(Form.PATH));

            try {
                mockService.setPort(Integer.parseInt(dialog.getValue(Form.PORT)));
            } catch (NumberFormatException e1) {
            }

            for (int i = 0; i < iface.getOperationCount(); i++) {
                Operation operation = iface.getOperationAt(i);
                if (!operations.contains(operation.getName())) {
                    continue;
                }

                MockOperation mockOperation = mockService.addNewMockOperation(operation);
                if (mockOperation != null) {
                    mockOperation.addNewMockResponse("Response 1");
                }
            }

            if (dialog.getBooleanValue(Form.ADD_ENDPOINT)) {
                iface.addEndpoint(mockService.getLocalEndpoint());
            }

            if (!atCreation) {
                WsdlMockServiceDesktopPanel desktopPanel = (WsdlMockServiceDesktopPanel) UISupport
                        .showDesktopPanel(mockService);

                if (dialog.getBooleanValue(Form.START_MOCKSERVICE)) {
                    desktopPanel.startMockService();
                    SoapUI.getDesktop().minimize(desktopPanel);
                }
            }
        }
    }

    public MockService getMockService(AbstractInterface modelItem, WsdlProject project, String mockServiceName) {
        MockService mockService = project.getMockServiceByName(mockServiceName);

        if (mockService == null || mockServiceName.equals(CREATE_MOCKSUITE_OPTION)) {
            mockServiceName = UISupport.prompt("Specify name of MockService to create", getName(), modelItem.getName()
                    + " MockService");
            if (mockServiceName != null) {
                mockService = project.addNewMockService(mockServiceName);
            }
        }

        return mockService;
    }

    @AForm(name = "Generate MockService", description = "Set options for generated MockOperations for this Interface", helpUrl = HelpUrls.GENERATE_MOCKSERVICE_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    private interface Form {
        @AField(name = "MockService", description = "The MockService to create or use", type = AFieldType.ENUMERATION)
        public final static String MOCKSERVICE = "MockService";

        @AField(name = "Operations", description = "The Operations for which to Generate MockOperations", type = AFieldType.MULTILIST)
        public final static String OPERATIONS = "Operations";

        @AField(name = "Path", description = "The URL path to mount on", type = AFieldType.STRING)
        public final static String PATH = "Path";

        @AField(name = "Port", description = "The endpoint port to listen on", type = AFieldType.STRING)
        public final static String PORT = "Port";

        @AField(name = "Add Endpoint", description = "Adds the MockServices endpoint to the mocked Interface", type = AFieldType.BOOLEAN)
        public final static String ADD_ENDPOINT = "Add Endpoint";

        @AField(name = "Start MockService", description = "Starts the MockService immediately", type = AFieldType.BOOLEAN)
        public final static String START_MOCKSERVICE = "Start MockService";
    }
}
