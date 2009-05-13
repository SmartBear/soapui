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

package com.eviware.soapui.impl.wsdl.actions.mockoperation;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Deletes a WsdlMockOperation from its WsdlMockService
 * 
 * @author Ole.Matzura
 */

public class DeleteMockOperationAction extends AbstractSoapUIAction<WsdlMockOperation>
{
	public DeleteMockOperationAction()
	{
		super( "Remove", "Removes this MockOperation" );
	}

	public void perform( WsdlMockOperation mockOperation, Object param )
	{
		if( UISupport.confirm( "Remove MockOperation [" + mockOperation.getName() + "] from MockService ["
				+ mockOperation.getMockService().getName() + "]", "Remove MockOperation" ) )
		{
			WsdlMockService mockService = mockOperation.getMockService();
			mockService.removeMockOperation( mockOperation );
		}
	}
}
