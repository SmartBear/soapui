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

package com.eviware.soapui.impl.wsdl.actions.iface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Clones an Interface to another project
 * 
 * @author Ole.Matzura
 */

public class CloneInterfaceAction extends AbstractSoapUIAction<WsdlInterface>
{
	public CloneInterfaceAction()
	{
		super( "Clone Interface", "Clones this Interface to another project" );
	}

	public void perform( WsdlInterface iface, Object param )
	{
		WorkspaceImpl workspace = iface.getProject().getWorkspace();
		String[] names = ModelSupport.getNames( workspace.getOpenProjectList(), new String[] { "<Create New>" } );

		List<String> asList = new ArrayList<String>( Arrays.asList( names ) );
		asList.remove( iface.getProject().getName() );

		String targetProjectName = UISupport.prompt( "Select target Project for cloned Interface", "Clone Interface",
				asList );
		if( targetProjectName == null )
			return;

		WsdlProject targetProject = ( WsdlProject )workspace.getProjectByName( targetProjectName );
		if( targetProject == null )
		{
			targetProjectName = UISupport.prompt( "Enter name for new Project", "Clone TestSuite", "" );
			if( targetProjectName == null )
				return;

			try
			{
				targetProject = workspace.createProject( targetProjectName, null );
			}
			catch( SoapUIException e )
			{
				UISupport.showErrorMessage( e );
			}

			if( targetProject == null )
				return;
		}

		WsdlInterface targetIface = ( WsdlInterface )targetProject.getInterfaceByTechnicalId( iface.getTechnicalId() );
		if( targetIface != null )
		{
			UISupport.showErrorMessage( "Target Project already contains Interface for binding" );
		}
		else
		{
			boolean importEndpoints = UISupport.confirm( "Import endpoint defaults also?", getName() );
			UISupport.select( targetProject.importInterface( iface, importEndpoints, true ) );
		}
	}
}
