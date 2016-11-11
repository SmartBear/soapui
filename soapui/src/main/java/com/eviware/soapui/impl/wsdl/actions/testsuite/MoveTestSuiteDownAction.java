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

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Moves the specified WsdlTestCase down one step in the WsdlTestSuites list of
 * WsdlTestCases
 *
 * @author ole.matzura
 */

public class MoveTestSuiteDownAction extends AbstractSoapUIAction<WsdlTestSuite> {
    public MoveTestSuiteDownAction() {
        super("Move TestSuite Down", "Moves this TestSuite down");
    }

    public void perform(WsdlTestSuite testSuite, Object param) {
        WsdlProject project = testSuite.getProject();
        int ix = project.getIndexOfTestSuite(testSuite);
        if (ix == -1 || ix >= project.getTestSuiteCount() - 1) {
            return;
        }

        project.moveTestSuite(ix, 1);
        UISupport.select(testSuite);
    }
}
