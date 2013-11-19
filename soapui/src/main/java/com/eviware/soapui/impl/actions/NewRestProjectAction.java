/*
 *  SoapUI, copyright (C) 2004-2013 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.actions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.RestURIParser;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestURIParserImpl;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.ModelItemNamer;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;

/**
 * Action class to create new REST project.
 *
 * @author Shadid Chowdhury
 */

public class NewRestProjectAction extends AbstractSoapUIAction<WorkspaceImpl>
{
	public static final String SOAPUI_ACTION_ID = "NewRestProjectAction";

	private static final String DEFAULT_PROJECT_NAME = "REST Project";
	private static final MessageSupport messages = MessageSupport.getMessages( NewRestProjectAction.class );


	private RestUriDialogHandler dialogBuilder = new RestUriDialogHandler();
	private XFormDialog dialog;


	public NewRestProjectAction()
	{
		super( messages.get( "Title" ), messages.get( "Description" ) );
	}


	public void perform( WorkspaceImpl workspace, Object param )
	{
		dialog = dialogBuilder.buildDialog( messages, new AbstractAction( "Import WADL..." )
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				dialog.setVisible( false );
				SoapUI.getActionRegistry().getAction( NewWadlProjectAction.SOAPUI_ACTION_ID ).perform( SoapUI.getWorkspace(), null );
			}
		} );
		while( dialog.show() )
		{
			WsdlProject project = null;
			try
			{
				String uri = dialogBuilder.getUri();
				if( uri != null )
				{
					project = workspace.createProject( ModelItemNamer.createName( DEFAULT_PROJECT_NAME, workspace.getProjectList() ), null );
					createRestService( project, uri );
				}
				// If there is no exception or error we break out
				break;

			}
			catch( Exception ex )
			{
				UISupport.showErrorMessage( ex.getMessage() );
				if( project != null )
				{
					workspace.removeProject( project );
				}
				dialogBuilder.resetUriField();
			}
		}
	}

	protected void createRestService( WsdlProject project, String URI ) throws MalformedURLException
	{
		if( StringUtils.isNullOrEmpty( URI ) )
		{
			return;
		}

		RestURIParser restURIParser = new RestURIParserImpl( URI );
		RestParamsPropertyHolder params = new XmlBeansRestParamsTestPropertyHolder( null,
				RestParametersConfig.Factory.newInstance() );

		String resourcePath = restURIParser.getResourcePath();
		String resourceName = restURIParser.getResourceName();
		String host = restURIParser.getEndpoint();

		RestService restService = ( RestService )project.addNewInterface( host, RestServiceFactory.REST_TYPE );
		restService.addEndpoint( restURIParser.getEndpoint() );
		RestResource restResource = restService.addNewResource( resourceName, resourcePath );

		RestMethod restMethod = addNewMethod( restResource, resourceName );

		extractAndFillParameters( URI, params );
		copyParameters( params, restResource.getParams() );

		RestRequest restRequest = addNewRequest( restMethod );
		UISupport.select( restRequest );
		UISupport.showDesktopPanel( restRequest );

	}

	protected void extractAndFillParameters( String URI, RestParamsPropertyHolder params )
	{
		// This does lot of magic including extracting and filling up parameters on the params
		RestUtils.extractParams( URI, params, false, false );
	}

	//TODO: In advanced version we have to apply filtering like which type of parameter goes to which location
	protected void copyParameters( RestParamsPropertyHolder srcParams, RestParamsPropertyHolder destinationParams )
	{
		for( int i = 0; i < srcParams.size(); i++ )
		{
			RestParamProperty prop = srcParams.getPropertyAt( i );

			destinationParams.addParameter( prop );

		}
	}

	protected RestMethod addNewMethod( RestResource restResource, String methodName )
	{
		RestMethod restMethod = restResource.addNewMethod( methodName );
		restMethod.setMethod( RestRequestInterface.RequestMethod.GET );
		return restMethod;
	}

	protected RestRequest addNewRequest( RestMethod restMethod )
	{
		return restMethod.addNewRequest( "Request " + ( restMethod.getRequestCount() + 1 ) );
	}
}