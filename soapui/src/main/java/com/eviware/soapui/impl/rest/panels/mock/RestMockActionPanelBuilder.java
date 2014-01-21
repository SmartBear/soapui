package com.eviware.soapui.impl.rest.panels.mock;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.WsdlMockOperationDesktopPanel;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import java.awt.*;

public class RestMockActionPanelBuilder  extends EmptyPanelBuilder<RestMockAction>
{
	public boolean hasOverviewPanel()
	{
		return true;
	}

	public Component buildOverviewPanel( RestMockAction mockAction )
	{
		JPropertiesTable<RestMockAction> table = new JPropertiesTable<RestMockAction>( "Mock Action" );
		table = new JPropertiesTable<RestMockAction>( "MockAction Properties" );
		table.addProperty( "Name", "name", true );
		table.addProperty( "Description", "description", true );
		table.setPropertyObject( mockAction );

		return table;
	}

	@Override
	public DesktopPanel buildDesktopPanel( RestMockAction mockOperation )
	{
		return new RestMockActionDesktopPanel( mockOperation );
	}

	@Override
	public boolean hasDesktopPanel()
	{
		return true;
	}
}
