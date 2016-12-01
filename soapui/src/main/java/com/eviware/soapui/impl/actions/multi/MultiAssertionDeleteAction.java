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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIMultiAction;

import java.util.HashSet;
import java.util.Set;

public class MultiAssertionDeleteAction extends AbstractSoapUIMultiAction<ModelItem> {
    public static final String SOAPUI_ACTION_ID = "MultiAssertionDeleteAction";

    public MultiAssertionDeleteAction() {
        super(SOAPUI_ACTION_ID, "Delete Assertions", "Delete selected Assertions");
    }

    public void perform(ModelItem[] targets, Object param) {
        if (UISupport.confirm("Delete selected Assertions?", "Delete Assertions")) {
            if (SoapUI.getTestMonitor().hasRunningTestCase((TestCase) targets[0].getParent().getParent())) {
                UISupport.showInfoMessage("Can not remove assertion(s) while test case is running");
                return;
            }
            // remove duplicates
            Set<TestAssertion> assertions = new HashSet<TestAssertion>();

            for (ModelItem target : targets) {
                assertions.add((TestAssertion) target);
            }

            for (TestAssertion assertion : assertions) {
                ((Assertable) assertion.getParent()).removeAssertion(assertion);
            }
        }
    }


    public boolean applies(ModelItem target) {
        return (target instanceof TestAssertion);
    }

}
