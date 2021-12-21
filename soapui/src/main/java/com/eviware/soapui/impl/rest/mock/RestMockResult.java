/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
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

package com.eviware.soapui.impl.rest.mock;

import javax.swing.Action;

import com.eviware.soapui.impl.rest.RestMockResultMessageExchange;
import com.eviware.soapui.impl.support.BaseMockResult;
import com.eviware.soapui.impl.wsdl.teststeps.actions.ShowMessageExchangeAction;
import com.eviware.soapui.support.action.swing.ActionList;


public class RestMockResult extends BaseMockResult<RestMockRequest, RestMockAction> {
    
	public RestMockResult(RestMockRequest request) {
        super(request);
    }
	
	@Override
	public ActionList getActions() {
		
		ActionList actionList = super.getActions();
		if(!mockResultContainsResponse())
			return actionList;
		
		actionList.setDefaultAction(createMessageExchangeAction());
		return actionList;
	}

	private boolean mockResultContainsResponse() {
		return getMockResponse() != null;
	}

	private Action createMessageExchangeAction() {
		return new ShowMessageExchangeAction(createMessageExchange(), "MockResult");
	}

	private RestMockResultMessageExchange createMessageExchange() {
		return new RestMockResultMessageExchange(this);
    }

}
