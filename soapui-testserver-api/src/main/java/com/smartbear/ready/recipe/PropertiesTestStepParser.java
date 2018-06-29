package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.PropertiesStepFactory;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.smartbear.ready.recipe.teststeps.PropertiesTestStepStruct;
import com.smartbear.ready.recipe.teststeps.TestStepStruct;

import java.util.Map;

import static com.smartbear.ready.recipe.TestStepNames.createUniqueName;

public class PropertiesTestStepParser implements TestStepJsonParser {
    @Override
    public void createTestStep(WsdlTestCase testCase, TestStepStruct testStepElement, StringToObjectMap context) throws ParseException {
        PropertiesTestStepStruct testStepStruct = (PropertiesTestStepStruct) testStepElement;
        String testStepName = createUniqueName(testCase, testStepStruct.name, "Properties");

        WsdlTestStep propertiesTestStep = testCase.addTestStep(PropertiesStepFactory.PROPERTIES_TYPE, testStepName);
        if (testStepStruct.properties != null) {
            for (Map.Entry<String, String> property : testStepStruct.properties.entrySet()) {
                propertiesTestStep.setPropertyValue(property.getKey(), property.getValue());
            }
        }
    }
}