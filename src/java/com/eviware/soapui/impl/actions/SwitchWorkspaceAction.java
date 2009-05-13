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
 * Action to swtich the current workspace
 * 
 * @author ole.matzura
 */

public class SwitchWorkspaceAction extends AbstractSoapUIAction<WorkspaceImpl>
{
	public static final String SOAPUI_ACTION_ID = "SwitchWorkspaceAction";
	public static final MessageSupport messages = MessageSupport.getMessages( SwitchWorkspaceAction.class );

	public SwitchWorkspaceAction()
	{
		super( messages.get( "SwitchWorkspaceAction.Title" ), messages.get( "SwitchWorkspaceAction.Description" ) );
	}

	public void perform( WorkspaceImpl workspace, Object param )
	{
		if( SoapUI.getTestMonitor().hasRunningTests() )
		{
			UISupport.showErrorMessage( messages.get( "SwitchWorkspaceAction.WhileTestsAreRunningError" ) );
			return;
		}

		File newPath = null;

		if( param != null )
		{
			newPath = new File( param.toString() );
		}
		else
		{
			newPath = UISupport.getFileDialogs().open( this, messages.get( "SwitchWorkspaceAction.FileOpenTitle" ),
					".xml", "soapUI Workspace (*.xml)", workspace.getPath() );
		}

		if( newPath != null )
		{
			if( SoapUI.getDesktop().closeAll() )
			{
				boolean save = true;

				if( !newPath.exists() )
				{
					if( !UISupport.confirm( messages.get( "SwitchWorkspaceAction.Confirm.Label", newPath.getName() ),
							messages.get( "SwitchWorkspaceAction.Confirm.Title" ) ) )
					{
						return;
					}

					save = false;
				}
				else if( workspace.getOpenProjectList().size() > 0 )
				{
					Boolean val = UISupport.confirmOrCancel( messages.get( "SwitchWorkspaceAction.SaveOpenProjects.Label" ),
							messages.get( "SwitchWorkspaceAction.SaveOpenProjects.Title" ) );
					if( val == null )
						return;

					save = val.booleanValue();
				}

				workspace.save( !save );

				try
				{
					workspace.switchWorkspace( newPath );
					SoapUI.getSettings().setString( SoapUI.CURRENT_SOAPUI_WORKSPACE, newPath.getAbsolutePath() );
					UISupport.select( workspace );
				}
				catch( SoapUIException e )
				{
					UISupport.showErrorMessage( e );
				}
			}
		}
	}
}