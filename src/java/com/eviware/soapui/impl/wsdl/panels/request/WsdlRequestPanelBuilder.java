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

package com.eviware.soapui.impl.wsdl.panels.request;

import javax.swing.JPanel;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.support.types.StringList;

/**
 * PanelBuilder for WsdlRequest
 * 
 * @author Ole.Matzura
 */

public class WsdlRequestPanelBuilder extends EmptyPanelBuilder<WsdlRequest>
{
	public WsdlRequestPanelBuilder()
	{
	}

	public WsdlRequestDesktopPanel buildDesktopPanel( WsdlRequest request )
	{
		return new WsdlRequestDesktopPanel( request );
	}

	public boolean hasDesktopPanel()
	{
		return true;
	}

	public JPanel buildOverviewPanel( WsdlRequest request )
	{
		JPropertiesTable<WsdlRequest> table = new JPropertiesTable<WsdlRequest>( "Request Properties", request );

		// basic properties
		table.addProperty( "Name", "name", true );
		table.addProperty( "Description", "description", true );
		table.addProperty( "Message Size", "contentLength", false );
		table.addProperty( "Encoding", "encoding", new String[] { null, "UTF-8", "iso-8859-1" } );
		table.addProperty( "Endpoint", "endpoint", request.getOperation().getInterface().getEndpoints() );
		table.addProperty( "Bind Address", "bindAddress", true );
		table.addProperty( "Follow Redirects", "followRedirects", JPropertiesTable.BOOLEAN_OPTIONS );

		// security / authentication
		table.addProperty( "Username", "username", true );
		table.addPropertyShadow( "Password", "password", true );
		table.addProperty( "Domain", "domain", true );
		table.addProperty( "WSS-Password Type", "wssPasswordType", new String[] { null, WsdlRequest.PW_TYPE_NONE,
				WsdlRequest.PW_TYPE_TEXT, WsdlRequest.PW_TYPE_DIGEST } );
		table.addProperty( "WSS TimeToLive", "wssTimeToLive", true );

		StringList keystores = new StringList( request.getOperation().getInterface().getProject().getWssContainer()
				.getCryptoNames() );
		keystores.add( 0, null );
		table.addProperty( "SSL Keystore", "sslKeystore", keystores.toStringArray() );

		table.addProperty( "Skip SOAP Action", "skipSoapAction", JPropertiesTable.BOOLEAN_OPTIONS );

		// mtom / attachments
		table.addProperty( "Enable MTOM", "mtomEnabled", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Force MTOM", "forceMtom", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Inline Response Attachments", "inlineResponseAttachments", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Expand MTOM Attachments", "expandMtomResponseAttachments", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Disable multiparts", "multipartEnabled", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Encode Attachments", "encodeAttachments", JPropertiesTable.BOOLEAN_OPTIONS );

		// preprocessing
		table.addProperty( "Enable Inline Files", "inlineFilesEnabled", JPropertiesTable.BOOLEAN_OPTIONS )
				.setDescription( "Enables inline file references in elements with binary content; file:<path>" );
		table.addProperty( "Strip whitespaces", "stripWhitespaces", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Remove Empty Content", "removeEmptyContent", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Entitize Properties", "entitizeProperties", JPropertiesTable.BOOLEAN_OPTIONS );

		// post-processing
		table.addProperty( "Pretty Print", "prettyPrint", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Dump File", "dumpFile", true ).setDescription( "Dumps response message to specified file" );
		table.addProperty( "Max Size", "maxSize", true ).setDescription( "The maximum number of bytes to receive" );

		table.addProperty( "WS-Addressing", "wsAddressing", JPropertiesTable.BOOLEAN_OPTIONS );

		table.setPropertyObject( request );

		return table;
	}

	public boolean hasOverviewPanel()
	{
		return true;
	}
}
