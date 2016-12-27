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

package com.eviware.soapui.impl.wsdl.actions.testsuite;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Removes a WsdlTestSuite from its WsdlProject
 *
 * @author Ole.Matzura
 */

public class DeleteTestSuiteAction extends AbstractSoapUIAction<WsdlTestSuite> {
    public DeleteTestSuiteAction() {
        super("Remove", "Removes this TestSuite from the project");
        // putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "DELETE" ));
    }

    public void perform(WsdlTestSuite testSuite, Object param) {
        for (int c = 0; c < testSuite.getTestCaseCount(); c++) {
            if (SoapUI.getTestMonitor().hasRunningTest(testSuite.getTestCaseAt(c))) {
                UISupport.showErrorMessage("Cannot remove testSuite due to running tests");
                return;
            }
        }

        if (UISupport.confirm("Remove TestSuite [" + testSuite.getName() + "] from project", "Remove TestSuite")) {
            ((WsdlProject) testSuite.getProject()).removeTestSuite(testSuite);
        }
    }
}
