/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com
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

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.rest.actions.service.GenerateRestTestSuiteAction;
import com.eviware.soapui.impl.rest.support.WadlImporter;
import com.eviware.soapui.impl.support.definition.support.InvalidDefinitionException;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.project.CreateWebTestAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;

import java.io.File;

/**
 * Action class to create new Generic project.
 *
 * @author Ole.Matzura
 */

public class NewWadlProjectAction extends AbstractSoapUIAction<WorkspaceImpl>
{
	public static final String SOAPUI_ACTION_ID = "NewWadlProjectAction";
	private XFormDialog dialog;

	public static final MessageSupport messages = MessageSupport.getMessages( NewWadlProjectAction.class );

	public NewWadlProjectAction()
	{
		super( messages.get( "Title" ), messages.get( "Description" ) );
	}

	public void perform( WorkspaceImpl workspace, Object param )
	{
		if( dialog == null )
		{
			dialog = ADialogBuilder.buildDialog( Form.class );
			dialog.getFormField( Form.INITIALWADL ).addFormFieldListener( new XFormFieldListener()
			{
				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
				{
					String value = newValue.toLowerCase().trim();

					dialog.getFormField( Form.GENERATETESTSUITE ).setEnabled( newValue.trim().length() > 0 );
					initProjectName( newValue );
				}
			} );
		}
		else
		{
			dialog.setValue( Form.INITIALWADL, "" );
			dialog.setValue( Form.PROJECTNAME, "" );
			dialog.getFormField( Form.GENERATETESTSUITE ).setEnabled( false );
		}

		if( param instanceof String )
		{
			dialog.setValue( Form.INITIALWADL, param.toString() );
			initProjectName( param.toString() );
		}

		while( dialog.show() )
		{
			WsdlProject project = null;
			try
			{
				String projectName = dialog.getValue( Form.PROJECTNAME ).trim();
				if( projectName.length() == 0 )
				{
					UISupport.showErrorMessage( messages.get( "MissingProjectNameError" ) );
				}
				else
				{
					project = workspace.createProject( projectName, null );

					if( project != null )
					{
						UISupport.select( project );
						String url = dialog.getValue( Form.INITIALWADL ).trim();

						if( dialog.getBooleanValue( Form.RELATIVEPATHS ) )
						{
							String folder = workspace.getProjectRoot();

							if( PathUtils.isFilePath( url ) && PathUtils.isAbsolutePath( url ) )
							{
								folder = new File( url ).getParent();
							}

							if( !project.save( folder ) )
							{
								UISupport
										.showErrorMessage( "Project was not saved, paths will not be stored relatively until configured." );
							}
							else
							{
								project.setResourceRoot( "${projectDir}" );
							}
						}

						if( url.length() > 0 )
						{
							if( new File( url ).exists() )
							{
								url = new File( url ).toURI().toURL().toString();
							}

							if( url.toUpperCase().endsWith( "WADL" ) )
							{
								importWadl( project, url );
							}
						}
						if( dialog.getBooleanValue( Form.CREATEWEBTEST ) )
						{
							new CreateWebTestAction().perform( project, param );
						}

						break;
					}
				}
			}
			catch( InvalidDefinitionException ex )
			{
				ex.show();
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

	public void initProjectName( String newValue )
	{
		if( StringUtils.isNullOrEmpty( dialog.getValue( Form.PROJECTNAME ) ) && StringUtils.hasContent( newValue ) )
		{
			int ix = newValue.lastIndexOf( '.' );
			if( ix > 0 )
				newValue = newValue.substring( 0, ix );

			ix = newValue.lastIndexOf( '/' );
			if( ix == -1 )
				ix = newValue.lastIndexOf( '\\' );

			if( ix != -1 )
				dialog.setValue( Form.PROJECTNAME, newValue.substring( ix + 1 ) );
		}
	}

	private void importWadl( WsdlProject project, String url )
	{
		RestService restService = ( RestService )project
				.addNewInterface( project.getName(), RestServiceFactory.REST_TYPE );
		UISupport.select( restService );
		try
		{
			new WadlImporter( restService ).initFromWadl( url );

			if( dialog.getBooleanValue( Form.GENERATETESTSUITE ) )
			{
				GenerateRestTestSuiteAction generateTestSuiteAction = new GenerateRestTestSuiteAction();
				generateTestSuiteAction.generateTestSuite( restService, true );
			}
		}
		catch( Exception e )
		{
			UISupport.showErrorMessage( e );
		}
	}

	@AForm( name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWPROJECT_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		@AField( description = "Form.ProjectName.Description", type = AField.AFieldType.STRING )
		public final static String PROJECTNAME = messages.get( "Form.ProjectName.Label" );

		@AField( description = "Form.InitialWadl.Description", type = AField.AFieldType.FILE )
		public final static String INITIALWADL = messages.get( "Form.InitialWadl.Label" );

		@AField( description = "Form.GenerateTestSuite.Description", type = AField.AFieldType.BOOLEAN, enabled = false )
		public final static String GENERATETESTSUITE = messages.get( "Form.GenerateTestSuite.Label" );

		@AField( description = "Form.RelativePaths.Description", type = AField.AFieldType.BOOLEAN, enabled = true )
		public final static String RELATIVEPATHS = messages.get( "Form.RelativePaths.Label" );

		@AField( description = "Form.CreateWebTest.Description", type = AField.AFieldType.BOOLEAN, enabled = true )
		public final static String CREATEWEBTEST = messages.get( "Form.CreateWebTest.Label" );

	}
}