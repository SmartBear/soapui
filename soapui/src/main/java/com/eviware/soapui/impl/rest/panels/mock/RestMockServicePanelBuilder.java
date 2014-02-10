package com.eviware.soapui.impl.rest.panels.mock;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.rest.mock.RestMockService;
import com.eviware.soapui.impl.wsdl.panels.mock.WsdlMockServiceDesktopPanel;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import java.awt.*;

public class RestMockServicePanelBuilder extends EmptyPanelBuilder<RestMockService>
{
	public RestMockServicePanelBuilder()
	{
	}

	public DesktopPanel buildDesktopPanel( RestMockService restMockService )
	{
		// TODO: implement return new WsdlMockServiceDesktopPanel( restMockService );
		return null;
	}

	@Override
	public boolean hasDesktopPanel()
	{
		return true;
	}

	public Component buildOverviewPanel( RestMockService mockService )
	{
		JPropertiesTable<RestMockService> table = new JPropertiesTable<RestMockService>( "MockService Properties" );
		table.addProperty( "Name", "name", true );
		table.addProperty( "Description", "description", true );
		table.addProperty( "Path", "path" );
		table.addProperty( "Port", "port" );
		table.addProperty( "Dispatch Responses", "dispatchResponseMessages", JPropertiesTable.BOOLEAN_OPTIONS );
		table.setPropertyObject( mockService );

		return table;
	}

	public boolean hasOverviewPanel()
	{
		return true;
	}
}
