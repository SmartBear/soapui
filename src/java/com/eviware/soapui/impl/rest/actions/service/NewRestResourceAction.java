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

package com.eviware.soapui.impl.rest.actions.service;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormDialog;

/**
 * Actions for importing an existing soapUI project file into the current
 * workspace
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

	protected RestResource createRestResource( RestService service, String path, XFormDialog dialog )
	{
		RestResource possibleParent = null;
		String p = service.getBasePath() + path;

		for( RestResource resource : service.getAllResources() )
		{
			if( p.startsWith( resource.getFullPath() ) )
			{
				int c = 0;
				for( ; c < resource.getChildResourceCount(); c++ )
				{
					if( p.startsWith( resource.getChildResourceAt( c ).getFullPath() ) )
						break;
				}

				// found subresource?
				if( c != resource.getChildResourceCount() )
					continue;

				possibleParent = resource;
				break;
			}
		}

		RestResource resource;

		if( possibleParent != null
				&& UISupport.confirm( "Create resource as child to [" + possibleParent.getName() + "]",
						"New Child Resource" ) )
		{
			// adjust path
			path = path.substring( p.length() - possibleParent.getFullPath().length() - 1 );
			resource = possibleParent.addNewChildResource( dialog.getValue( Form.RESOURCENAME ), path );
		}
		else
		{
			resource = service.addNewResource( dialog.getValue( Form.RESOURCENAME ), path );
		}

		return resource;
	}

	@Override
	protected RestMethod createRestMethod( RestResource resource, XFormDialog dialog )
	{
		RestMethod method = resource.addNewMethod( dialog.getValue( Form.RESOURCENAME ) );
		method.setMethod( RestRequestInterface.RequestMethod.GET );
		return method;
	}

}