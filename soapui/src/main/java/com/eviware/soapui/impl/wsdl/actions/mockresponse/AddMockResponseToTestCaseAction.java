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

package com.eviware.soapui.impl.wsdl.actions.mockresponse;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.MockResponseStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.actions.support.AbstractAddToTestCaseAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SchemaComplianceAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlMockResponseStepFactory;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.ui.desktop.SoapUIDesktop;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

public class AddMockResponseToTestCaseAction extends AbstractAddToTestCaseAction<WsdlMockResponse> {
    public final static String SOAPUI_ACTION_ID = "AddMockResponseToTestCaseAction";
    private XFormDialog dialog;

    public AddMockResponseToTestCaseAction() {
        super("Add to TestCase", "Adds this MockResponse to a TestCase");
    }

    public void perform(WsdlMockResponse mockResponse, Object param) {
        WsdlMockService mockService = mockResponse.getMockOperation().getMockService();
        WsdlTestCase testCase = getTargetTestCase(mockService.getProject());
        if (testCase == null) {
            return;
        }

        addMockResponseToTestCase(mockResponse, testCase, -1);
    }

    public void addMockResponseToTestCase(WsdlMockResponse mockResponse, WsdlTestCase testCase, int index) {
        if (mockResponse.getMockOperation().getOperation() == null) {
            UISupport.showErrorMessage("Missing operation for this mock response");
            return;
        }

        WsdlMockService mockService = mockResponse.getMockOperation().getMockService();

        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
        }

        dialog.setValue(Form.STEP_NAME, mockResponse.getMockOperation().getName());
        dialog.setBooleanValue(Form.CLOSE_EDITOR, true);
        dialog.setBooleanValue(Form.SHOW_TESTCASE, true);
        dialog.setIntValue(Form.PORT, mockService.getPort());
        dialog.setValue(Form.PATH, mockService.getPath());

        SoapUIDesktop desktop = SoapUI.getDesktop();
        dialog.getFormField(Form.CLOSE_EDITOR).setEnabled(desktop != null && desktop.hasDesktopPanel(mockResponse));

        if (!dialog.show()) {
            return;
        }

        TestStepConfig config = WsdlMockResponseStepFactory.createConfig(mockResponse.getMockOperation().getOperation(),
                false);
        MockResponseStepConfig mockResponseStepConfig = ((MockResponseStepConfig) config.getConfig());

        config.setName(dialog.getValue(Form.STEP_NAME));
        mockResponseStepConfig.setPath(dialog.getValue(Form.PATH));
        mockResponseStepConfig.setPort(dialog.getIntValue(Form.PORT, mockService.getPort()));

        mockResponse.beforeSave();
        mockResponseStepConfig.getResponse().set(mockResponse.getConfig());

        WsdlMockResponseTestStep testStep = (WsdlMockResponseTestStep) testCase.insertTestStep(config, -1);
        if (testStep == null) {
            return;
        }

        if (dialog.getBooleanValue(Form.ADD_SCHEMA_ASSERTION)) {
            testStep.addAssertion(SchemaComplianceAssertion.ID);
        }

        UISupport.selectAndShow(testStep);

        if (dialog.getBooleanValue(Form.CLOSE_EDITOR) && desktop != null) {
            desktop.closeDesktopPanel(mockResponse);
        }

        if (dialog.getBooleanValue(Form.SHOW_TESTCASE)) {
            UISupport.selectAndShow(testCase);
        }
    }

    @AForm(name = "Add MockResponse to TestCase", description = "Options for adding this MockResponse to a "
            + "TestCase", helpUrl = HelpUrls.ADDMOCKRESPONSETOTESTCASE_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    private interface Form {
        @AField(name = "Name", description = "Unique name of MockResponse Step")
        public final static String STEP_NAME = "Name";

        @AField(name = "Path", description = "Path to listen on")
        public final static String PATH = "Path";

        @AField(name = "Port", description = "Port to listen on", type = AFieldType.INT)
        public final static String PORT = "Port";

        @AField(name = "Add Schema Assertion", description = "Adds SchemaCompliance Assertion for request", type = AFieldType.BOOLEAN)
        public final static String ADD_SCHEMA_ASSERTION = "Add Schema Assertion";

        @AField(name = "Close MockResponse Window", description = "Closes the MockResponse editor if visible", type = AFieldType.BOOLEAN)
        public final static String CLOSE_EDITOR = "Close MockResponse Window";

        @AField(name = "Shows TestCase Editor", description = "Shows the target steps TestCase editor", type = AFieldType.BOOLEAN)
        public final static String SHOW_TESTCASE = "Shows TestCase Editor";
    }
}
