package com.smartbear.ready.recipe;

import com.eviware.soapui.config.MockResponseStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.support.CompressedStringSupport;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlMockResponseStepFactory;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.smartbear.ready.recipe.teststeps.TestStepStruct;
import com.smartbear.ready.recipe.teststeps.WsdlMockResponseStepStruct;

import static com.smartbear.ready.recipe.TestStepNames.createUniqueName;
import static com.smartbear.ready.recipe.WsdlExtractor.getWsdlInterface;

public class SoapMockResponseTestStepParser implements TestStepJsonParser {
    @Override
    public void createTestStep(WsdlTestCase testCase, TestStepStruct testStepElement, StringToObjectMap context) throws ParseException {
        WsdlMockResponseStepStruct stepStruct = (WsdlMockResponseStepStruct) testStepElement;
        WsdlInterface wsdlInterface = getWsdlInterface(testCase, stepStruct.wsdl, stepStruct.binding);
        if (wsdlInterface == null) {
            throw new ParseException("Failed to find specified binding [" + stepStruct.binding + "] in WSDL");
        }

        WsdlOperation operation = wsdlInterface.getOperationByName(stepStruct.operation);
        if (operation == null) {
            throw new ParseException("Failed to find specified operation [" + stepStruct +
                    "] in binding [" + wsdlInterface.getBindingName().toString() + "]");
        }

        TestStepConfig testStepConfig = createStepConfig(testCase, stepStruct, operation);
        testCase.addTestStep(testStepConfig);
    }

    private TestStepConfig createStepConfig(WsdlTestCase testCase, WsdlMockResponseStepStruct stepStruct,
                                            WsdlOperation operation) {
        TestStepConfig testStepConfig = TestStepConfig.Factory.newInstance();
        testStepConfig.setType(WsdlMockResponseStepFactory.MOCKRESPONSE_TYPE);
        testStepConfig.setName(createUniqueName(testCase, stepStruct.name, "WsdlMockResponse"));

        MockResponseStepConfig config = MockResponseStepConfig.Factory.newInstance();
        config.setInterface(stepStruct.binding);
        config.setOperation(stepStruct.operation);
        config.setPort(stepStruct.port);
        config.setPath(stepStruct.path);
        config.addNewResponse();
        config.getResponse().addNewResponseContent();

        if (stepStruct.createResponse) {
            String response = operation.createResponse(true);
            CompressedStringSupport.setString(config.getResponse().getResponseContent(), response);
        }

        testStepConfig.addNewConfig().set(config);
        return testStepConfig;
    }
}