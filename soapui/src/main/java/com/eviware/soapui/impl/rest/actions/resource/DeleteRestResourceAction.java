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

package com.eviware.soapui.impl.rest.actions.resource;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Deletes a RestResource from its containing Service or Resource
 * 
 * @author Ole.Matzura
 */

public class DeleteRestResourceAction extends AbstractSoapUIAction<RestResource>
{
	public DeleteRestResourceAction()
	{
		super( "Delete", "Deletes this Resource" );
	}

	public void perform( RestResource resource, Object param )
	{
		if( UISupport.confirm( "Delete Resource [" + resource.getName() + "] from ["
				+ resource.getResourceContainer().getName() + "]. This will also delete all the child resources and test steps!", "Delete Resource" ) )
		{
			resource.getResourceContainer().deleteResource( resource );
		}
	}
}
