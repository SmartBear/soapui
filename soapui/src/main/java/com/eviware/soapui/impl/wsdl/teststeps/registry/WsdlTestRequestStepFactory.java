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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.config.RequestStepConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.config.WsaConfigConfig;
import com.eviware.soapui.config.WsdlRequestConfig;
import com.eviware.soapui.config.WsrmConfigConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SchemaComplianceAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.NotSoapFaultAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.SoapResponseAssertion;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for WsdlTestRequestSteps
 *
 * @author Ole.Matzura
 */

public class WsdlTestRequestStepFactory extends WsdlTestStepFactory {
    public static final String REQUEST_TYPE = "request";
    private static final String CREATE_OPTIONAL_ELEMENTS_IN_REQUEST = "Create optional elements";
    private static final String ADD_SOAP_RESPONSE_ASSERTION = "Add SOAP Response Assertion";
    private static final String ADD_SOAP_FAULT_ASSERTION = "Add Not SOAP Fault Assertion";
    private static final String ADD_SCHEMA_ASSERTION = "Add Schema Assertion";
    public static final String STEP_NAME = "Name";
    private XFormDialog dialog;
    private StringToStringMap dialogValues = new StringToStringMap();

    public WsdlTestRequestStepFactory() {
        super(REQUEST_TYPE, "SOAP Request", "Submits a SOAP request and validates its response", "/soap_request_step.png");
    }

    public WsdlTestStep buildTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {
        return new WsdlTestRequestStep(testCase, config, forLoadTest);
    }

    public static TestStepConfig createConfig(WsdlRequest request, String stepName) {
        RequestStepConfig requestStepConfig = RequestStepConfig.Factory.newInstance();

        requestStepConfig.setInterface(request.getOperation().getInterface().getName());
        requestStepConfig.setOperation(request.getOperation().getName());

        WsdlRequestConfig testRequestConfig = requestStepConfig.addNewRequest();

        testRequestConfig.setName(stepName);
        testRequestConfig.setEncoding(request.getEncoding());
        testRequestConfig.setEndpoint(request.getEndpoint());
        testRequestConfig.addNewRequest().setStringValue(request.getRequestContent());
        testRequestConfig.setOutgoingWss(request.getOutgoingWss());
        testRequestConfig.setIncomingWss(request.getIncomingWss());
        testRequestConfig.setTimeout(request.getTimeout());
        testRequestConfig.setSslKeystore(request.getSslKeystore());

        testRequestConfig.setUseWsAddressing(request.isWsaEnabled());
        testRequestConfig.setUseWsReliableMessaging(request.isWsrmEnabled());

        if (request.getConfig().isSetWsaConfig()) {
            testRequestConfig.setWsaConfig((WsaConfigConfig) request.getConfig().getWsaConfig().copy());
        }

        if (request.getConfig().isSetWsrmConfig()) {
            testRequestConfig.setWsrmConfig((WsrmConfigConfig) request.getConfig().getWsrmConfig().copy());
        }

        if ((CredentialsConfig) request.getConfig().getCredentials() != null) {
            testRequestConfig.setCredentials((CredentialsConfig) request.getConfig().getCredentials().copy());
        }

        testRequestConfig.setWssPasswordType(request.getConfig().getWssPasswordType());

        TestStepConfig testStep = TestStepConfig.Factory.newInstance();
        testStep.setType(REQUEST_TYPE);
        testStep.setConfig(requestStepConfig);

        return testStep;
    }

    public static TestStepConfig createConfig(WsdlOperation operation, String stepName) {
        RequestStepConfig requestStepConfig = RequestStepConfig.Factory.newInstance();

        requestStepConfig.setInterface(operation.getInterface().getName());
        requestStepConfig.setOperation(operation.getName());

        WsdlRequestConfig testRequestConfig = requestStepConfig.addNewRequest();
        testRequestConfig.addNewWsaConfig();

        testRequestConfig.setName(stepName);
        testRequestConfig.setEncoding("UTF-8");
        String[] endpoints = operation.getInterface().getEndpoints();
        if (endpoints.length > 0) {
            testRequestConfig.setEndpoint(endpoints[0]);
        }

        String requestContent = operation.createRequest(SoapUI.getSettings().getBoolean(
                WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS));
        testRequestConfig.addNewRequest().setStringValue(requestContent);

        // add ws-a action
        String defaultAction = WsdlUtils.getDefaultWsaAction(operation, false);
        if (StringUtils.hasContent(defaultAction)) {
            testRequestConfig.getWsaConfig().setAction(defaultAction);
        }

        TestStepConfig testStep = TestStepConfig.Factory.newInstance();
        testStep.setType(REQUEST_TYPE);
        testStep.setConfig(requestStepConfig);

        return testStep;
    }

    public TestStepConfig createNewTestStep(WsdlTestCase testCase, String name) {
        // build list of available interfaces / operations
        Project project = testCase.getTestSuite().getProject();
        List<String> options = new ArrayList<String>();
        List<Operation> operations = new ArrayList<Operation>();

        for (int c = 0; c < project.getInterfaceCount(); c++) {
            Interface iface = project.getInterfaceAt(c);
            for (int i = 0; i < iface.getOperationCount(); i++) {
                options.add(iface.getName() + " -> " + iface.getOperationAt(i).getName());
                operations.add(iface.getOperationAt(i));
            }
        }

        Object op = UISupport.prompt("Select operation to invoke for request", "New TestRequest", options.toArray());
        if (op != null) {
            int ix = options.indexOf(op);
            if (ix != -1) {
                WsdlOperation operation = (WsdlOperation) operations.get(ix);

                if (dialog == null) {
                    buildDialog();
                }

                dialogValues.put(STEP_NAME, name);
                dialogValues = dialog.show(dialogValues);
                if (dialog.getReturnValue() != XFormDialog.OK_OPTION) {
                    return null;
                }

                return createNewTestStep(operation, dialogValues);
            }
        }

        return null;
    }

    public TestStepConfig createNewTestStep(WsdlOperation operation, StringToStringMap values) {
        String name;
        name = values.get(STEP_NAME);

        String requestContent = operation.createRequest(values.getBoolean(CREATE_OPTIONAL_ELEMENTS_IN_REQUEST));

        RequestStepConfig requestStepConfig = RequestStepConfig.Factory.newInstance();

        requestStepConfig.setInterface(operation.getInterface().getName());
        requestStepConfig.setOperation(operation.getName());

        WsdlRequestConfig testRequestConfig = requestStepConfig.addNewRequest();

        testRequestConfig.setName(name);
        testRequestConfig.setEncoding("UTF-8");
        String[] endpoints = operation.getInterface().getEndpoints();
        if (endpoints.length > 0) {
            testRequestConfig.setEndpoint(endpoints[0]);
        }
        testRequestConfig.addNewRequest().setStringValue(requestContent);

        if (values.getBoolean(ADD_SOAP_RESPONSE_ASSERTION)) {
            TestAssertionConfig assertionConfig = testRequestConfig.addNewAssertion();
            assertionConfig.setType(SoapResponseAssertion.ID);
        }

        if (values.getBoolean(ADD_SCHEMA_ASSERTION)) {
            TestAssertionConfig assertionConfig = testRequestConfig.addNewAssertion();
            assertionConfig.setType(SchemaComplianceAssertion.ID);
        }

        if (values.getBoolean(ADD_SOAP_FAULT_ASSERTION)) {
            TestAssertionConfig assertionConfig = testRequestConfig.addNewAssertion();
            assertionConfig.setType(NotSoapFaultAssertion.ID);
        }

        TestStepConfig testStep = TestStepConfig.Factory.newInstance();
        testStep.setType(REQUEST_TYPE);
        testStep.setConfig(requestStepConfig);
        testStep.setName(name);

        return testStep;
    }

    public boolean canCreate() {
        return true;
    }

    private void buildDialog() {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Add Request to TestCase");
        XForm mainForm = builder.createForm("Basic");

        mainForm.addTextField(STEP_NAME, "Name of TestStep", XForm.FieldType.URL).setWidth(30);

        mainForm.addCheckBox(ADD_SOAP_RESPONSE_ASSERTION, "(adds validation that response is a SOAP message)");
        mainForm.addCheckBox(ADD_SCHEMA_ASSERTION, "(adds validation that response complies with its schema)");
        mainForm.addCheckBox(ADD_SOAP_FAULT_ASSERTION, "(adds validation that response is not a SOAP Fault)");
        mainForm.addCheckBox(CREATE_OPTIONAL_ELEMENTS_IN_REQUEST, "(creates optional content in sample request)");

        dialog = builder.buildDialog(builder.buildOkCancelActions(),
                "Specify options for adding a new request to a TestCase", UISupport.OPTIONS_ICON);

        dialogValues.put(ADD_SOAP_RESPONSE_ASSERTION, Boolean.TRUE.toString());
    }

    @Override
    public boolean canAddTestStepToTestCase(WsdlTestCase testCase) {
        for (Interface iface : testCase.getTestSuite().getProject().getInterfaceList()) {
            if (iface instanceof WsdlInterface && iface.getOperationCount() > 0) {
                return true;
            }
        }

        UISupport.showErrorMessage("Missing SOAP Operations in Project");
        return false;
    }
}
