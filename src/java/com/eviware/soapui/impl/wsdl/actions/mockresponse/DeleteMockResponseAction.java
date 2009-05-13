/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.actions.mockresponse;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Deletes a WsdlMockResponse from its WsdlMockOperation
 * 
 * @author ole.matzura
 */

public class DeleteMockResponseAction extends AbstractSoapUIAction<WsdlMockResponse>
{
	public DeleteMockResponseAction()
	{
		super( "Delete", "Deletes this MockResponse" );
	}

	public void perform( WsdlMockResponse mockResponse, Object param )
	{
		if( UISupport.confirm( "Delete MockResponse [" + mockResponse.getName() + "] from MockOperation ["
				+ mockResponse.getMockOperation().getName() + "]", getName() ) )
		{
			WsdlMockOperation operation = ( WsdlMockOperation )mockResponse.getMockOperation();
			operation.removeMockResponse( mockResponse );
		}
	}
}
