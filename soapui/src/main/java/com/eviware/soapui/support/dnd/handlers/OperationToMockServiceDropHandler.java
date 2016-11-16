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
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.actions.operation.AddOperationToMockServiceAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.support.action.SoapUIAction;

public class OperationToMockServiceDropHandler extends
        AbstractAfterModelItemDropHandler<WsdlOperation, WsdlMockService> {
    public OperationToMockServiceDropHandler() {
        super(WsdlOperation.class, WsdlMockService.class);
    }

    @Override
    boolean canCopyAfter(WsdlOperation source, WsdlMockService target) {
        return source.getInterface().getProject() == target.getProject();
    }

    @Override
    boolean canMoveAfter(WsdlOperation source, WsdlMockService target) {
        return canCopyAfter(source, target);
    }

    @Override
    boolean copyAfter(WsdlOperation source, WsdlMockService target) {
        SoapUIAction<WsdlOperation> action = SoapUI.getActionRegistry().getAction(
                AddOperationToMockServiceAction.SOAPUI_ACTION_ID);
        AddOperationToMockServiceAction a = (AddOperationToMockServiceAction) action;

        return a.addOperationToMockService(source, target);
    }

    @Override
    boolean moveAfter(WsdlOperation source, WsdlMockService target) {
        return copyAfter(source, target);
    }

    @Override
    String getCopyAfterInfo(WsdlOperation source, WsdlMockService target) {
        return "Add MockOperation for [" + source.getName() + "] to MockService [" + target.getName() + "]";
    }

    @Override
    String getMoveAfterInfo(WsdlOperation source, WsdlMockService target) {
        return getCopyAfterInfo(source, target);
    }

}
