package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlDelayTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.DelayStepFactory;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.smartbear.ready.recipe.teststeps.DelayTestStepStruct;
import com.smartbear.ready.recipe.teststeps.TestStepStruct;

import static com.smartbear.ready.recipe.TestStepNames.createUniqueName;

public class DelayTestStepParser implements TestStepJsonParser {
    @Override
    public void createTestStep(WsdlTestCase testCase, TestStepStruct testStepStruct, StringToObjectMap context) throws ParseException {
        DelayTestStepStruct delayTestStepStruct = (DelayTestStepStruct) testStepStruct;
        String testStepName = createUniqueName(testCase, testStepStruct.name, "Delay");
        WsdlDelayTestStep delayTestStep = (WsdlDelayTestStep) testCase.addTestStep(DelayStepFactory.DELAY_TYPE, testStepName);
        delayTestStep.setDelay(delayTestStepStruct.delay);
    }
}