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
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames a WsdlMockOperation
 * 
 * @author Ole.Matzura
 */

public class RenameMockOperationAction extends AbstractSoapUIAction<WsdlMockOperation>
{
	public RenameMockOperationAction()
	{
		super( "Rename", "Renames this MockOperation" );
	}

	public void perform( WsdlMockOperation mockOperation, Object param )
	{
		String name = UISupport.prompt( "Specify name of MockOperation", "Rename MockOperation", mockOperation.getName() );
		if( name == null || name.equals( mockOperation.getName() ) )
			return;

		mockOperation.setName( name );
	}

}
