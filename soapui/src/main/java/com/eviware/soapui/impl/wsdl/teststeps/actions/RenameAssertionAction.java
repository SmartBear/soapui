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

package com.eviware.soapui.impl.wsdl.teststeps.actions;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames the specified assertion
 *
 * @author ole.matzura
 */

public class RenameAssertionAction extends AbstractSoapUIAction<WsdlMessageAssertion> {
    public RenameAssertionAction() {
        super("Rename", "Renames this assertion");
    }

    public void perform(WsdlMessageAssertion target, Object param) {
        String name = UISupport.prompt("Specify name for this assertion", "Rename Assertion", target.getName());
        if (name == null || name.equals(target.getName())) {
            return;
        }
        while (target.getAssertable().getAssertionByName(name.trim()) != null) {
            name = UISupport.prompt("Specify unique name of Assertion", "Rename Assertion", target.getName());
            if (name == null || name.equals(target.getName())) {
                return;
            }
        }

        target.setName(name);
    }
}
