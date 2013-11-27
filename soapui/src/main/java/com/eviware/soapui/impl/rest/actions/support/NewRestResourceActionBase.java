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

package com.eviware.soapui.impl.rest.actions.support;

import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Base class for action classes
 *
 * @author Ole.Matzura
 */

public abstract class NewRestResourceActionBase<T extends ModelItem> extends AbstractSoapUIAction<T>
{
	public static final String CONFIRM_DIALOG_TITLE = "New Child Resource";

	private XFormDialog dialog;
	public static final MessageSupport messages = MessageSupport.getMessages( NewRestResourceActionBase.class );

	public NewRestResourceActionBase( String title, String description )
	{
		super( title, description );
	}

	public void perform( T parent, Object param )
	{
		if( dialog == null )
		{
			dialog = ADialogBuilder.buildDialog( Form.class );
		}
		else
		{
			dialog.setValue( Form.RESOURCEPATH, "" );
		}


		if( param instanceof URL )
		{
			XmlBeansRestParamsTestPropertyHolder params = new XmlBeansRestParamsTestPropertyHolder( null, RestParametersConfig.Factory.newInstance() );
			String path = RestUtils.extractParams( param.toString(), params, false );
			dialog.setValue( Form.RESOURCEPATH, path );
		}

		if( dialog.show() )
		{
			String path = dialog.getValue( Form.RESOURCEPATH );
			RestResource resource = createRestResource( parent, path );
			RestUtils.extractParams( dialog.getValue( Form.RESOURCEPATH ), resource.getParams(), false );
			resource.setPath(removeParametersFrom( resource.getPath() ));

			createMethodAndRequestFor( resource );
		}

	}

	protected RestResource createRestResource( T item, String path )
	{
		RestResource possibleParent = null;
		String pathWithoutEndpoint = removeEndpointFrom( path );

		for( RestResource resource : getResourcesFor(item) )
		{
			if( pathWithoutEndpoint.startsWith( resource.getFullPath() + "/" ) )
			{
				int c = 0;
				for( ; c < resource.getChildResourceCount(); c++ )
				{
					if( pathWithoutEndpoint.startsWith( resource.getChildResourceAt( c ).getFullPath() + "/" ) )
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
			String strippedPath = pathWithoutEndpoint;
			if( pathWithoutEndpoint.length() > 0 && possibleParent.getFullPath().length() > 0 )
			{
				strippedPath = pathWithoutEndpoint.substring( possibleParent.getFullPath().length() + 1 );
			}
			return possibleParent.addNewChildResource( extractNameFromPath( strippedPath ), strippedPath );
		}
		else
		{
			String pathWithoutLeadingSlash = pathWithoutEndpoint.startsWith( "/" ) ? pathWithoutEndpoint.substring(1) :
					pathWithoutEndpoint;
			return addResourceTo( item, extractNameFromPath( pathWithoutEndpoint ), pathWithoutLeadingSlash );
		}

	}

	protected abstract List<RestResource> getResourcesFor( T item );

	protected abstract RestResource addResourceTo( T item, String name, String path );

	private String removeEndpointFrom( String path )
	{
		try
		{
			return new URL(path).getPath();
		}
		catch( MalformedURLException ignore )
		{
			return path;
		}
	}

	private String extractNameFromPath( String path )
	{
		String strippedPath = removeParametersFrom( path );
		String[] items = strippedPath.split( "/" );
		return items.length == 0 ? "" : items[items.length - 1];
	}

	private String removeParametersFrom( String path )
	{
		String strippedPath;
		if (path.contains("?") || path.contains(";"))
		{
			int parametersIndex = findParametersIndex( path );
			strippedPath = path.substring(0, parametersIndex);
		}
		else
		{
			strippedPath = path;
		}
		return strippedPath;
	}

	private int findParametersIndex( String path )
	{
		int semicolonIndex = path.indexOf( ';' );
		int questionMarkIndex = path.indexOf( '?' );
		return Math.min( semicolonIndex == -1 ? Integer.MAX_VALUE : semicolonIndex,
				questionMarkIndex == -1  ? Integer.MAX_VALUE : questionMarkIndex);
	}

	private void createMethodAndRequestFor( RestResource resource )
	{
		RestMethod method = resource.addNewMethod( "Method " + (resource.getRestMethodCount() + 1) );
		method.setMethod( RestRequestInterface.RequestMethod.GET );
		RestRequest request = method.addNewRequest( "Request " + ( method.getRequestCount() + 1 ) );
		UISupport.select( request );
		UISupport.showDesktopPanel( request );
	}

	//TODO: Make this non-inner!

	public enum ParamLocation
	{
		RESOURCE, METHOD

	}
	@AForm(name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWRESTSERVICE_HELP_URL, icon = UISupport.TOOL_ICON_PATH)
	public interface Form
	{

		@AField(description = "Form.ServiceUrl.Description", type = AFieldType.STRING)
		public final static String RESOURCEPATH = messages.get( "Form.ResourcePath.Label" );

	}
}
