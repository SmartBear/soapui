/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.mock;

import java.util.Vector;

import com.eviware.soapui.impl.support.BaseMockResult;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.WsdlMockResultMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.actions.ShowMessageExchangeAction;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.action.swing.DefaultActionList;

/**
 * The result of a handled WsdlMockRequest
 * 
 * @author ole.matzura
 */

public class WsdlMockResult extends BaseMockResult<WsdlMockRequest, WsdlMockOperation, WsdlMockResponse>
{
	public WsdlMockResult( WsdlMockRequest request ) throws Exception
	{
		super(request);
	}

	public Vector<?> getRequestWssResult()
	{
		return getMockRequest().getWssResult();
	}

	@Override
	public ActionList getActions()
	{
		ActionList actionList = super.getActions();

		actionList.setDefaultAction( createMessageExchangeAction() );

		return actionList;
	}

	private ShowMessageExchangeAction createMessageExchangeAction()
	{
		return new ShowMessageExchangeAction( createMessageExchange(), "MockResult" );
	}

	private WsdlMockResultMessageExchange createMessageExchange()
	{
		WsdlMockResponse mockResponse = ( WsdlMockResponse )getMockResponse();
		return new WsdlMockResultMessageExchange( this, mockResponse );
	}


}
