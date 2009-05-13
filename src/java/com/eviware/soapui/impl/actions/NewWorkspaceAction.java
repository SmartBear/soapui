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

package com.eviware.soapui.impl.actions;

import java.io.File;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Action for creating a new Workspace
 * 
 * @author ole.matzura
 */

public class NewWorkspaceAction extends AbstractSoapUIAction<WorkspaceImpl>
{
	public static final String SOAPUI_ACTION_ID = "NewWorkspaceAction";
	public static final MessageSupport messages = MessageSupport.getMessages( NewWorkspaceAction.class );

	public NewWorkspaceAction()
	{
		super( messages.get( "Title" ), messages.get( "Description" ) );
	}

	public void perform( WorkspaceImpl workspace, Object param )
	{
		if( SoapUI.getTestMonitor().hasRunningTests() )
		{
			UISupport.showErrorMessage( messages.get( "FailBecauseOfRunningTests" ) );
			return;
		}

		String name = UISupport.prompt( messages.get( "EnterName.Prompt" ), messages.get( "EnterName.Title" ), "" );
		if( name == null )
			return;

		File newPath = UISupport.getFileDialogs().saveAs( this, messages.get( "SaveAs.Title" ), ".xml",
				"soapUI Workspace (*.xml)", new File( name + "-workspace.xml" ) );
		if( newPath == null )
			return;

		if( SoapUI.getDesktop().closeAll() )
		{
			if( newPath.exists() )
			{
				if( !UISupport.confirm( messages.get( "Overwrite.Prompt" ), messages.get( "Overwrite.Title" ) ) )
				{
					return;
				}

				if( !newPath.delete() )
				{
					UISupport.showErrorMessage( messages.get( "NewWorkspaceAction.FailedToDeleteExisting" ) );
					return;
				}
			}

			Boolean val = Boolean.TRUE;

			if( workspace.getOpenProjectList().size() > 0 )
			{
				val = UISupport.confirmOrCancel( messages.get( "SaveAllProjects.Prompt" ), messages
						.get( "SaveAllProjects.Title" ) );
				if( val == null )
					return;
			}

			workspace.save( val.booleanValue() );

			try
			{
				workspace.switchWorkspace( newPath );
				SoapUI.getSettings().setString( SoapUI.CURRENT_SOAPUI_WORKSPACE, newPath.getAbsolutePath() );
				workspace.setName( name );
			}
			catch( SoapUIException e )
			{
				UISupport.showErrorMessage( e );
			}
		}
	}
}