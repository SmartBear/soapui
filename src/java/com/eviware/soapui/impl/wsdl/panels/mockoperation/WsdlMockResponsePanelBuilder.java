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

package com.eviware.soapui.impl.wsdl.panels.mockoperation;

import java.awt.Component;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * PanelBuilder for WsdlMockResponses
 * 
 * @author ole.matzura
 */

public class WsdlMockResponsePanelBuilder extends EmptyPanelBuilder<WsdlMockResponse>
{
	public DesktopPanel buildDesktopPanel( WsdlMockResponse mockResponse )
	{
		return new WsdlMockResponseDesktopPanel( mockResponse );
	}

	public boolean hasDesktopPanel()
	{
		return true;
	}

	public boolean hasOverviewPanel()
	{
		return true;
	}

	public Component buildOverviewPanel( WsdlMockResponse mockResponse )
	{
		JPropertiesTable<WsdlMockResponse> table = new JPropertiesTable<WsdlMockResponse>( "MockResponse Properties" );
		table.addProperty( "Name", "name", true );
		table.addProperty( "Description", "description", true );
		table.addProperty( "Message Size", "contentLength", false );
		table.addProperty( "Encoding", "encoding", new String[] { null, "UTF-8", "iso-8859-1" } );

		StringList outgoingNames = new StringList( mockResponse.getMockOperation().getMockService().getProject()
				.getWssContainer().getOutgoingWssNames() );
		outgoingNames.add( "" );
		table.addProperty( "Outgoing WSS", "outgoingWss", outgoingNames.toStringArray() );

		// attachments
		table.addProperty( "Enable MTOM", "mtomEnabled", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Force MTOM", "forceMtom", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Enable multiparts", "multipartEnabled", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Encode Attachments", "encodeAttachments", JPropertiesTable.BOOLEAN_OPTIONS );

		// preprocessing
		table.addProperty( "Strip whitespaces", "stripWhitespaces", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Remove Empty Content", "removeEmptyContent", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Entitize Properties", "entitizeProperties", JPropertiesTable.BOOLEAN_OPTIONS );

		// others
		table.addProperty( "Enable Inline Files", "inlineFilesEnabled", JPropertiesTable.BOOLEAN_OPTIONS )
				.setDescription( "Enables inline file references [file:<path>] in elements with binary content" );
		table.addProperty( "Response HTTP-Status", "responseHttpStatus", true );
		table.addProperty( "Response Delay", "responseDelay", true );
		table.addProperty( "Response Compression", "responseCompression", new String[] {
				WsdlMockResponse.AUTO_RESPONSE_COMPRESSION, WsdlMockResponse.NO_RESPONSE_COMPRESSION,
				CompressionSupport.ALG_DEFLATE, CompressionSupport.ALG_GZIP } );

		table.addProperty( "WS-Addressing", "wsAddressing", JPropertiesTable.BOOLEAN_OPTIONS );

		table.setPropertyObject( mockResponse );

		return table;
	}
}
