package com.smartbear.swagger;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.RestRequestStepFactory;
import com.eviware.soapui.security.assertion.ValidHttpStatusCodesAssertion;
import com.eviware.soapui.support.ModelItemNamer;
import com.smartbear.swagger.assertion.SwaggerComplianceAssertionCreator;
import io.swagger.models.Response;
import io.swagger.v3.oas.models.responses.ApiResponses;

import java.util.Collections;
import java.util.Map;

public class OpenAPI3TestCaseGenerator {
    private WsdlTestSuite testSuite;

    void generateTestCase(WsdlProject project, RestMethod restMethod, Map<String, Object> context, ApiResponses responses) {

        if (testSuite == null) {
            testSuite = project.addNewTestSuite(ModelItemNamer.getUniqueName("TestSuite", project));
        }
        WsdlTestCase testCase = testSuite.addNewTestCase(restMethod.getResource().getName() + " " + restMethod.getMethod().name() + "-Test Case");
        for (RestRequest restRequest : restMethod.getRequestList()) {
            WsdlTestStep testStep = testCase.addTestStep(RestRequestStepFactory.createConfig(restRequest, restRequest.getName()));
            testStep.setName(restRequest.getMethod() + " " + " " + restRequest.getName());
            addAssertions((RestTestRequestStep) testStep, responses, context);
        }
    }

    private static void addAssertions(RestTestRequestStep testStep, ApiResponses apiResponse, Map<String, Object> context) {
        if (apiResponse != null && !(apiResponse.size() == 1 && apiResponse.keySet().contains("default"))) {
            ValidHttpStatusCodesAssertion validHttpStatusCodesAssertion = (ValidHttpStatusCodesAssertion) testStep.addAssertion(ValidHttpStatusCodesAssertion.LABEL);
            String codes = "";
            int i = 0;
            for (String code : apiResponse.keySet()) {
                if (code.equals("default")) {
                    continue;
                }
                codes = codes + code;
            }
            codes = codes.replaceAll(", $", "");
            validHttpStatusCodesAssertion.setCodes(codes);
        }

        new SwaggerComplianceAssertionCreator().createAssertion(testStep, Collections.<String, Response>emptyMap(), context);
    }
}
