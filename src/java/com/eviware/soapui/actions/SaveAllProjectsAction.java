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

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Action to save all projects
 * 
 * @author ole.matzura
 */

public class SaveAllProjectsAction extends AbstractSoapUIAction<WorkspaceImpl>
{
	public static final String SOAPUI_ACTION_ID = "SaveAllProjectsAction";

	public SaveAllProjectsAction()
	{
		super( "Save All Projects", "Saves all projects in the current Workspace" );
	}

	public void perform( WorkspaceImpl workspace, Object param )
	{
		workspace.save( false );
	}
}