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

import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames the specified WsdlMockResponse
 * 
 * @author ole.matzura
 */

public class RenameMockResponseAction extends AbstractSoapUIAction<WsdlMockResponse>
{
	public RenameMockResponseAction()
	{
		super( "Rename", "Renames this MockResponse" );
	}

	public void perform( WsdlMockResponse mockResponse, Object param )
	{
		String name = UISupport.prompt( "Specify name of MockResponse", getName(), mockResponse.getName() );
		if( name == null || name.equals( mockResponse.getName() ) )
			return;

		mockResponse.setName( name );
	}
}
