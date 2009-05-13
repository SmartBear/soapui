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

package com.eviware.soapui.impl.actions;

import com.eviware.soapui.actions.CloseOpenProjectsAction;
import com.eviware.soapui.actions.OpenClosedProjectsAction;
import com.eviware.soapui.actions.SaveAllProjectsAction;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.action.SoapUIActionMapping;
import com.eviware.soapui.support.action.support.DefaultSoapUIActionGroup;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * SoapUIActionGroup for WsdlProjects, returns different actions depending on if
 * the project is disabled or not.
 * 
 * @author ole.matzura
 */

public class WorkspaceImplSoapUIActionGroup extends DefaultSoapUIActionGroup<WorkspaceImpl>
{
	public WorkspaceImplSoapUIActionGroup( String id, String name )
	{
		super( id, name );
	}

	public SoapUIActionMappingList<WorkspaceImpl> getActionMappings( WorkspaceImpl workspace )
	{
		SoapUIActionMappingList<WorkspaceImpl> mappings = super.getActionMappings( workspace );

		mappings.getMapping( SaveAllProjectsAction.SOAPUI_ACTION_ID ).setEnabled( workspace.getProjectCount() > 0 );
		SoapUIActionMapping<WorkspaceImpl> openMapping = mappings.getMapping( OpenClosedProjectsAction.SOAPUI_ACTION_ID );
		openMapping.setEnabled( false );
		SoapUIActionMapping<WorkspaceImpl> closeMapping = mappings.getMapping( CloseOpenProjectsAction.SOAPUI_ACTION_ID );
		closeMapping.setEnabled( false );

		for( Project project : workspace.getProjectList() )
		{
			if( project.isOpen() )
			{
				closeMapping.setEnabled( true );
				if( openMapping.isEnabled() )
					break;
			}
			else if( !project.isDisabled() )
			{
				openMapping.setEnabled( true );
				if( closeMapping.isEnabled() )
					break;
			}
		}

		return mappings;
	}
}