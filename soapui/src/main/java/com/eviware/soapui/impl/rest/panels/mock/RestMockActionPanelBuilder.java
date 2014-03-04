package com.eviware.soapui.impl.rest.panels.mock;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.rest.HttpMethod;
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
		JPropertiesTable<RestMockAction> table = new JPropertiesTable<RestMockAction>( "MockAction Properties" );
		boolean editable = true;
		table.addProperty( "Name", "name", editable );
		table.addProperty( "Description", "description", editable );
		table.addProperty( "Resource path", "resourcePath", editable );
		table.addProperty( "Method", "method", HttpMethod.values() );
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
