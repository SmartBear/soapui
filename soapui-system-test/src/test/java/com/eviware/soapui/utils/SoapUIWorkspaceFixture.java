/*
 * SoapUI, copyright (C) 2004-2013 smartbear.com
 *
 * SoapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.utils;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.project.Project;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class SoapUIWorkspaceFixture
{

	private final List<String> originalProjectNameList;

	public SoapUIWorkspaceFixture()
	{
		originalProjectNameList = createProjectNameList();
	}

	public List<String> createProjectNameList()
	{
		List<String> projectNameList = new ArrayList<String>(  );
		for(Project project : SoapUI.getWorkspace().getProjectList() )
		{
			projectNameList.add(project.getName());
		}
		Collections.sort( projectNameList );
		return projectNameList;
	}

	public int getTheIndexOfCurrentProjectInNavigationTree()
	{
		List<String> projectNameListWithNewProject = createProjectNameList();
		projectNameListWithNewProject.removeAll( originalProjectNameList );
		String projectName = projectNameListWithNewProject.get( 0 );

		return createProjectNameList().indexOf( projectName );
	}
}
