/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import java.awt.Component;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.ui.desktop.DesktopPanel;

public class MockResponseStepPanelBuilder extends EmptyPanelBuilder<WsdlMockResponseTestStep>
{
	public MockResponseStepPanelBuilder()
	{
	}

	public DesktopPanel buildDesktopPanel( WsdlMockResponseTestStep mockResponseStep )
	{
		return new WsdlMockResponseStepDesktopPanel( mockResponseStep );
	}

	@Override
	public boolean hasDesktopPanel()
	{
		return true;
	}

	public boolean hasOverviewPanel()
	{
		return true;
	}

	public Component buildOverviewPanel( WsdlMockResponseTestStep mockResponseStep )
	{
		JPropertiesTable<WsdlMockResponseTestStep> table = new JPropertiesTable<WsdlMockResponseTestStep>(
				"MockResponse Properties" );
		table.addProperty( "Name", "name", true );
		table.addProperty( "Description", "description", true );
		table.addProperty( "Message Size", "contentLength", false );
		table.addProperty( "Encoding", "encoding", new String[] { null, "UTF-8", "iso-8859-1" } );

		StringList outgoingNames = new StringList( mockResponseStep.getTestCase().getTestSuite().getProject()
				.getWssContainer().getOutgoingWssNames() );
		outgoingNames.add( "" );
		table.addProperty( "Outgoing WSS", "outgoingWss", outgoingNames.toStringArray() );

		table.addProperty( "Handle Fault", "handleFault", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Handle Response", "handleResponse", JPropertiesTable.BOOLEAN_OPTIONS );

		// attachments
		table.addProperty( "Enable MTOM", "mtomEnabled", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Force MTOM", "forceMtom", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Enable multiparts", "multipartEnabled", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Encode Attachments", "encodeAttachments", JPropertiesTable.BOOLEAN_OPTIONS );

		// preprocessing
		table.addProperty( "Strip whitespaces", "stripWhitespaces", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Remove Empty Content", "removeEmptyContent", JPropertiesTable.BOOLEAN_OPTIONS );

		// others
		table.addProperty( "Enable Inline Files", "inlineFilesEnabled", JPropertiesTable.BOOLEAN_OPTIONS )
				.setDescription( "Enables inline file references [file:<path>] in elements with binary content" );
		table.addProperty( "Response HTTP-Status", "responseHttpStatus", true );
		table.addProperty( "Response Delay", "responseDelay", true );
		table.addProperty( "Timeout", "timeout", true );

		String[] names = ModelSupport.getNames( new String[] { "" }, mockResponseStep.getTestCase().getTestStepList() );

		table.addProperty( "Start Step", "startStep", names );

		table.addProperty( "Port", "port", true );
		table.addProperty( "Path", "path", true );
		table.addProperty( "Host", "host", true );
		table.setPropertyObject( mockResponseStep );

		return table;
	}
}
