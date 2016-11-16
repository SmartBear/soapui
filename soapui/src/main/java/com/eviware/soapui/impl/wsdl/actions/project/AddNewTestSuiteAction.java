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

package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Action for adding a new WsdlTestSuite to a WsdlProject
 *
 * @author Ole.Matzura
 */

public class AddNewTestSuiteAction extends AbstractSoapUIAction<WsdlProject> {
    public static final String SOAPUI_ACTION_ID = "AddNewTestSuiteAction";

    public AddNewTestSuiteAction() {
        super("New TestSuite", "Creates a new TestSuite in this project");
    }

    public void perform(WsdlProject target, Object param) {
        createTestSuite(target);
    }

    public WsdlTestSuite createTestSuite(WsdlProject project) {
        String name = UISupport.prompt("Specify name of TestSuite", "New TestSuite",
                "TestSuite " + (project.getTestSuiteCount() + 1));
        if (name == null) {
            return null;
        }
        while (project.getTestSuiteByName(name.trim()) != null) {
            name = UISupport.prompt("Specify unique name of TestSuite", "Rename TestSuite", name);
        }

        WsdlTestSuite testSuite = project.addNewTestSuite(name);
        UISupport.showDesktopPanel(testSuite);
        return testSuite;
    }
}
