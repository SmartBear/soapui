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
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.actions.request.AddRequestToMockServiceAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.SoapUIAction;

public class RequestToMockOperationDropHandler extends
        AbstractAfterModelItemDropHandler<WsdlRequest, WsdlMockOperation> {
    public RequestToMockOperationDropHandler() {
        super(WsdlRequest.class, WsdlMockOperation.class);
    }

    @Override
    boolean canCopyAfter(WsdlRequest source, WsdlMockOperation target) {
        return source.getOperation() == target.getOperation();
    }

    @Override
    boolean canMoveAfter(WsdlRequest source, WsdlMockOperation target) {
        return source.getOperation() == target.getOperation();
    }

    @Override
    boolean copyAfter(WsdlRequest source, WsdlMockOperation target) {
        return addRequestToMockOperation(source, target);
    }

    private boolean addRequestToMockOperation(WsdlRequest request, WsdlMockOperation mockOperation) {
        if (!UISupport.confirm("Add request to MockOperation [" + mockOperation.getName() + "]", "Add Request")) {
            return false;
        }

        SoapUIAction<WsdlRequest> action = SoapUI.getActionRegistry().getAction(
                AddRequestToMockServiceAction.SOAPUI_ACTION_ID);
        ((AddRequestToMockServiceAction) action).perform(request, mockOperation);
        return true;
    }

    @Override
    boolean moveAfter(WsdlRequest source, WsdlMockOperation target) {
        return addRequestToMockOperation(source, target);
    }

    @Override
    String getCopyAfterInfo(WsdlRequest source, WsdlMockOperation target) {
        return "Add Request [" + source.getName() + "] to MockOperation [" + target.getName() + "]";
    }

    @Override
    String getMoveAfterInfo(WsdlRequest source, WsdlMockOperation target) {
        return getCopyAfterInfo(source, target);
    }
}
