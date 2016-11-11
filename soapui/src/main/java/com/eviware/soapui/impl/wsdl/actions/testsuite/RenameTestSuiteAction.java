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

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames a WsdlTestSuite
 *
 * @author Ole.Matzura
 */

public class RenameTestSuiteAction extends AbstractSoapUIAction<WsdlTestSuite> {
    public RenameTestSuiteAction() {
        super("Rename", "Renames this TestSuite");
        // putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "F2" ));
    }

    public void perform(WsdlTestSuite testSuite, Object param) {
        String name = UISupport.prompt("Specify name of TestSuite", "Rename TestSuite", testSuite.getName());
        if (name == null || name.equals(testSuite.getName())) {
            return;
        }
        while (testSuite.getProject().getTestSuiteByName(name.trim()) != null) {
            name = UISupport.prompt("Specify unique name of TestSuite", "Rename TestSuite", testSuite.getName());
            if (name == null || name.equals(testSuite.getName())) {
                return;
            }
        }

        testSuite.setName(name);
    }
}
