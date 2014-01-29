package com.eviware.soapui.impl.rest.panels.mock;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.WsdlMockResponseDesktopPanel;
import com.eviware.soapui.impl.wsdl.support.CompressionSupport;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import java.awt.*;

public class RestMockResponsePanelBuilder extends EmptyPanelBuilder<RestMockResponse>
{

	public DesktopPanel buildDesktopPanel( RestMockResponse mockResponse )
	{
		return new RestMockResponseDesktopPanel( mockResponse );
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

		WsdlProject project = ( WsdlProject )mockResponse.getMockOperation().getMockService().getProject();
		StringList outgoingNames = new StringList( project.getWssContainer().getOutgoingWssNames() );
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
