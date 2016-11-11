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

package com.eviware.soapui.security.scan;

import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.support.types.StringToStringMap;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class PropertyMutation {

    public static final String REQUEST_MUTATIONS_STACK = "RequestMutationsStack";

    private String propertyName;
    private String propertyValue;
    private StringToStringMap mutatedParameters;
    private TestStep testStep;

    public TestStep getTestStep() {
        return testStep;
    }

    public void setTestStep(TestStep testStep) {
        this.testStep = testStep;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public StringToStringMap getMutatedParameters() {
        return mutatedParameters;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public void setMutatedParameters(StringToStringMap mutatedParameters) {
        if (this.mutatedParameters == null) {
            this.mutatedParameters = new StringToStringMap();
        }
        this.mutatedParameters.putAll(mutatedParameters);
    }

    public void updateRequestProperty(TestStep testStep) {
        testStep.getProperty(this.getPropertyName()).setValue(this.getPropertyValue());
    }

    @SuppressWarnings("unchecked")
    public void addMutation(SecurityTestRunContext context) {
        Stack<PropertyMutation> stack = (Stack<PropertyMutation>) context.get(REQUEST_MUTATIONS_STACK);
        stack.push(this);
    }

    @SuppressWarnings("unchecked")
    public static PropertyMutation popMutation(SecurityTestRunContext context) {
        Stack<PropertyMutation> requestMutationsStack = (Stack<PropertyMutation>) context.get(REQUEST_MUTATIONS_STACK);
        return requestMutationsStack.empty() ? null : requestMutationsStack.pop();
    }

    @SuppressWarnings("unchecked")
    public static List<PropertyMutation> popAllMutation(SecurityTestRunContext context) {
        Stack<PropertyMutation> requestMutationsStack = (Stack<PropertyMutation>) context.get(REQUEST_MUTATIONS_STACK);
        PropertyMutation[] array = requestMutationsStack.toArray(new PropertyMutation[requestMutationsStack.size()]);
        requestMutationsStack.clear();
        return Arrays.asList(array);
    }

}
