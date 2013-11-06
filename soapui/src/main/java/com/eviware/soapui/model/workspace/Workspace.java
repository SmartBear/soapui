/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.model.workspace;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.project.SaveStatus;
import com.eviware.soapui.support.SoapUIException;

import java.io.File;
import java.util.List;

/**
 * SoapUI workspace behaviour
 *
 * @author Ole.Matzura
 */

public interface Workspace extends ModelItem
{
	public Project getProjectAt( int index );

	public Project getProjectByName( String projectName );

	public int getProjectCount();

	public SaveStatus onClose();

	public SaveStatus save( boolean workspaceOnly );

	public void addWorkspaceListener( WorkspaceListener listener );

	public void removeWorkspaceListener( WorkspaceListener listener );

	public Project createProject( String name, File file ) throws SoapUIException;

	public void removeProject( Project project );

	public Project importProject( String filename ) throws SoapUIException;

	public int getIndexOfProject( Project project );

	public String getPath();

	public List<? extends Project> getProjectList();

	public void switchWorkspace( File newPath ) throws SoapUIException;

	public Project openProject( Project modelItem ) throws SoapUIException;

	public void inspectProjects();

}
