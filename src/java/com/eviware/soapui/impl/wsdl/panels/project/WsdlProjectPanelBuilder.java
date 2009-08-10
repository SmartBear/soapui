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

package com.eviware.soapui.impl.wsdl.panels.project;

import javax.swing.JPanel;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * PanelBuilder for WsdlProject. Only builds an overview panel.
 * 
 * @author Ole.Matzura
 */

public class WsdlProjectPanelBuilder extends EmptyPanelBuilder<WsdlProject>
{
	public WsdlProjectPanelBuilder()
	{
	}

	public JPanel buildOverviewPanel( WsdlProject project )
	{
		JPropertiesTable<WsdlProject> table = new JPropertiesTable<WsdlProject>( "Project Properties", project );

		if( project.isOpen() )
		{
			table.addProperty( "Name", "name", true );
			table.addProperty( "Description", "description", true );
			table.addProperty( "File", "path" );

			if( !project.isDisabled() )
			{
				table.addProperty( "Resource Root", "resourceRoot", new String[] { null, "${projectDir}" } );
				table.addProperty( "Cache Definitions", "cacheDefinitions", JPropertiesTable.BOOLEAN_OPTIONS );
				table.addPropertyShadow( "Project Password", "shadowPassword", true );
				table.addProperty( "Script Language", "defaultScriptLanguage", SoapUIScriptEngineRegistry
						.getAvailableEngineIds() );
			}
		}
		else
		{
			table.addProperty( "File", "path" );
		}

		return table;
	}

	public boolean hasOverviewPanel()
	{
		return true;
	}

	public boolean hasDesktopPanel()
	{
		return true;
	}

	public DesktopPanel buildDesktopPanel( WsdlProject modelItem )
	{
		return new WsdlProjectDesktopPanel( modelItem );
	}
}
