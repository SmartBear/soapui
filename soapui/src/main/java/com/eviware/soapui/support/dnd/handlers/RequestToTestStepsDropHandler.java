/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.support.dnd.handlers;

import java.util.HashSet;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.actions.request.AddRequestToTestCaseAction;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.tree.nodes.support.WsdlTestStepsModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIAction;

public class RequestToTestStepsDropHandler extends
        AbstractAfterModelItemDropHandler<WsdlRequest, WsdlTestStepsModelItem> {
    public RequestToTestStepsDropHandler() {
        super(WsdlRequest.class, WsdlTestStepsModelItem.class);
    }

    @Override
    boolean canCopyAfter(WsdlRequest source, WsdlTestStepsModelItem target) {
        return true;
    }

    @Override
    boolean canMoveAfter(WsdlRequest source, WsdlTestStepsModelItem target) {
        return true;
    }

    @Override
    boolean copyAfter(WsdlRequest source, WsdlTestStepsModelItem target) {
        return addRequestToTestCase(source, target);
    }

    private boolean addRequestToTestCase(WsdlRequest source, WsdlTestStepsModelItem target) {
        if (!UISupport.confirm("Add Request [" + source.getName() + "] to TestCase [" + target.getTestCase().getName()
                + "]", "Add Request to TestCase")) {
            return false;
        }

        WsdlProject targetProject = target.getTestCase().getTestSuite().getProject();
        if (targetProject != source.getOperation().getInterface().getProject()) {
            HashSet<Interface> requiredInterfaces = new HashSet<Interface>();
            requiredInterfaces.add(source.getOperation().getInterface());

            if (!DragAndDropSupport
                    .importRequiredInterfaces(targetProject, requiredInterfaces, "Add Request to TestCase")) {
                return false;
            }
        }

        SoapUIAction<WsdlRequest> action = SoapUI.getActionRegistry().getAction(
                AddRequestToTestCaseAction.SOAPUI_ACTION_ID);
        return ((AddRequestToTestCaseAction) action).addRequest(target.getTestCase(), source, 0) != null;
    }

    @Override
    boolean moveAfter(WsdlRequest source, WsdlTestStepsModelItem target) {
        return addRequestToTestCase(source, target);
    }

    @Override
    String getCopyAfterInfo(WsdlRequest source, WsdlTestStepsModelItem target) {
        return "Add Request [" + source.getName() + "] to TestCase [" + target.getTestCase().getName() + "]";
    }

    @Override
    String getMoveAfterInfo(WsdlRequest source, WsdlTestStepsModelItem target) {
        return getCopyAfterInfo(source, target);
    }
}
