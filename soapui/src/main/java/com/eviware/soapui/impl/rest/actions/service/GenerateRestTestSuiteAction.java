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

package com.eviware.soapui.impl.rest.actions.service;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.RestRequestStepFactory;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.types.StringList;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormOptionsField;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

import java.util.Arrays;
import java.util.List;

/**
 * Generates a TestSuite for the specified Interface
 *
 * @author ole.matzura
 */

public class GenerateRestTestSuiteAction extends AbstractSoapUIAction<RestService> {
    public GenerateRestTestSuiteAction() {
        super("Generate TestSuite", "Generates TestSuite with TestCase(s) for all Resources in this Service");
    }

    public void perform(RestService target, Object param) {
        generateTestSuite(target, false);
    }

    public WsdlTestSuite generateTestSuite(RestService service, boolean atCreation) {
        XFormDialog dialog = ADialogBuilder.buildDialog(GenerateForm.class);
        dialog.setValue(GenerateForm.STYLE, "One TestCase for each Resource");

        StringList paths = new StringList();
        for (RestResource resource : service.getAllResources()) {
            paths.add(resource.getName() + ": " + resource.getFullPath(false));
        }

        dialog.setOptions(GenerateForm.RESOURCES, paths.toStringArray());
        XFormOptionsField operationsFormField = (XFormOptionsField) dialog.getFormField(GenerateForm.RESOURCES);
        operationsFormField.setSelectedOptions(paths.toStringArray());

        WsdlProject project = service.getProject();
        String[] testSuites = ModelSupport.getNames(new String[]{"<create>"}, project.getTestSuiteList());
        dialog.setOptions(GenerateForm.TESTSUITE, testSuites);

        if (dialog.show()) {
            List<String> resources = Arrays.asList(StringUtils.toStringArray(operationsFormField.getSelectedOptions()));
            if (resources.size() == 0) {
                UISupport.showErrorMessage("No Resources selected..");
                return null;
            }

            String testSuiteName = dialog.getValue(GenerateForm.TESTSUITE);

            if (testSuiteName.equals("<create>")) {
                testSuiteName = UISupport.prompt("Enter name of TestSuite to create", "Generate TestSuite",
                        service.getName() + " TestSuite");
            }

            if (testSuiteName != null && testSuiteName.trim().length() > 0) {
                WsdlTestSuite testSuite = project.getTestSuiteByName(testSuiteName);

                if (testSuite == null) {
                    testSuite = project.addNewTestSuite(testSuiteName);
                    testSuite.setDescription("TestSuite generated for REST Service [" + service.getName() + "]");
                }

                int style = dialog.getValueIndex(GenerateForm.STYLE);
                boolean generateLoadTest = dialog.getBooleanValue(GenerateForm.GENERATE_LOADTEST);
                if (style == 0) {
                    generateMulipleTestCases(testSuite, service, generateLoadTest, resources);
                } else if (style == 1) {
                    generateSingleTestCase(testSuite, service, generateLoadTest, resources);
                }

                if (!atCreation) {
                    UISupport.showDesktopPanel(testSuite);
                }

                return testSuite;
            }
        }

        return null;
    }

    private void generateSingleTestCase(WsdlTestSuite testSuite, RestService service, boolean createLoadTest,
                                        List<String> resources) {
        WsdlTestCase testCase = testSuite.addNewTestCase(service.getName() + " TestSuite");

        for (RestResource resource : service.getAllResources()) {
            if (!resources.contains(resource.getName() + ": " + resource.getFullPath(false))) {
                continue;
            }

            if (resource.getRequestCount() > 0) {
                for (int x = 0; x < resource.getRequestCount(); x++) {
                    testCase.addTestStep(RestRequestStepFactory.createConfig(resource.getRequestAt(x),
                            resource.getName()));
                }
            }
        }

        if (createLoadTest) {
            testCase.addNewLoadTest("LoadTest 1");
        }
    }

    private void generateMulipleTestCases(WsdlTestSuite testSuite, RestService service, boolean createLoadTest,
                                          List<String> resources) {
        for (RestResource resource : service.getAllResources()) {
            if (!resources.contains(resource.getName() + ": " + resource.getFullPath(false))) {
                continue;
            }

            WsdlTestCase testCase = testSuite.addNewTestCase(resource.getName() + " TestCase");
            testCase.setDescription("TestCase generated for REST Resource [" + resource.getName() + "] located at ["
                    + resource.getFullPath(false) + "]");

            if (resource.getRequestCount() > 0) {
                for (int x = 0; x < resource.getRequestCount(); x++) {
                    RestRequest request = resource.getRequestAt(x);
                    testCase.addTestStep(RestRequestStepFactory.createConfig(request, request.getName()));
                }
            }

            if (createLoadTest) {
                testCase.addNewLoadTest("LoadTest 1");
            }
        }
    }

    @AForm(name = "Generate TestSuite", description = "Generates TestSuite with TestCase(s) for all Resources in this Service", helpUrl = HelpUrls.GENERATE_TESTSUITE_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
    private class GenerateForm {
        @AField(name = "TestSuite", description = "The TestSuite to create or use", type = AFieldType.ENUMERATION)
        public final static String TESTSUITE = "TestSuite";

        @AField(name = "Style", description = "Select the style of TestCases to create", type = AFieldType.RADIOGROUP, values = {
                "One TestCase for each Resource", "Single TestCase with one Request for each Method"})
        public final static String STYLE = "Style";

        @AField(name = "Resources", description = "The Resources for which to Generate Tests", type = AFieldType.MULTILIST)
        public final static String RESOURCES = "Resources";

        @AField(name = "Generate LoadTest", description = "Generates a default LoadTest for each created TestCase", type = AFieldType.BOOLEAN)
        public final static String GENERATE_LOADTEST = "Generate LoadTest";
    }
}
