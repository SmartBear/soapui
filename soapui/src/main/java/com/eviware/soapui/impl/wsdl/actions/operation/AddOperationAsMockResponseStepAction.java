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

package com.eviware.soapui.impl.wsdl.actions.operation;

import com.eviware.soapui.config.MockResponseStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.actions.support.AbstractAddToTestCaseAction;
import com.eviware.soapui.impl.wsdl.support.CompressedStringSupport;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SchemaComplianceAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlMockResponseStepFactory;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

public class AddOperationAsMockResponseStepAction extends AbstractAddToTestCaseAction<WsdlOperation> {
    private XFormDialog dialog;

    public AddOperationAsMockResponseStepAction() {
        super("Add as MockResponse Step", "Creates a MockResponseStep for this Operation");
    }

    public void perform(WsdlOperation operation, Object param) {
        WsdlTestCase testCase = getTargetTestCase(operation.getInterface().getProject());
        if (testCase != null) {
            addMockResponse(testCase, operation);
        }
    }

    protected boolean addMockResponse(WsdlTestCase testCase, WsdlOperation operation) {
        if (dialog == null) {
            dialog = ADialogBuilder.buildDialog(Form.class);
            dialog.setWidth(450);
        }

        dialog.setValue(Form.STEP_NAME, operation.getName());
        dialog.setBooleanValue(Form.SHOW_TESTCASE, true);
        dialog.setIntValue(Form.PORT, 8181);
        dialog.setValue(Form.PATH, "/" + operation.getName());

        if (!dialog.show()) {
            return false;
        }

        TestStepConfig config = WsdlMockResponseStepFactory.createConfig(operation, false);
        MockResponseStepConfig mockResponseStepConfig = ((MockResponseStepConfig) config.getConfig());

        config.setName(dialog.getValue(Form.STEP_NAME));
        mockResponseStepConfig.setPath(dialog.getValue(Form.PATH));
        mockResponseStepConfig.setPort(dialog.getIntValue(Form.PORT, 8181));

        String response = operation.createResponse(operation.getSettings().getBoolean(
                WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS));
        CompressedStringSupport.setString(mockResponseStepConfig.addNewResponse().addNewResponseContent(), response);

        WsdlMockResponseTestStep testStep = (WsdlMockResponseTestStep) testCase.addTestStep(config);

        if (dialog.getBooleanValue(Form.ADD_SCHEMA_ASSERTION)) {
            testStep.addAssertion(SchemaComplianceAssertion.ID);
        }

        UISupport.selectAndShow(testStep);

        if (dialog.getBooleanValue(Form.SHOW_TESTCASE)) {
            UISupport.selectAndShow(testCase);
        }

        return true;
    }

    @AForm(name = "Add MockResponse to TestCase", description = "Options for adding this Operations Response to a "
            + "TestCase using a default response message generated from the schema.", helpUrl = HelpUrls.ADDMOCKOPERATIONASMOCKRESPONSESTEP_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    private interface Form {
        @AField(name = "Name", description = "Unique name of MockResponse Step")
        public final static String STEP_NAME = "Name";

        @AField(name = "Path", description = "Path to listen on")
        public final static String PATH = "Path";

        @AField(name = "Port", description = "Port to listen on", type = AFieldType.INT)
        public final static String PORT = "Port";

        @AField(name = "Add Schema Assertion", description = "Adds SchemaCompliance Assertion for request", type = AFieldType.BOOLEAN)
        public final static String ADD_SCHEMA_ASSERTION = "Add Schema Assertion";

        @AField(name = "Shows TestCase Editor", description = "Shows the target steps TestCase editor", type = AFieldType.BOOLEAN)
        public final static String SHOW_TESTCASE = "Shows TestCase Editor";
    }
}
