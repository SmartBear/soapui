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

package com.eviware.soapui.security.actions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Removes a SecurityTest from its WsdlTestCase
 *
 * @author Ole.Matzura
 */

public class DeleteSecurityTestAction extends AbstractSoapUIAction<SecurityTest> {
    public DeleteSecurityTestAction() {
        super("Remove", "Removes this Test Schedule from the test-case");
    }

    public void perform(SecurityTest securityTest, Object param) {

        if (SoapUI.getTestMonitor().hasRunningSecurityTest((securityTest.getTestCase()))) {
            UISupport.showErrorMessage("Cannot remove test while tests are running");
            return;
        }

        if (UISupport.confirm("Remove SecurityTest [" + securityTest.getName() + "] from test-case",
                "Remove SecurityTest")) {
            ((WsdlTestCase) securityTest.getTestCase()).removeSecurityTest(securityTest);
        }
    }
}
