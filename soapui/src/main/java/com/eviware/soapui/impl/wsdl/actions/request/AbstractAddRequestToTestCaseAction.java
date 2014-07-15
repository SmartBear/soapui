/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
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
