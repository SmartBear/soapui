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
	   return new WsdlMockServiceDesktopPanel( restMockService );
	}

	@Override
	public boolean hasDesktopPanel()
	{
		return true;
	}

	public Component buildOverviewPanel( RestMockService mockService )
	{
		JPropertiesTable<RestMockService> table = new JPropertiesTable<RestMockService>( "MockService Properties" );
		boolean editable = true;
		table.addProperty( "Name", "name", editable );
		table.addProperty( "Description", "description", editable );
		table.addProperty( "Path", "path", !editable );
		table.addProperty( "Port", "port", !editable );
		table.setPropertyObject( mockService );

		return table;
	}

	public boolean hasOverviewPanel()
	{
		return true;
	}
}
