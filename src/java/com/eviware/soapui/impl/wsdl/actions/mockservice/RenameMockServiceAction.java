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

import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames a WsdlMockService
 * 
 * @author Ole.Matzura
 */

public class RenameMockServiceAction extends AbstractSoapUIAction<WsdlMockService>
{
	public RenameMockServiceAction()
	{
		super( "Rename", "Renames this MockService" );
	}

	public void perform( WsdlMockService mockService, Object param )
	{
		String name = UISupport.prompt( "Specify name of MockService", "Rename MockService", mockService.getName() );
		if( name == null || name.equals( mockService.getName() ) )
			return;

		mockService.setName( name );
	}
}
