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

package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.action.SoapUIActionGroup;
import com.eviware.soapui.support.action.support.AbstractSoapUIActionGroup;
import com.eviware.soapui.support.action.support.SoapUIActionMappingList;

/**
 * SoapUIActionGroup for WsdlProjects, returns different actions depending on if
 * the project is disabled or not.
 * 
 * @author ole.matzura
 */

public class WsdlProjectSoapUIActionGroup extends AbstractSoapUIActionGroup<WsdlProject>
{
	public WsdlProjectSoapUIActionGroup( String id, String name )
	{
		super( id, name );
	}

	public SoapUIActionMappingList<WsdlProject> getActionMappings( WsdlProject project )
	{
		if( project.isDisabled() )
		{
			SoapUIActionGroup<WsdlProject> actionGroup = SoapUI.getActionRegistry().getActionGroup(
					"DisabledWsdlProjectActions" );
			return actionGroup.getActionMappings( project );
		}
		else if( !project.isOpen() )
		{
			if( project.getEncrypted() != 0 )
			{
				SoapUIActionGroup<WsdlProject> actionGroup = SoapUI.getActionRegistry().getActionGroup(
						"EncryptedWsdlProjectActions" );
				return actionGroup.getActionMappings( project );
			}
			else
			{
				SoapUIActionGroup<WsdlProject> actionGroup = SoapUI.getActionRegistry().getActionGroup(
						"ClosedWsdlProjectActions" );
				return actionGroup.getActionMappings( project );
			}
		}
		else
		{
			SoapUIActionGroup<WsdlProject> actionGroup = SoapUI.getActionRegistry().getActionGroup(
					"EnabledWsdlProjectActions" );
			SoapUIActionMappingList<WsdlProject> mappings = actionGroup.getActionMappings( project );

			mappings.getMapping( SaveProjectAction.SOAPUI_ACTION_ID ).setEnabled(
					!project.isRemote() && project.getPath() != null );

			return mappings;
		}
	}
}