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

package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Sets the delay to of a WsdlDelayTestStep
 *
 * @author ole.matzura
 */

public class SetWaitTimeAction extends AbstractSoapUIAction<WsdlDelayTestStep> {
    public SetWaitTimeAction() {
        super("Set Delay Time", "Sets the Delay for this DelayStep");
    }

    public void perform(WsdlDelayTestStep target, Object param) {
        String value = UISupport.prompt("Specify delay in milliseconds", "Set Delay",
                String.valueOf(target.getDelayString()));
        if (value != null) {
            try {
                target.setDelayString(value);
            } catch (NumberFormatException e1) {
                UISupport.showErrorMessage(e1);
            }
        }
    }
}
