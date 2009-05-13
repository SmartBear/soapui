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

package com.eviware.soapui.impl;

import javax.swing.JPanel;

import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.components.JPropertiesTable;

/**
 * PanelBuilder for default Workspace implementation
 * 
 * @author ole.matzura
 */

public class WorkspaceImplPanelBuilder extends EmptyPanelBuilder<WorkspaceImpl>
{
	public static final MessageSupport messages = MessageSupport.getMessages( WorkspaceImplPanelBuilder.class );

	public WorkspaceImplPanelBuilder()
	{
	}

	public JPanel buildOverviewPanel( WorkspaceImpl workspace )
	{
		JPropertiesTable<WorkspaceImpl> table = buildDefaultProperties( workspace, messages.get( "OverviewPanel.Title" ) );
		table.addProperty( messages.get( "OverviewPanel.File.Label" ), "path", false );
		table.addProperty( messages.get( "OverviewPanel.ProjectRoot.Label" ), "projectRoot",
				new String[] { null, "${workspaceDir}" } ).setDescription(
				messages.get( "OverviewPanel.ProjectRoot.Description" ) );
		return table;
	}

	public boolean hasOverviewPanel()
	{
		return true;
	}
}