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

package com.eviware.soapui.model.workspace;

import com.eviware.soapui.model.project.Project;

/**
 * Listener for Workspace-related events
 * 
 * @author Ole.Matzura
 */

public interface WorkspaceListener
{
	public void projectAdded( Project project );

	public void projectRemoved( Project project );

	public void projectChanged( Project project );

	public void workspaceSwitching( Workspace workspace );

	public void workspaceSwitched( Workspace workspace );
}
