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

package com.eviware.soapui.impl.wsdl.actions.mockresponse;

import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Deletes a MockResponse from its MockOperation
 * 
 * @author ole.matzura
 */

public class DeleteMockResponseAction extends AbstractSoapUIAction<MockResponse>
{
	public DeleteMockResponseAction()
	{
		super( "Delete", "Deletes this MockResponse" );
	}

	public void perform( MockResponse mockResponse, Object param )
	{
		if( UISupport.confirm( "Delete MockResponse [" + mockResponse.getName() + "] from MockOperation ["
				+ mockResponse.getMockOperation().getName() + "]", getName() ) )
		{
			MockOperation operation = mockResponse.getMockOperation();
			operation.removeMockResponse( mockResponse );
		}
	}
}
