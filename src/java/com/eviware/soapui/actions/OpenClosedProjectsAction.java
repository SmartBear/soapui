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

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Action to save all projects
 * 
 * @author ole.matzura
 */

public class OpenClosedProjectsAction extends AbstractSoapUIAction<WorkspaceImpl>
{
	public static final String SOAPUI_ACTION_ID = "OpenClosedProjectsAction";

	public OpenClosedProjectsAction()
	{
		super( "Open All Closed Projects", "Opens all closed projects in the current Workspace" );
	}

	public void perform( WorkspaceImpl workspace, Object param )
	{
		List<Project> openProjects = new ArrayList<Project>();
		for( Project project : workspace.getProjectList() )
			if( !project.isOpen() && !project.isDisabled() )
				openProjects.add( project );

		if( openProjects.isEmpty() )
		{
			UISupport.showErrorMessage( "No closed projects in workspace" );
			return;
		}

		for( Project project : openProjects )
		{
			try
			{
				workspace.openProject( project );
			}
			catch( SoapUIException e )
			{
				SoapUI.logError( e );
			}
		}
	}
}