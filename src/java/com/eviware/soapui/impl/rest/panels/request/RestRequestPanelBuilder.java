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

package com.eviware.soapui.impl.rest.panels.request;

import java.awt.Component;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.support.types.StringList;

/**
 * PanelBuilder for WsdlInterface
 * 
 * @author Ole.Matzura
 */

public class RestRequestPanelBuilder extends EmptyPanelBuilder<RestRequest>
{
	public RestRequestPanelBuilder()
	{
	}

	public RestRequestDesktopPanel buildDesktopPanel( RestRequest request )
	{
		return new RestRequestDesktopPanel( request );
	}

	public boolean hasDesktopPanel()
	{
		return true;
	}

	public Component buildOverviewPanel( RestRequest request )
	{
		JPropertiesTable<RestRequest> table = new JPropertiesTable<RestRequest>( "Request Properties" );
		table.addProperty( "Name", "name" );
		table.addProperty( "Description", "description", true );
		// table.addProperty( "Method", "method", new Object[]{RequestMethod.GET,
		// RequestMethod.POST,
		// RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.HEAD} );
		table.addProperty( "Encoding", "encoding", new String[] { null, "UTF-8", "iso-8859-1" } );
		table.addProperty( "Endpoint", "endpoint", request.getOperation().getInterface().getEndpoints() );
		table.addProperty( "Bind Address", "bindAddress", true );
		table.addProperty( "Follow Redirects", "followRedirects", JPropertiesTable.BOOLEAN_OPTIONS );

		// security / authentication
		table.addProperty( "Username", "username", true );
		table.addPropertyShadow( "Password", "password", true );
		table.addProperty( "Domain", "domain", true );

		StringList keystores = new StringList( request.getOperation().getInterface().getProject().getWssContainer()
				.getCryptoNames() );
		keystores.add( "" );
		table.addProperty( "SSL Keystore", "sslKeystore", keystores.toStringArray() );

		table.addProperty( "Strip whitespaces", "stripWhitespaces", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Remove Empty Content", "removeEmptyContent", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Entitize Properties", "entitizeProperties", JPropertiesTable.BOOLEAN_OPTIONS );

		// post-processing
		table.addProperty( "Pretty Print", "prettyPrint", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Dump File", "dumpFile" ).setDescription( "Dumps response message to specified file" );
		table.addProperty( "Max Size", "maxSize", true ).setDescription( "The maximum number of bytes to receive" );

		table.setPropertyObject( request );

		return table;
	}

	public boolean hasOverviewPanel()
	{
		return true;
	}
}
