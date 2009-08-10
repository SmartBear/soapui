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

package com.eviware.soapui.impl.rest.panels.method;

import java.awt.Component;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.support.components.JPropertiesTable;

/**
 * PanelBuilder for WsdlInterface
 * 
 * @author Ole.Matzura
 */

public class RestMethodPanelBuilder extends EmptyPanelBuilder<RestMethod>
{
	public RestMethodPanelBuilder()
	{
	}

	public RestMethodDesktopPanel buildDesktopPanel( RestMethod method )
	{
		return new RestMethodDesktopPanel( method );
	}

	public boolean hasDesktopPanel()
	{
		return true;
	}

	public Component buildOverviewPanel( RestMethod method )
	{
		JPropertiesTable<RestMethod> table = new JPropertiesTable<RestMethod>( "Method Properties" );
		table.addProperty( "Name", "name", true );
		table.addProperty( "Description", "description", true );
		table.addProperty( "HTTP Method", "method", RestRequestInterface.RequestMethod.getMethods() );

		table.setPropertyObject( method );

		return table;
	}

	public boolean hasOverviewPanel()
	{
		return true;
	}
}
