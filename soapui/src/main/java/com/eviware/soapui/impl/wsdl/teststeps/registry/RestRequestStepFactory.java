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

import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.RestRequestStepConfig;
import com.eviware.soapui.config.StringToStringMapConfig;
import com.eviware.soapui.config.StringToStringMapConfig.Entry;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.wsdl.monitor.WsdlMonitorMessageExchange;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.TupleList;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory for WsdlTestRequestSteps
 *
 * @author Ole.Matzura
 */

public class RestRequestStepFactory extends WsdlTestStepFactory {
    public static final String RESTREQUEST_TYPE = "restrequest";
    public static final String STEP_NAME = "Name";

    // private XFormDialog dialog;
    // private StringToStringMap dialogValues = new StringToStringMap();

    public RestRequestStepFactory() {
        super(RESTREQUEST_TYPE, "REST Request", "Submits a REST-style Request and validates its response",
                "/rest_request_step.png");
    }

    public static class ItemDeletedException extends Exception {

    }

    public WsdlTestStep buildTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest) {
        try {
            return new RestTestRequestStep(testCase, config, forLoadTest);
        } catch (ItemDeletedException e) {
            return null;
        }
    }

    public static TestStepConfig createConfig(RestRequest request, String stepName) {
        request.beforeSave(); //SOAP-1098
        RestRequestStepConfig requestStepConfig = RestRequestStepConfig.Factory.newInstance();

        requestStepConfig.setService(request.getOperation().getInterface().getName());
        requestStepConfig.setResourcePath(request.getOperation().getFullPath());
        requestStepConfig.setMethodName(request.getRestMethod().getName());
        requestStepConfig.addNewRestRequest().set(request.getConfig().copy());

        TestStepConfig testStep = TestStepConfig.Factory.newInstance();
        testStep.setType(RESTREQUEST_TYPE);
        testStep.setConfig(requestStepConfig);
        testStep.setName(stepName);

        return testStep;
    }

    @SuppressWarnings("unchecked")
    public TestStepConfig createNewTestStep(WsdlTestCase testCase, String name) {
        // build list of available interfaces / restResources
        Project project = testCase.getTestSuite().getProject();
        List<String> options = new ArrayList<String>();
        TupleList<RestMethod, RestRequest> restMethods = new TupleList<RestMethod, RestRequest>();

        for (int c = 0; c < project.getInterfaceCount(); c++) {
            Interface iface = project.getInterfaceAt(c);
            if (iface instanceof RestService) {
                List<RestResource> resources = ((RestService) iface).getAllResources();

                for (RestResource resource : resources) {
                    // options.add( iface.getName() + " -> " + resource.getPath() );
                    // restMethods.add( resource, null );

                    for (RestMethod method : resource.getRestMethodList()) {
                        String methodStr = iface.getName() + " -> " + resource.getPath() + " -> " + method.getName();
                        restMethods.add(method, null);
                        options.add(methodStr);

                        for (RestRequest request : method.getRequestList()) {
                            restMethods.add(method, request);
                            options.add(methodStr + " -> " + request.getName());
                        }
                    }
                }
            }
        }

        if (restMethods.size() == 0) {
            UISupport.showErrorMessage("Missing REST Methods in project");
            return null;
        }

        Object op = UISupport.prompt("Select REST method to invoke for request", "New RestRequest", options.toArray());
        if (op != null) {
            int ix = options.indexOf(op);
            if (ix != -1) {
                TupleList<RestMethod, RestRequest>.Tuple tuple = restMethods.get(ix);

                // if( dialog == null )
                // buildDialog();
                //
                // dialogValues.put( STEP_NAME, name );
                // dialogValues = dialog.show( dialogValues );
                // if( dialog.getReturnValue() != XFormDialog.OK_OPTION )
                // return null;

                return tuple.getValue2() == null ? createNewTestStep(tuple.getValue1(), name) : createConfig(
                        tuple.getValue2(), name);
            }
        }

        return null;
    }

    public TestStepConfig createNewTestStep(RestMethod restMethod, String name) {
        RestRequestStepConfig requestStepConfig = RestRequestStepConfig.Factory.newInstance();
        RestRequestConfig testRequestConfig = requestStepConfig.addNewRestRequest();

        testRequestConfig.setName(name);
        testRequestConfig.setEncoding("UTF-8");

        if (restMethod != null) {
            requestStepConfig.setService(restMethod.getInterface().getName());
            requestStepConfig.setMethodName(restMethod.getName());
            requestStepConfig.setResourcePath(restMethod.getOperation().getFullPath());

            String[] endpoints = restMethod.getInterface().getEndpoints();
            if (endpoints.length > 0) {
                testRequestConfig.setEndpoint(endpoints[0]);
            }

            testRequestConfig.addNewRequest();
            StringToStringMapConfig parametersConfig = testRequestConfig.addNewParameters();

            for (RestParamProperty property : restMethod.getDefaultParams()) {
                if (StringUtils.hasContent(property.getDefaultValue())) {
                    Entry entry = parametersConfig.addNewEntry();
                    entry.setKey(property.getName());
                    entry.setValue(property.getDefaultValue());
                }
            }
        }

        TestStepConfig testStepConfig = TestStepConfig.Factory.newInstance();
        testStepConfig.setType(RESTREQUEST_TYPE);
        testStepConfig.setConfig(requestStepConfig);
        testStepConfig.setName(name);

        return testStepConfig;
    }

    public boolean canCreate() {
        return true;
    }

    public TestStepConfig createConfig(WsdlMonitorMessageExchange me, String stepName) {
        RestRequestConfig testRequestConfig = RestRequestConfig.Factory.newInstance();

        testRequestConfig.setName(stepName);
        testRequestConfig.setEncoding("UTF-8");
        testRequestConfig.setEndpoint(me.getEndpoint());
        // testRequestConfig.setParameters(
        // set parameters

        String requestContent = me.getRequestContent();
        testRequestConfig.addNewRequest().setStringValue(requestContent);

        TestStepConfig testStep = TestStepConfig.Factory.newInstance();
        testStep.setType(RESTREQUEST_TYPE);
        testStep.setConfig(testRequestConfig);
        testStep.setName(stepName);
        return testStep;
    }

    // private void buildDialog()
    // {
    // XFormDialogBuilder builder = XFormFactory.createDialogBuilder(
    // "Add REST Request to TestCase" );
    // XForm mainForm = builder.createForm( "Basic" );
    //
    // mainForm.addTextField( STEP_NAME, "Name of TestStep", XForm.FieldType.URL
    // ).setWidth( 30 );
    //
    // dialog = builder.buildDialog( builder.buildOkCancelActions(),
    // "Specify options for adding a new REST Request to a TestCase",
    // UISupport.OPTIONS_ICON );
    // }

    @Override
    public boolean canAddTestStepToTestCase(WsdlTestCase testCase) {
        for (Interface iface : testCase.getTestSuite().getProject().getInterfaceList()) {
            if (iface instanceof RestService) {
                for (RestResource resource : ((RestService) iface).getAllResources()) {
                    if (resource.getRestMethodCount() > 0) {
                        return true;
                    }
                }
            }
        }

        UISupport.showErrorMessage("Missing REST Methods in Project");
        return false;

    }
}
