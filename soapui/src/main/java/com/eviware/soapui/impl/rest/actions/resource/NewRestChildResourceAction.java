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
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.support.MessageSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Action for adding a new child REST resource.
 * 
 * @author Ole.Matzura
 */

public class NewRestChildResourceAction extends NewRestResourceActionBase<RestResource>
{
	public static final String SOAPUI_ACTION_ID = "NewRestChildResourceAction";
	public static final MessageSupport messages = MessageSupport.getMessages( NewRestChildResourceAction.class );

	public NewRestChildResourceAction()
	{
		super( messages.get( "title" ), messages.get( "description" ) );
	}


	@Override
	protected List<RestResource> getResourcesFor( RestResource item )
	{
		List<RestResource> returnValue = new ArrayList<RestResource>( );
		returnValue.add(item);
		returnValue.addAll( Arrays.asList( item.getAllChildResources() ));
		return returnValue;
	}

	@Override
	protected RestResource addResourceTo( RestResource parentResource, String name, String path )
	{
		return parentResource.addNewChildResource( name, path );
	}


}
