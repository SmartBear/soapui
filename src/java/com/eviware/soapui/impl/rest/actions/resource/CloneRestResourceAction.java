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
 * Clones a WsdlRequest
 * 
 * @author Ole.Matzura
 */

public class CloneRestResourceAction extends AbstractSoapUIAction<RestResource>
{
	public static final String SOAPUI_ACTION_ID = "CloneRestResourceAction";

	public CloneRestResourceAction()
	{
		super( "Clone Resource", "Creates a copy of this Resource" );
	}

	public void perform( RestResource resource, Object param )
	{
		String name = UISupport.prompt( "Specify name of cloned Resource", "Clone Resource", "Copy of "
				+ resource.getName() );
		if( name == null )
			return;

		RestResource newResource = resource.getResourceContainer().cloneResource( resource, name );
		UISupport.selectAndShow( newResource );
	}
}
