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

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Moves the specified WsdlTestCase up one step in the WsdlTestSuites list of
 * WsdlTestCases
 *
 * @author ole.matzura
 */

public class MoveTestCaseUpAction extends AbstractSoapUIAction<WsdlTestCase> {
    public MoveTestCaseUpAction() {
        super("Move TestCase Up", "Moves this TestCase up");
    }

    public void perform(WsdlTestCase testCase, Object param) {
        WsdlTestSuite testSuite = testCase.getTestSuite();
        int ix = testSuite.getIndexOfTestCase(testCase);
        if (ix == -1 || ix == 0) {
            return;
        }

        testSuite.moveTestCase(ix, -1);
        UISupport.select(testCase);
    }
}
