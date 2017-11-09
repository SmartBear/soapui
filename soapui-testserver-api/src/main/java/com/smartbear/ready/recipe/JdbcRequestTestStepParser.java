package com.smartbear.ready.recipe;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.registry.JdbcRequestTestStepFactory;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.smartbear.ready.recipe.assertions.AssertionStruct;
import com.smartbear.ready.recipe.teststeps.JdbcRequestTestStepStruct;
import com.smartbear.ready.recipe.teststeps.TestStepStruct;

import static com.smartbear.ready.recipe.TestStepNames.createUniqueName;

public class JdbcRequestTestStepParser implements TestStepJsonParser {

    @Override
    public void createTestStep(WsdlTestCase testCase, TestStepStruct testStepStruct, StringToObjectMap context) {
        JdbcRequestTestStepStruct jdbcRequestTestStepStruct = (JdbcRequestTestStepStruct) testStepStruct;

        JdbcRequestTestStepFactory factory = new JdbcRequestTestStepFactory();
        String testStepName = createUniqueName(testCase, jdbcRequestTestStepStruct.name, "JDBC Request");

        TestStepConfig config = factory.createNewTestStep(testCase, testStepName);
        JdbcRequestTestStep jdbcRequestTestStep = (JdbcRequestTestStep) testCase.addTestStep(config);

        jdbcRequestTestStep.setDriver(jdbcRequestTestStepStruct.driver);
        jdbcRequestTestStep.setConnectionString(jdbcRequestTestStepStruct.connectionString);

        jdbcRequestTestStep.setQuery(jdbcRequestTestStepStruct.sqlQuery);
        jdbcRequestTestStep.setStoredProcedure(jdbcRequestTestStepStruct.storedProcedure);

        if (jdbcRequestTestStepStruct.properties != null) {
            for (String key : jdbcRequestTestStepStruct.properties.keySet()) {
                jdbcRequestTestStep.setPropertyValue(key, jdbcRequestTestStepStruct.properties.get(key));
            }
        }

        for (AssertionStruct assertion : jdbcRequestTestStepStruct.assertions) {
            WsdlMessageAssertion testAssertion = (WsdlMessageAssertion) jdbcRequestTestStep.addAssertion(assertion.type);
            assertion.setNameAndConfigureAssertion(testAssertion);
        }
    }
}
