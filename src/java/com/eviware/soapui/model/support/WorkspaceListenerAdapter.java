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

package com.eviware.soapui.model.support;

import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.model.workspace.WorkspaceListener;

/**
 * Adapter for WorkspaceListener implementations
 * 
 * @author Ole.Matzura
 */

public class WorkspaceListenerAdapter implements WorkspaceListener
{
	public void projectAdded( Project project )
	{
	}

	public void projectRemoved( Project project )
	{
	}

	public void projectChanged( Project project )
	{
	}

	public void workspaceSwitched( Workspace workspace )
	{
	}

	public void workspaceSwitching( Workspace workspace )
	{
	}
}
