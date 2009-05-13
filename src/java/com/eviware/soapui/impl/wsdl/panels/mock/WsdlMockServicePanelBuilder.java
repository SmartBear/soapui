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

package com.eviware.soapui.impl.wsdl.panels.mock;

import java.awt.Component;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * PanelBuilder for WsdlMockServices
 * 
 * @author ole.matzura
 */

public class WsdlMockServicePanelBuilder extends EmptyPanelBuilder<WsdlMockService>
{
	public WsdlMockServicePanelBuilder()
	{
	}

	public DesktopPanel buildDesktopPanel( WsdlMockService mockService )
	{
		return new WsdlMockServiceDesktopPanel( mockService );
	}

	@Override
	public boolean hasDesktopPanel()
	{
		return true;
	}

	public Component buildOverviewPanel( WsdlMockService mockService )
	{
		JPropertiesTable<WsdlMockService> table = new JPropertiesTable<WsdlMockService>( "MockService Properties" );
		table.addProperty( "Name", "name", true );
		table.addProperty( "Description", "description", true );
		table.addProperty( "Path", "path" );
		table.addProperty( "Port", "port" );
		table.addProperty( "Match SOAP Version", "requireSoapVersion", JPropertiesTable.BOOLEAN_OPTIONS ).setDescription(
				"Matches incoming SOAP Version against corresponding Interface" );
		table.addProperty( "Require SOAP Action", "requireSoapAction", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Dispatch Responses", "dispatchResponseMessages", JPropertiesTable.BOOLEAN_OPTIONS );
		StringList incomingNames = new StringList( mockService.getProject().getWssContainer().getIncomingWssNames() );
		incomingNames.add( "" );
		table.addProperty( "Incoming WSS", "incomingWss", incomingNames.toStringArray() );
		StringList outgoingNames = new StringList( mockService.getProject().getWssContainer().getOutgoingWssNames() );
		outgoingNames.add( "" );
		table.addProperty( "Default Outgoing WSS", "outgoingWss", outgoingNames.toStringArray() );
		table.setPropertyObject( mockService );

		return table;
	}

	public boolean hasOverviewPanel()
	{
		return true;
	}
}
