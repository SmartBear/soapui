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

package com.eviware.soapui.impl.wsdl.actions.teststep;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames a WsdlTestStep
 *
 * @author Ole.Matzura
 */

public class RenameTestStepAction extends AbstractSoapUIAction<WsdlTestStep> {
    public RenameTestStepAction() {
        super("Rename", "Renames this TestStep");
    }

    public void perform(WsdlTestStep testStep, Object param) {
        String name = UISupport.prompt("Specify unique name of TestStep", "Rename TestStep", testStep.getName());
        if (name == null || name.equals(testStep.getName())) {
            return;
        }

        while (testStep.getTestCase().getTestStepByName(name.trim()) != null) {
            name = UISupport.prompt("Specify unique name of TestStep", "Rename TestStep", testStep.getName());
            if (name == null || name.equals(testStep.getName())) {
                return;
            }
        }

        testStep.setName(name);
    }
}
