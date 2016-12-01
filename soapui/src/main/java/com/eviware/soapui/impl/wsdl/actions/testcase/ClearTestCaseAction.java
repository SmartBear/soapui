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

package com.eviware.soapui.impl.wsdl.actions.testcase;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Clears a WsdlTestCase
 *
 * @author Ole.Matzura
 */

public class ClearTestCaseAction extends AbstractSoapUIAction<WsdlTestCase> {
    public ClearTestCaseAction() {
        super("Clear", "Clears this TestCase");
    }

    public void perform(WsdlTestCase testCase, Object param) {
        if (SoapUI.getTestMonitor().hasRunningTest(testCase)) {
            UISupport.showErrorMessage("Cannot clear TestCase while tests are running");
        } else if (UISupport.confirm("Remove all TestSteps and LoadTests from this TestCase?", "Clear TestCase")) {
            while (testCase.getLoadTestCount() > 0) {
                testCase.removeLoadTest(testCase.getLoadTestAt(0));
            }

            while (testCase.getTestStepCount() > 0) {
                testCase.removeTestStep(testCase.getTestStepAt(0));
            }
        }
    }
}
