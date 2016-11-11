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

package com.eviware.soapui.support.resolver;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;

public class RunTestCaseRemoveResolver implements Resolver {
    private WsdlTestStep testStep;
    private boolean resolved;

    public RunTestCaseRemoveResolver(WsdlTestStep testStep) {
        this.testStep = testStep;
    }

    public void perform(WsdlTestStep target, Object param) {
        target.setDisabled(true);
    }

    @Override
    public String toString() {
        return getDescription();
    }

    public String getDescription() {
        return "Disable Run Test step";
    }

    public String getResolvedPath() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isResolved() {
        return resolved;
    }

    public boolean resolve() {

        if (UISupport.confirm("Are you sure to disable test step?", "Disable Test Step") && testStep != null) {
            testStep.setDisabled(true);
            resolved = true;
        }
        return resolved;
    }
}
