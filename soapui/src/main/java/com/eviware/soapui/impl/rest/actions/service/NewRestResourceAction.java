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

package com.eviware.soapui.impl.rest.actions.service;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.support.MessageSupport;

import java.util.List;

/**
 * Action for creating a new top-level REST resource.
 * 
 * @author Ole.Matzura
 */

public class NewRestResourceAction extends NewRestResourceActionBase<RestService>
{
	public static final String SOAPUI_ACTION_ID = "NewRestResourceAction";
	public static final MessageSupport messages = MessageSupport.getMessages( NewRestResourceAction.class );

	public NewRestResourceAction()
	{
		super( messages.get( "title" ), messages.get( "description" ) );
	}


	@Override
	protected List<RestResource> getResourcesFor( RestService item )
	{
		return item.getResourceList();
	}

	@Override
	protected RestResource addResourceTo( RestService service, String name, String path )
	{
		return service.addNewResource( name, path );
	}

}
