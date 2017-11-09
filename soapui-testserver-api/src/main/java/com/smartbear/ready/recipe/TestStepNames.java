package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.StringUtils;

/**
 * Holds names identifying test step types in the test recipe format.
 */
public class TestStepNames {
    public static final String REST_REQUEST_TYPE = "REST Request";
    public static final String SOAP_REQUEST_TYPE = "SOAP Request";
    public static final String PROPERTY_TRANSFER_TYPE = "Property Transfer";
    public static final String GROOVY_SCRIPT_TYPE = "Groovy";
    public static final String JDBC_REQUEST_TYPE = "JDBC Request";
    public static final String DELAY_TYPE = "Delay";
    public static final String PROPERTIES_TYPE = "Properties";
    public static final String SOAP_MOCK_RESPONSE_TYPE = "SOAPMockResponse";

    public static String createUniqueName(WsdlTestCase testCase, String testStepName, String defaultName) {

        if( !StringUtils.hasContent( testStepName )){
            testStepName = defaultName;
        }

        if (testCase.getTestStepByName(testStepName) == null) {
            return testStepName;
        }

        String originalName = testStepName;
        int count = 2;
        while (testCase.getTestStepByName(testStepName) != null) {
            testStepName = originalName + " " + String.valueOf(count);
            count++;
        }
        return testStepName;
    }
}
