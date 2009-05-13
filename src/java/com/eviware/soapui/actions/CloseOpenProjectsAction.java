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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Action to save all projects
 * 
 * @author ole.matzura
 */

public class CloseOpenProjectsAction extends AbstractSoapUIAction<WorkspaceImpl>
{
	public static final String SOAPUI_ACTION_ID = "CloseOpenProjectsAction";

	public CloseOpenProjectsAction()
	{
		super( "Close All Open Projects", "Closes all open projects in the current Workspace" );
	}

	public void perform( WorkspaceImpl workspace, Object param )
	{
		List<Project> openProjects = new ArrayList<Project>();
		for( Project project : workspace.getProjectList() )
			if( project.isOpen() )
				openProjects.add( project );

		if( openProjects.isEmpty() )
		{
			UISupport.showErrorMessage( "No open projects in workspace" );
			return;
		}

		Boolean coc = UISupport.confirmOrCancel( "Save projects before closing?", getName() );
		if( coc == null )
			return;

		for( Project project : openProjects )
		{
			try
			{
				if( coc )
					project.save();

				workspace.closeProject( project );
			}
			catch( IOException e )
			{
				SoapUI.logError( e );
			}
		}
	}
}