package com.smartbear.ready.recipe;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.smartbear.ready.recipe.teststeps.TestStepStruct;

/**
 * Defines a class that can parse a specific type of test step JSON object in a test recipe and create test steps in a
 * test case based on the information extracted.
 */
interface TestStepJsonParser {

    /**
     * Adds test steps to a test case according to the instruction in the JSON element that is passed in. Typically
     * this will just be one test case, but there are exceptions. For instance, a Data source loop will
     * add at least three test steps.
     *
     * @param testCase        the TestCase object to add test steps to
     * @param testStepElement a JSON object found in a test recipe, describing the test step(s) objects to add
     * @param context A map containing key-value pairs which could provide the context to various parsers
     */
    void createTestStep(WsdlTestCase testCase, TestStepStruct testStepElement, StringToObjectMap context) throws ParseException;
}
