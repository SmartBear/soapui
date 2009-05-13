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

package com.eviware.soapui.impl.rest.panels.service;

import java.awt.Component;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.support.components.JPropertiesTable;

/**
 * PanelBuilder for WsdlInterface
 * 
 * @author Ole.Matzura
 */

public class RestServicePanelBuilder extends EmptyPanelBuilder<RestService>
{
	public RestServicePanelBuilder()
	{
	}

	public RestServiceDesktopPanel buildDesktopPanel( RestService service )
	{
		return new RestServiceDesktopPanel( service );
	}

	public boolean hasDesktopPanel()
	{
		return true;
	}

	public Component buildOverviewPanel( RestService service )
	{
		JPropertiesTable<RestService> table = new JPropertiesTable<RestService>( "Service Properties" );
		table.addProperty( "Name", "name" );
		table.addProperty( "Description", "description", true );
		table.addProperty( "Base Path", "basePath", true );
		table.addProperty( "WADL", "wadlUrl", !service.isGenerated() );
		table.addProperty( "Generated", "generated", false );

		table.setPropertyObject( service );

		return table;
	}

	public boolean hasOverviewPanel()
	{
		return true;
	}
}
