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

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestRequestStepFactory;
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
 * Generates a TestSuite for the specified Interface
 *
 * @author ole.matzura
 */

public class GenerateWsdlTestSuiteAction extends AbstractSoapUIAction<WsdlInterface> {
    public GenerateWsdlTestSuiteAction() {
        super("Generate TestSuite", "Generates TestSuite with TestCase(s) for all Operations in this Interface");
    }

    public void perform(WsdlInterface target, Object param) {
        generateTestSuite(target, false);
    }

    public WsdlTestSuite generateTestSuite(WsdlInterface iface, boolean atCreation) {
        XFormDialog dialog = ADialogBuilder.buildDialog(GenerateForm.class);
        dialog.setValue(GenerateForm.STYLE, "One TestCase for each Operation");
        dialog.setValue(GenerateForm.REQUEST_CONTENT, "Create new empty requests");
        String[] names = ModelSupport.getNames(iface.getOperationList());
        dialog.setOptions(GenerateForm.OPERATIONS, names);
        XFormOptionsField operationsFormField = (XFormOptionsField) dialog.getFormField(GenerateForm.OPERATIONS);
        operationsFormField.setSelectedOptions(names);

        WsdlProject project = iface.getProject();
        String[] testSuites = ModelSupport.getNames(new String[]{"<create>"}, project.getTestSuiteList());
        dialog.setOptions(GenerateForm.TESTSUITE, testSuites);

        if (dialog.show()) {
            List<String> operations = StringUtils.toStringList(operationsFormField.getSelectedOptions());
            if (operations.size() == 0) {
                UISupport.showErrorMessage("No Operations selected..");
                return null;
            }

            String testSuiteName = dialog.getValue(GenerateForm.TESTSUITE);

            if (testSuiteName.equals("<create>")) {
                testSuiteName = UISupport.prompt("Enter name of TestSuite to create", "Generate TestSuite",
                        iface.getName() + " TestSuite");
            }

            if (testSuiteName != null && testSuiteName.trim().length() > 0) {
                WsdlTestSuite testSuite = project.getTestSuiteByName(testSuiteName);

                if (testSuite == null) {
                    testSuite = project.addNewTestSuite(testSuiteName);
                }

                int style = dialog.getValueIndex(GenerateForm.STYLE);
                boolean useExistingRequests = dialog.getValueIndex(GenerateForm.REQUEST_CONTENT) == 0;
                boolean generateLoadTest = dialog.getBooleanValue(GenerateForm.GENERATE_LOADTEST);
                if (style == 0) {
                    generateMulipleTestCases(testSuite, iface, useExistingRequests, generateLoadTest, operations);
                } else if (style == 1) {
                    generateSingleTestCase(testSuite, iface, useExistingRequests, generateLoadTest, operations);
                }

                if (!atCreation) {
                    UISupport.showDesktopPanel(testSuite);
                }

                return testSuite;
            }
        }

        return null;
    }

    private void generateSingleTestCase(WsdlTestSuite testSuite, WsdlInterface iface, boolean useExisting,
                                        boolean createLoadTest, List<String> operations) {
        WsdlTestCase testCase = testSuite.addNewTestCase(iface.getName() + " TestSuite");

        for (int i = 0; i < iface.getOperationCount(); i++) {
            WsdlOperation operation = iface.getOperationAt(i);
            if (!operations.contains(operation.getName())) {
                continue;
            }

            if (useExisting && operation.getRequestCount() > 0) {
                for (int x = 0; x < operation.getRequestCount(); x++) {
                    testCase.addTestStep(WsdlTestRequestStepFactory.createConfig(operation.getRequestAt(x),
                            operation.getName()));
                }
            } else {
                testCase.addTestStep(WsdlTestRequestStepFactory.createConfig(operation, operation.getName()));
            }
        }

        if (createLoadTest) {
            testCase.addNewLoadTest("LoadTest 1");
        }
    }

    private void generateMulipleTestCases(WsdlTestSuite testSuite, WsdlInterface iface, boolean useExisting,
                                          boolean createLoadTest, List<String> operations) {
        for (int i = 0; i < iface.getOperationCount(); i++) {
            WsdlOperation operation = iface.getOperationAt(i);
            if (!operations.contains(operation.getName())) {
                continue;
            }

            WsdlTestCase testCase = testSuite.addNewTestCase(operation.getName() + " TestCase");

            if (useExisting && operation.getRequestCount() > 0) {
                for (int x = 0; x < operation.getRequestCount(); x++) {
                    testCase.addTestStep(WsdlTestRequestStepFactory.createConfig(operation.getRequestAt(x),
                            operation.getName()));
                }
            } else {
                testCase.addTestStep(WsdlTestRequestStepFactory.createConfig(operation, operation.getName()));
            }

            if (createLoadTest) {
                testCase.addNewLoadTest("LoadTest 1");
            }
        }
    }

    @AForm(name = "Generate TestSuite", description = "Generates TestSuite with TestCase(s) for all Operations in this Interface", helpUrl = HelpUrls.GENERATE_TESTSUITE_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    private class GenerateForm {
        @AField(name = "TestSuite", description = "The TestSuite to create or use", type = AFieldType.ENUMERATION)
        public final static String TESTSUITE = "TestSuite";

        @AField(name = "Style", description = "Select the style of TestCases to create", type = AFieldType.RADIOGROUP, values = {
                "One TestCase for each Operation", "Single TestCase with one Request for each Operation"})
        public final static String STYLE = "Style";

        @AField(name = "Request Content", description = "Select how to create Test Requests", type = AFieldType.RADIOGROUP, values = {
                "Use existing Requests in Interface", "Create new empty requests"})
        public final static String REQUEST_CONTENT = "Request Content";

        @AField(name = "Operations", description = "The Operations for which to Generate Tests", type = AFieldType.MULTILIST)
        public final static String OPERATIONS = "Operations";

        @AField(name = "Generate LoadTest", description = "Generates a default LoadTest for each created TestCase", type = AFieldType.BOOLEAN)
        public final static String GENERATE_LOADTEST = "Generate LoadTest";
    }
}
