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
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormDialog;

/**
 * Action for creating a new top-level REST resource.
 * 
 * @author Ole.Matzura
 */

public class NewRestResourceAction extends NewRestResourceActionBase<RestService>
{
	public static final String SOAPUI_ACTION_ID = "NewRestResourceAction";
	public static final MessageSupport messages = MessageSupport.getMessages( NewRestResourceAction.class );
	public static final String CONFIRM_DIALOG_TITLE = "New Child Resource";

	public NewRestResourceAction()
	{
		super( messages.get( "title" ), messages.get( "description" ) );
	}

	protected RestResource createRestResource( RestService service, String path, XFormDialog dialog )
	{
		RestResource possibleParent = null;
		String strippedPath = null;
		for( String endpoint : service.getEndpoints() )
		{
			if (path.startsWith( endpoint + "/"))
			{
				strippedPath = path.substring(endpoint.length());
			}
		}
		if( strippedPath == null )
		{
			strippedPath = path.startsWith( service.getBasePath() ) ? path : service.getBasePath() + path;
		}


		for( RestResource resource : service.getAllResources() )
		{
			if( strippedPath.startsWith( resource.getFullPath() ) )
			{
				int c = 0;
				for( ; c < resource.getChildResourceCount(); c++ )
				{
					if( strippedPath.startsWith( resource.getChildResourceAt( c ).getFullPath() ) )
					{
						break;
					}
				}

				// found subresource?
				if( c != resource.getChildResourceCount() )
				{
					continue;
				}

				possibleParent = resource;
				break;
			}
		}

		if( possibleParent != null
				&& UISupport.confirm( "Create resource as child to [" + possibleParent.getName() + "]",
				CONFIRM_DIALOG_TITLE ) )
		{
			// adjust path
			if( strippedPath.length() > 0 && possibleParent.getFullPath().length() > 0 )
			{
				strippedPath = strippedPath.substring( possibleParent.getFullPath().length() + 1 );
			}
			return possibleParent.addNewChildResource( extractNameFromPath( strippedPath ), strippedPath );
		}
		else
		{
			return service.addNewResource( extractNameFromPath( path ), path );
		}

	}



}
