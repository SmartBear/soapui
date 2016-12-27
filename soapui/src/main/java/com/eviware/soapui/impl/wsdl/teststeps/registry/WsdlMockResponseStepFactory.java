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

package com.eviware.soapui.impl.wsdl.teststeps.registry;

import com.eviware.soapui.config.MockResponseConfig;
import com.eviware.soapui.config.MockResponseStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.CompressedStringSupport;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.util.ModelItemNames;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creation TransferValue steps
 *
 * @author Ole.Matzura
 */
public class WsdlMockResponseStepFactory extends WsdlTestStepFactory {
    public static final String MOCKRESPONSE_TYPE = "mockresponse";
    private static XFormDialog dialog;
    private static WsdlProject project;

    public WsdlMockResponseStepFactory() {
        super(MOCKRESPONSE_TYPE, "SOAP Mock Response", "Waits for a request and returns the specified response",
                "/mockResponseStep.gif");
    }

    public WsdlTestStep buildTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {
        return new WsdlMockResponseTestStep(testCase, config, forLoadTest);
    }

    public TestStepConfig createNewTestStep(WsdlTestCase testCase, String name) {
        ensureDialog();

        return createFromDialog(testCase.getTestSuite().getProject(), name);
    }

    private static void ensureDialog() {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(CreateForm.class);
            dialog.getFormField(CreateForm.INTERFACE).addFormFieldListener(new XFormFieldListener() {

                public void valueChanged(XFormField sourceField, String newValue, String oldValue) {
                    WsdlInterface iface = (WsdlInterface) project.getInterfaceByName(newValue);
                    dialog.setOptions(CreateForm.OPERATION,
                            new ModelItemNames<Operation>(iface.getOperationList()).getNames());
                }
            });

            dialog.setBooleanValue(CreateForm.CREATE_RESPONSE, true);
            dialog.setValue(CreateForm.PATH, "/");
        }
    }

    private static TestStepConfig createFromDialog(WsdlProject project, String name) {
        WsdlMockResponseStepFactory.project = project;

        try {
            List<Interface> interfaces = new ArrayList<Interface>();
            for (Interface iface : project.getInterfaces(WsdlInterfaceFactory.WSDL_TYPE)) {
                if (iface.getOperationCount() > 0) {
                    interfaces.add(iface);
                }
            }

            if (interfaces.isEmpty()) {
                UISupport.showErrorMessage("Missing Interfaces/Operations to mock");
                return null;
            }

            dialog.setValue(CreateForm.NAME, name);
            dialog.setOptions(CreateForm.INTERFACE, new ModelItemNames<Interface>(interfaces).getNames());
            dialog.setOptions(CreateForm.OPERATION,
                    new ModelItemNames<Operation>(interfaces.get(0).getOperationList()).getNames());

            if (!dialog.show()) {
                return null;
            }

            TestStepConfig testStepConfig = TestStepConfig.Factory.newInstance();
            testStepConfig.setType(MOCKRESPONSE_TYPE);
            testStepConfig.setName(dialog.getValue(CreateForm.NAME));

            MockResponseStepConfig config = MockResponseStepConfig.Factory.newInstance();
            config.setInterface(dialog.getValue(CreateForm.INTERFACE));
            config.setOperation(dialog.getValue(CreateForm.OPERATION));
            config.setPort(dialog.getIntValue(CreateForm.PORT, 8080));
            config.setPath(dialog.getValue(CreateForm.PATH));
            config.addNewResponse();
            config.getResponse().addNewResponseContent();

            if (dialog.getBooleanValue(CreateForm.CREATE_RESPONSE)) {
                WsdlInterface iface = (WsdlInterface) project.getInterfaceByName(config.getInterface());
                String response = iface.getOperationByName(config.getOperation()).createResponse(
                        project.getSettings().getBoolean(WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS));

                CompressedStringSupport.setString(config.getResponse().getResponseContent(), response);
            }

            testStepConfig.addNewConfig().set(config);
            return testStepConfig;
        } finally {
            WsdlMockResponseStepFactory.project = null;
        }
    }

    @AForm(description = "Specify options for new MockResponse step", name = "New MockResponse Step", helpUrl = HelpUrls.CREATEMOCKRESPONSESTEP_HELP_URL, icon = UISupport.OPTIONS_ICON_PATH)
    private class CreateForm {
        @AField(description = "The name of the MockResponse step", name = "Name", type = AFieldType.STRING)
        public static final String NAME = "Name";

        @AField(description = "Specifies the operation to be mocked", name = "Operation", type = AFieldType.ENUMERATION)
        public final static String OPERATION = "Operation";

        @AField(description = "Specifies the interface containing the operation to be mocked", name = "Interface", type = AFieldType.ENUMERATION)
        public final static String INTERFACE = "Interface";

        @AField(description = "Specifies if a mock response is to be created from the schema", name = "Create Response", type = AFieldType.BOOLEAN)
        public final static String CREATE_RESPONSE = "Create Response";

        @AField(description = "Specifies the port to listen on", name = "Port", type = AFieldType.INT)
        public final static String PORT = "Port";

        @AField(description = "Specifies the path to listen on", name = "Path")
        public final static String PATH = "Path";
    }

    public static TestStepConfig createConfig(WsdlOperation operation, boolean interactive) {
        return createConfig(operation, null, interactive);
    }

    public static TestStepConfig createConfig(WsdlRequest request, boolean interactive) {
        return createConfig(request.getOperation(), request, interactive);
    }

    public static TestStepConfig createConfig(WsdlOperation operation, WsdlRequest request, boolean interactive) {
        if (interactive) {
            ensureDialog();

            dialog.setValue(CreateForm.INTERFACE, operation.getInterface().getName());
            dialog.setValue(CreateForm.OPERATION, operation.getName());
            dialog.setBooleanValue(CreateForm.CREATE_RESPONSE, request.getResponse() == null);

            return createFromDialog(operation.getInterface().getProject(), request.getName() + " Response");
        } else {
            TestStepConfig testStepConfig = TestStepConfig.Factory.newInstance();
            testStepConfig.setType(MOCKRESPONSE_TYPE);
            testStepConfig.setName("Mock Response");

            MockResponseStepConfig config = MockResponseStepConfig.Factory.newInstance();
            config.setInterface(operation.getInterface().getName());
            config.setOperation(operation.getName());
            MockResponseConfig response = config.addNewResponse();
            response.addNewResponseContent();

            if (request != null && request.getResponse() != null) {
                CompressedStringSupport.setString(response.getResponseContent(), request.getResponse()
                        .getContentAsString());
            }

            testStepConfig.addNewConfig().set(config);

            return testStepConfig;
        }
    }

    public static TestStepConfig createNewTestStep(WsdlMockResponse mockResponse) {
        WsdlOperation operation = mockResponse.getMockOperation().getOperation();
        if (operation == null) {
            UISupport.showErrorMessage("Missing operation for this mock response");
            return null;
        }

        ensureDialog();

        dialog.setValue(CreateForm.INTERFACE, operation.getInterface().getName());
        dialog.setValue(CreateForm.OPERATION, operation.getName());
        dialog.setBooleanValue(CreateForm.CREATE_RESPONSE, false);
        dialog.setIntValue(CreateForm.PORT, mockResponse.getMockOperation().getMockService().getPort());
        dialog.setValue(CreateForm.PATH, mockResponse.getMockOperation().getMockService().getPath());

        return createFromDialog(operation.getInterface().getProject(), mockResponse.getMockOperation().getName() + " - "
                + mockResponse.getName());
    }

    public boolean canCreate() {
        return true;
    }

    @Override
    public boolean canAddTestStepToTestCase(WsdlTestCase testCase) {
        for (Interface iface : testCase.getTestSuite().getProject().getInterfaceList()) {
            if (iface instanceof WsdlInterface && iface.getOperationCount() > 0) {

                return true;
            }
        }

        UISupport.showErrorMessage("Missing SOAP Operations to Mock in Project");
        return false;

    }
}
