/*
 *  soapUI, copyright (C) 2004-2013 smartbear.com
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
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
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JTextFieldFormField;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.MalformedURLException;

/**
 * Action class to create new REST project.
 *
 * @author Shadid Chowdhury
 */

public class NewRESTProjectAction extends AbstractSoapUIAction<WorkspaceImpl>
{
	public static final String SOAPUI_ACTION_ID = "NewRESTProjectAction";
	private static final String PROJECT_NAME = "REST Project"; //TODO: configurable or some other intelligent way
	private XFormDialog dialog;

	public static final MessageSupport messages = MessageSupport.getMessages( NewRESTProjectAction.class );
	private KeyListener initialKeyListener;
	private MouseListener initialMouseListener;

	public NewRESTProjectAction()
	{
		super( messages.get( "Title" ), messages.get( "Description" ) );
	}

	public void perform( WorkspaceImpl workspace, Object param )
	{
		if( dialog == null )
		{
			dialog = ADialogBuilder.buildDialog( Form.class );

		}
		dialog.setValue( Form.URI, "http://example.com/resource/path/search?parameter=value" );
		XFormField uriField = dialog.getFormField( Form.URI );
		if (uriField instanceof JTextFieldFormField) {
			JUndoableTextField textField = (( JTextFieldFormField )uriField).getComponent();
			textField.requestFocus();
			textField.setFont( textField.getFont().deriveFont( Font.ITALIC ) );
			textField.setForeground( new Color(170, 170, 170) );
			addListenersTo( textField );
		}

		while( dialog.show() )
		{
			WsdlProject project = null;
			try
			{
				String URI = dialog.getValue( Form.URI ).trim();

				project = workspace.createProject( PROJECT_NAME, null );

				createRestProject( project, URI );

				// If there is no exception or error we break out
				break;

			}
			catch( Exception ex )
			{
				UISupport.showErrorMessage( ex );
				if( project != null )
				{
					workspace.removeProject( project );
				}
			}
		}
	}


	private void addListenersTo( final JUndoableTextField innerField )
	{
		initialKeyListener = new KeyAdapter()
		{
			@Override
			public void keyTyped( KeyEvent e )
			{
				resetUriField( innerField );
			}
		};
		innerField.addKeyListener( initialKeyListener );
		initialMouseListener = new MouseAdapter() {

			@Override
			public void mouseClicked( MouseEvent e )
			{
				resetUriField( innerField );
			}
		};
		innerField.addMouseListener( initialMouseListener );
	}

	private void resetUriField( JUndoableTextField innerField )
	{
		try
		{
			innerField.setText( "" );
			innerField.setFont( innerField.getFont().deriveFont( Font.PLAIN ) );
			innerField.setForeground( Color.BLACK );
		}
		catch (Exception e)
		{
			SoapUI.logError(e, "Exception resetting URI field");
		}
		// errors should not be swallowed, hence a second catch clause that rethrows it
		catch (Error e)
		{
			SoapUI.logError(e, "Error resetting URI field");
			throw e;
		}
		finally
		{
			if( initialKeyListener != null )
			{
				innerField.removeKeyListener( initialKeyListener );
			}
			if( initialMouseListener != null )
			{
				innerField.removeMouseListener( initialMouseListener );
			}
		}

	}

	protected RestService createRestProject( WsdlProject project, String URI ) throws MalformedURLException
	{

		RestURIParser restURIParser = new RestURIParserImpl( URI );
		RestParamsPropertyHolder params = new XmlBeansRestParamsTestPropertyHolder( null,
				RestParametersConfig.Factory.newInstance() );

		String resourcePath = restURIParser.getResourcePath();
		String resourceName = restURIParser.getResourceName();
		String host = restURIParser.getEndpoint();

		RestService restService = ( RestService )project.addNewInterface( host, RestServiceFactory.REST_TYPE );
		restService.addEndpoint( restURIParser.getEndpoint() );
		//TODO: to find out why do we need to separate base path and resource path
		// restService.setBasePath( restURIParser.getPath() );

		RestResource restResource = restService.addNewResource( resourceName, resourcePath );

		RestMethod restMethod = addNewMethod( restResource, resourceName );

		extractAndFillParameters( URI, params );
		copyParameters( params, restResource.getParams() );

		RestRequest restRequest = addNewRequest( restMethod );
		UISupport.select( restRequest );
		UISupport.showDesktopPanel( restRequest );

		return restService;
	}

	protected void extractAndFillParameters( String URI, RestParamsPropertyHolder params )
	{
		// This does lot of magic including extracting and filling up parameters on the params
		RestUtils.extractParams( URI, params, false );
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

	/**
	 * This Form interface contains all the fields that will be used to populate the XFormDialog
	 */
	@AForm( name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWPROJECT_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		@AField( description = "Form.URI.Description", type = AField.AFieldType.STRING )
		public final static String URI = messages.get( "Form.URI.Label" );


	}
}