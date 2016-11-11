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

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIMultiAction;

public class MultiTestStepDeleteAction extends AbstractSoapUIMultiAction<ModelItem> {
    public static final String SOAPUI_ACTION_ID = "MultiTestStepDeleteAction";

    public MultiTestStepDeleteAction() {
        super(SOAPUI_ACTION_ID, "Delete TestSteps", "Delete selected TestSteps");
    }

    public void perform(ModelItem[] targets, Object param) {
        if (UISupport.confirm("Delete selected Test Steps?", "Delete Items")) {
            for (ModelItem target : targets) {
                ((WsdlTestStep) target).getTestCase().removeTestStep((WsdlTestStep) target);
            }
        }
    }

    public boolean applies(ModelItem target) {
        return (target instanceof WsdlTestStep);
    }
}
