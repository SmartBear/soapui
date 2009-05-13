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

package com.eviware.soapui.impl.rest.actions.resource;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Renames a WsdlRequest
 * 
 * @author Ole.Matzura
 */

public class RenameRestResourceAction extends AbstractSoapUIAction<RestResource>
{
	public RenameRestResourceAction()
	{
		super( "Rename", "Renames this Resource" );
	}

	public void perform( RestResource request, Object param )
	{
		String name = UISupport.prompt( "Specify new name for Resource", "Rename Resource", request.getName() );
		if( name == null || name.equals( request.getName() ) )
			return;

		request.setName( name );
	}
}