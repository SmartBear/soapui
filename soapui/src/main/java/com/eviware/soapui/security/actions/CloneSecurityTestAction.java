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

import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Clones a SecurityTest
 *
 * @author Ole.Matzura
 */

public class CloneSecurityTestAction extends AbstractSoapUIAction<SecurityTest> {
    public CloneSecurityTestAction() {
        super("Clone SecurityTest", "Clones this SecurityTest");
    }

    public void perform(SecurityTest securityTest, Object param) {
        String name = UISupport.prompt("Specify name of cloned SecurityTest", "Clone SecurityTest", "Copy of "
                + securityTest.getName());
        if (StringUtils.isNullOrEmpty(name)) {
            return;
        }

        while (securityTest.getTestCase().getSecurityTestByName(name.trim()) != null) {
            name = UISupport.prompt("Specify unique name of SecurityTest", "Clone SecurityTest", name);
            if (StringUtils.isNullOrEmpty(name)) {
                return;
            }
        }

        SecurityTest newSecurityTest = securityTest.getTestCase().cloneSecurityTest(securityTest, name);
        UISupport.selectAndShow(newSecurityTest);
    }
}
