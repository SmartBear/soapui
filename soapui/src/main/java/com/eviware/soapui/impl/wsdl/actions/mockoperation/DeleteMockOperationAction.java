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

package com.eviware.soapui.impl.wsdl.actions.mockoperation;

import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Deletes a WsdlMockOperation from its WsdlMockService
 * 
 * @author Ole.Matzura
 */

public class DeleteMockOperationAction extends AbstractSoapUIAction<MockOperation>
{
	public DeleteMockOperationAction()
	{
		super( "Remove", "Removes this node" );
	}

	public void perform( MockOperation mockOperation, Object param )
	{

		String opEquivalentName = mockOperation instanceof RestMockAction ? "Mock Action" : "Mock Operation";

		if( UISupport.confirm( "Remove "+ opEquivalentName +" [" + mockOperation.getName() + "] from MockService ["
				+ mockOperation.getMockService().getName() + "]", "Remove "+ opEquivalentName ) )
		{
			MockService mockService = mockOperation.getMockService();
			mockService.removeMockOperation( mockOperation );
		}
	}
}
