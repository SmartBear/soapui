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

package com.eviware.soapui.impl.actions.multi;

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.support.AbstractSoapUIMultiAction;

public class MultiTestStepDisableAction extends AbstractSoapUIMultiAction<ModelItem> {
    public static final String SOAPUI_ACTION_ID = "MultiTestStepDisableAction";

    public MultiTestStepDisableAction() {
        super(SOAPUI_ACTION_ID, "Disable", "Disables the selected items");
    }

    public void perform(ModelItem[] targets, Object param) {
        for (ModelItem target : targets) {
            if (target instanceof WsdlTestStep) {
                ((WsdlTestStep) target).setDisabled(true);
            } else if (target instanceof WsdlTestCase) {
                ((WsdlTestCase) target).setDisabled(true);
            } else if (target instanceof WsdlTestSuite) {
                ((WsdlTestSuite) target).setDisabled(true);
            }
        }
    }

    public boolean applies(ModelItem target) {
        return ((target instanceof WsdlTestStep) && !((WsdlTestStep) target).isDisabled())
                || ((target instanceof WsdlTestCase) && !((WsdlTestCase) target).isDisabled())
                || ((target instanceof WsdlTestSuite) && !((WsdlTestSuite) target).isDisabled());
    }
}
