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

package com.eviware.soapui.support.resolver;

import com.eviware.soapui.analytics.Analytics;
import com.eviware.soapui.analytics.SoapUIActions;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlRunTestCaseTestStep;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;

public class CreateNewEmptyTestCase implements Resolver {

    private boolean resolved;
    private WsdlRunTestCaseTestStep testStep;

    public CreateNewEmptyTestCase(WsdlRunTestCaseTestStep wsdlRunTestCaseTestStep) {
        testStep = wsdlRunTestCaseTestStep;
    }

    public String getDescription() {
        return "Create new empty test case";
    }

    @Override
    public String toString() {
        return getDescription();
    }

    public String getResolvedPath() {
        return null;
    }

    public boolean isResolved() {
        return resolved;
    }

    public boolean resolve() {
        WsdlTestCase tCase = testStep.getTestCase().getTestSuite().addNewTestCase("New Test Case");
        testStep.setTargetTestCase(tCase);
        resolved = true;

        Analytics.trackAction(SoapUIActions.CREATE_TEST_CASE.getActionName());

        return resolved;
    }

}
