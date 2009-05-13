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

package com.eviware.soapui.impl.rest.panels.resource;

import java.awt.Component;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.support.components.JPropertiesTable;

/**
 * PanelBuilder for WsdlInterface
 * 
 * @author Ole.Matzura
 */

public class RestResourcePanelBuilder extends EmptyPanelBuilder<RestResource>
{
	public RestResourcePanelBuilder()
	{
	}

	public RestResourceDesktopPanel buildDesktopPanel( RestResource resource )
	{
		return new RestResourceDesktopPanel( resource );
	}

	public boolean hasDesktopPanel()
	{
		return true;
	}

	public Component buildOverviewPanel( RestResource service )
	{
		JPropertiesTable<RestResource> table = new JPropertiesTable<RestResource>( "Resource Properties" );
		table.addProperty( "Name", "name", true );
		table.addProperty( "Description", "description", true );
		table.addProperty( "Path", "path", true );

		table.setPropertyObject( service );

		return table;
	}

	public boolean hasOverviewPanel()
	{
		return true;
	}
}
