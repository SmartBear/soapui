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

package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Action for adding a new WsdlMockService to a WsdlProject
 * 
 * @author Ole.Matzura
 */

public class AddNewMockServiceAction extends AbstractSoapUIAction<WsdlProject>
{
	public static final String SOAPUI_ACTION_ID = "AddNewMockServiceAction";

	public AddNewMockServiceAction()
	{
		super( "New MockService", "Creates a new MockService in this project" );
		// putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "menu O" ));
	}

	public void perform( WsdlProject target, Object param )
	{
		createMockService( target );
	}

	public WsdlMockService createMockService( WsdlProject project )
	{
		String name = UISupport.prompt( "Specify name of MockService", "New MockService", "MockService "
				+ ( project.getMockServiceCount() + 1 ) );
		if( name == null )
			return null;

		WsdlMockService mockService = project.addNewMockService( name );
		UISupport.select( mockService );

		return mockService;
	}
}
