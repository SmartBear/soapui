/*
 * SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 * SoapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.smartbear.soapui.utils.fest;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.project.Project;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JPanelFixture;
import org.fest.swing.fixture.JPopupMenuFixture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class used for generic operations on the workspace level
 */
public final class WorkspaceUtils
{
	private static final String NAVIGATOR = "navigator";

	private WorkspaceUtils()
	{
		throw new AssertionError();
	}

	public static JPanelFixture getNavigatorPanel( FrameFixture frame )
	{
		return frame.panel( NAVIGATOR );
	}

	public static JPopupMenuFixture rightClickOnWorkspace( FrameFixture frame )
	{
		return getNavigatorPanel( frame ).tree().showPopupMenuAt( SoapUI.getWorkspace().getName() );
	}

	public static List<String> getProjectNameList()
	{
		List<String> projectNameList = new ArrayList<String>();
		for( Project project : SoapUI.getWorkspace().getProjectList() )
		{
			projectNameList.add( project.getName() );
		}
		Collections.sort( projectNameList );
		return projectNameList;
	}
}