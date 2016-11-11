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

package com.eviware.soapui.support.dnd.handlers;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.mockservice.CloneMockServiceAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIAction;

public class MockServiceToProjectDropHandler extends AbstractAfterModelItemDropHandler<WsdlMockService, WsdlProject> {
    public MockServiceToProjectDropHandler() {
        super(WsdlMockService.class, WsdlProject.class);
    }

    @Override
    boolean canCopyAfter(WsdlMockService source, WsdlProject target) {
        return true;
    }

    @Override
    boolean canMoveAfter(WsdlMockService source, WsdlProject target) {
        return source.getProject() != target;
    }

    @Override
    boolean copyAfter(WsdlMockService source, WsdlProject target) {
        SoapUIAction<WsdlMockService> action = SoapUI.getActionRegistry().getAction(
                CloneMockServiceAction.SOAPUI_ACTION_ID);
        CloneMockServiceAction a = (CloneMockServiceAction) action;

        String name = UISupport.prompt("Specify name for copied MockService", "Copy MockService",
                "Copy of " + source.getName());
        if (name == null) {
            return false;
        }

        if (source.getProject() == target) {
            a.cloneMockServiceWithinProject(source, name, target, source.getDescription());
        } else {
            a.cloneToAnotherProject(source, target.getName(), name, source.getDescription());
        }

        return true;
    }

    @Override
    boolean moveAfter(WsdlMockService source, WsdlProject target) {
        SoapUIAction<WsdlMockService> action = SoapUI.getActionRegistry().getAction(
                CloneMockServiceAction.SOAPUI_ACTION_ID);
        CloneMockServiceAction a = (CloneMockServiceAction) action;

        String name = UISupport.prompt("Specify name for moved MockService", "Move MockService", source.getName());
        if (name == null) {
            return false;
        }

        if (a.cloneToAnotherProject(source, target.getName(), name, source.getDescription()) == null) {
            return false;
        }

        source.getProject().removeMockService(source);
        return true;
    }

    @Override
    String getCopyAfterInfo(WsdlMockService source, WsdlProject target) {
        return "Copy MockService [" + source.getName() + "] to Project [" + target.getName() + "]";
    }

    @Override
    String getMoveAfterInfo(WsdlMockService source, WsdlProject target) {
        return "Move MockService [" + source.getName() + "] to Project [" + target.getName() + "]";
    }

}
