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
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Toggles the disabled state of WsdlTestStep
 *
 * @author Ole.Matzura
 */

public class ToggleDisableAssertionAction extends AbstractSoapUIAction<WsdlMessageAssertion> {
    public static final String SOAPUI_ACTION_ID = "ToggleDisableAssertionAction";

    public ToggleDisableAssertionAction() {
        super("Disable", "Disables this TestCase");
    }

    public void perform(WsdlMessageAssertion testCase, Object param) {
        testCase.setDisabled(!testCase.isDisabled());
    }
}
