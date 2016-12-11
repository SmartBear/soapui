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

package com.eviware.soapui.impl.wsdl.mock;

import com.eviware.soapui.impl.support.BaseMockResult;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.WsdlMockResultMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.actions.ShowMessageExchangeAction;
import com.eviware.soapui.support.action.swing.ActionList;

import java.util.Vector;

/**
 * The result of a handled WsdlMockRequest
 *
 * @author ole.matzura
 */

public class WsdlMockResult extends BaseMockResult<WsdlMockRequest, WsdlMockOperation> {
    public WsdlMockResult(WsdlMockRequest request) {
        super(request);
    }

    public Vector<?> getRequestWssResult() {
        return getMockRequest().getWssResult();
    }

    @Override
    public ActionList getActions() {
        ActionList actionList = super.getActions();

        actionList.setDefaultAction(createMessageExchangeAction());

        return actionList;
    }

    private ShowMessageExchangeAction createMessageExchangeAction() {
        return new ShowMessageExchangeAction(createMessageExchange(), "MockResult");
    }

    private WsdlMockResultMessageExchange createMessageExchange() {
        WsdlMockResponse mockResponse = (WsdlMockResponse) getMockResponse();
        return new WsdlMockResultMessageExchange(this, mockResponse);
    }


}
