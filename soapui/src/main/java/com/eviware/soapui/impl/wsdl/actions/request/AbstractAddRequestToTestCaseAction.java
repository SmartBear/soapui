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

package com.eviware.soapui.impl.wsdl.actions.request;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.actions.request.AddRestRequestToTestCaseAction;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.actions.support.AbstractAddToTestCaseAction;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.dnd.handlers.DragAndDropSupport;

import java.util.HashSet;

public abstract class AbstractAddRequestToTestCaseAction<T extends AbstractHttpRequest> extends AbstractAddToTestCaseAction<T> {
    public AbstractAddRequestToTestCaseAction(String name, String description) {
        super(name, description);
    }

    public static AbstractAddRequestToTestCaseAction findActionForRequest(AbstractHttpRequest source) {
        String actionId = source instanceof RestRequest ? AddRestRequestToTestCaseAction.SOAPUI_ACTION_ID :
                AddRequestToTestCaseAction.SOAPUI_ACTION_ID;
        return (AbstractAddRequestToTestCaseAction)
                SoapUI.getActionRegistry().getAction(actionId);
    }

    @SuppressWarnings("unchecked")
    public static boolean addRequestToTestCase(AbstractHttpRequest source, TestCase testCase, int index) {
        if (!UISupport.confirm("Add Request [" + source.getName() + "] to TestCase [" + testCase.getName() + "]",
                "Add Request to TestCase")) {
            return false;
        }

        Project targetProject = testCase.getTestSuite().getProject();
        if (targetProject != source.getOperation().getInterface().getProject()) {
            HashSet<Interface> requiredInterfaces = new HashSet<Interface>();
            requiredInterfaces.add(source.getOperation().getInterface());

            if (!DragAndDropSupport
                    .importRequiredInterfaces(targetProject, requiredInterfaces, "Add Request to TestCase")) {
                return false;
            }
        }
        // unchecked, but hard to avoid that
        return findActionForRequest(source).addRequest(testCase, source, index);
    }

    public abstract boolean addRequest(TestCase testCase, T request, int position);

}
