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

package com.eviware.soapui.impl.wsdl.actions.mockservice;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Removes a WsdlMockService from its WsdlProject
 * 
 * @author Ole.Matzura
 */

public class DeleteMockServiceAction extends AbstractSoapUIAction<WsdlMockService>
{
	public DeleteMockServiceAction()
	{
		super( "Remove", "Removes this MockService from the MockService" );
	}

	public void perform( WsdlMockService mockService, Object param )
	{
		if( SoapUI.getMockEngine().hasRunningMock( mockService ) )
		{
			UISupport.showErrorMessage( "Cannot remove MockService while mocks are running" );
			return;
		}

		if( UISupport.confirm( "Remove MockService [" + mockService.getName() + "] from MockService",
				"Remove MockService" ) )
		{
			( ( WsdlProject )mockService.getProject() ).removeMockService( mockService );
		}
	}

}
