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

package com.eviware.soapui.actions;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.model.workspace.WorkspaceListener;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Action to save all projects
 * 
 * @author ole.matzura
 */

public class SaveAllProjectsAction extends AbstractSoapUIAction<WorkspaceImpl> implements WorkspaceListener
{
	public static final String SOAPUI_ACTION_ID = "SaveAllProjectsAction";

	public SaveAllProjectsAction()
	{
		super( "Save All Projects", "Saves all projects in the current Workspace" );
		
		setEnabled( SoapUI.getWorkspace().getProjectCount() > 0 );
		SoapUI.getWorkspace().addWorkspaceListener( this );
	}

	public void perform( WorkspaceImpl workspace, Object param )
	{
		workspace.save( false );
	}

	public void projectAdded( Project project )
	{
		setEnabled( true );
	}

	public void projectChanged( Project project )
	{
	}

	public void projectRemoved( Project project )
	{
		setEnabled( project.getWorkspace().getProjectCount() == 0 );
	}

	public void workspaceSwitched( Workspace workspace )
	{
		setEnabled( workspace.getProjectCount() > 0 );
	}

	public void workspaceSwitching( Workspace workspace )
	{
	}
}