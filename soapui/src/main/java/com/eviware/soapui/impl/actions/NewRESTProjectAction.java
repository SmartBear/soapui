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

import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.rest.*;
import com.eviware.soapui.impl.rest.support.*;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.workspace.Workspace;
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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;

/**
 * Action class to create new REST project.
 *
 * @author Shadid Chowdhury
 */

public class NewRESTProjectAction extends AbstractSoapUIAction<WorkspaceImpl>
{
	public static final String SOAPUI_ACTION_ID = "NewRESTProjectAction";

	private final static Logger logger = Logger.getLogger( NewRESTProjectAction.class );
	protected static final String DEFAULT_PROJECT_NAME = "REST Project";
	public static final String EXAMPLE_URI = "http://example.com/resource/path/search?parameter=value";
	private XFormDialog dialog;

	public static final MessageSupport messages = MessageSupport.getMessages( NewRESTProjectAction.class );
	private KeyListener initialKeyListener;
	private MouseListener initialMouseListener;
	private Font originalFont;

	private boolean defaultURIReplaced;

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
		dialog.setValue( Form.URI, EXAMPLE_URI );
		XFormField uriField = dialog.getFormField( Form.URI );

		JUndoableTextField textField = null;
		if( uriField instanceof JTextFieldFormField )
		{
			defaultURIReplaced = false;
			textField = ( ( JTextFieldFormField )uriField ).getComponent();
			textField.requestFocus();
			originalFont = textField.getFont();
			textField.setFont( originalFont.deriveFont( Font.ITALIC ) );
			textField.setForeground( new Color( 170, 170, 170 ) );
			logger.log( Level.DEBUG, "Adding listeners to URI text field" );
			addListenersTo( textField );
		}

		while( dialog.show() )
		{
			WsdlProject project = null;
			try
			{
				if( !defaultURIReplaced && textField != null )
				{
					resetUriField( textField );
				}

				String URI = dialog.getValue( Form.URI ).trim();

				project = workspace.createProject( createDefaultProjectName( workspace ), null );

				createRestProject( project, URI );

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
			}
		}
		if( !defaultURIReplaced && textField != null )
		{
			resetUriField( textField );
		}
	}

	protected String createDefaultProjectName( Workspace workspace )
	{
		int index = 1;
		while(workspace.getProjectByName( DEFAULT_PROJECT_NAME + " " + index ) !=null)
		{
			index++;
		}
		return DEFAULT_PROJECT_NAME + " " + index ;
	}


	private void addListenersTo( final JUndoableTextField innerField )
	{
		initialKeyListener = new KeyAdapter()
		{

			@Override
			public void keyPressed( KeyEvent e )
			{
				resetUriField( innerField );

			}
		};
		innerField.addKeyListener( initialKeyListener );
		initialMouseListener = new MouseAdapter()
		{

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
			defaultURIReplaced = true;
			innerField.setText( "" );
			innerField.setFont( originalFont );
			innerField.setForeground( Color.BLACK );
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

	/**
	 * This Form interface contains all the fields that will be used to populate the XFormDialog
	 */
	@AForm( name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWRESTPROJECT_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		@AField( description = "Form.URI.Description", type = AField.AFieldType.STRING )
		public final static String URI = messages.get( "Form.URI.Label" );


	}
}