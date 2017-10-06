package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlGroovyScriptTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.GroovyScriptStepFactory;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.smartbear.ready.recipe.teststeps.GroovyScriptTestStepStruct;
import com.smartbear.ready.recipe.teststeps.TestStepStruct;

import static com.smartbear.ready.recipe.TestStepNames.createUniqueName;

public class GroovyScriptTestStepParser implements TestStepJsonParser {
    @Override
    public void createTestStep(WsdlTestCase testCase, TestStepStruct struct, StringToObjectMap context) {
        GroovyScriptTestStepStruct groovyScriptTestStepStruct = (GroovyScriptTestStepStruct) struct;
        String testStepName = createUniqueName(testCase, struct.name, "Groovy Script");
        WsdlGroovyScriptTestStep groovyScriptTestStep = (WsdlGroovyScriptTestStep) testCase.addTestStep(GroovyScriptStepFactory.GROOVY_TYPE, testStepName);
        groovyScriptTestStep.setScript(groovyScriptTestStepStruct.script);
    }
}
