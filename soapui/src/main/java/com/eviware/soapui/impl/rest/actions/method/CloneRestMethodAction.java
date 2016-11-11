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

package com.eviware.soapui.impl.rest.actions.method;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Clones a RestMethod
 *
 * @author Dain Nilsson
 */

public class CloneRestMethodAction extends AbstractSoapUIAction<RestMethod> {
    public static final String SOAPUI_ACTION_ID = "CloneRestMethodAction";

    public CloneRestMethodAction() {
        super("Clone Method", "Creates a copy of this Method");
    }

    public void perform(RestMethod method, Object param) {
        String name = UISupport.prompt("Specify name of cloned Method", "Clone Method", "Copy of " + method.getName());
        if (name == null) {
            return;
        }

        RestMethod newMethod = method.getOperation().cloneMethod(method, name);
        UISupport.selectAndShow(newMethod);
    }
}
