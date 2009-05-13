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

public class NewRestChildResourceAction extends NewRestResourceActionBase<RestResource>
{
	public static final String SOAPUI_ACTION_ID = "NewRestChildResourceAction";
	public static final MessageSupport messages = MessageSupport.getMessages( NewRestChildResourceAction.class );
	private XFormDialog dialog;

	public NewRestChildResourceAction()
	{
		super( messages.get( "title" ), messages.get( "description" ) );
	}

	protected RestResource createRestResource( RestResource parentResource, String path, XFormDialog dialog )
	{
		RestResource possibleParent = null;
		String p = parentResource.getFullPath() + path;

		for( RestResource resource : parentResource.getAllChildResources() )
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
			resource = parentResource.addNewChildResource( dialog.getValue( Form.RESOURCENAME ), path );
		}

		return resource;
	}
}