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
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Clones the specified WsdlMessageAssertion
 *
 * @author ole.matzura
 */

public class CloneAssertionAction extends AbstractSoapUIAction<WsdlMessageAssertion> {
    public static final String SOAPUI_ACTION_ID = "CloneAssertionAction";

    public CloneAssertionAction() {
        super("Clone", "Clones this assertion");
    }

    public void perform(WsdlMessageAssertion target, Object param) {
        String name = target.getName();

        while (target.getName().equals(name)) {
            name = UISupport.prompt("Specify unique name for cloned assertion", "Clone Assertion", target.getName());
            if (name == null) {
                return;
            }
        }

        TestAssertion assertion = target.getAssertable().cloneAssertion(target, name);

        if (assertion.isConfigurable()) {
            assertion.configure();
        }
    }
}
