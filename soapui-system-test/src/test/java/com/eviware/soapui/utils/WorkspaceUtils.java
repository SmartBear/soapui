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
package com.eviware.soapui.utils;

import com.eviware.soapui.SoapUI;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JPanelFixture;
import org.fest.swing.fixture.JPopupMenuFixture;

/**
 * Utility class used for generic operations on the workspace level
 */
public class WorkspaceUtils
{
	private static final String NAVIGATOR = "navigator";

	public static JPanelFixture getNavigatorPanel( FrameFixture frame )
	{
		return frame.panel( NAVIGATOR );
	}

	public static JPopupMenuFixture rightClickOnWorkspace( FrameFixture frame )
	{
		return getNavigatorPanel( frame ).tree().showPopupMenuAt( SoapUI.getWorkspace().getName() );
	}
}
