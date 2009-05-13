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
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * PanelBuilder for WsdlMockOperation
 * 
 * @author Ole.Matzura
 */

public class WsdlMockOperationPanelBuilder extends EmptyPanelBuilder<WsdlMockOperation>
{
	public boolean hasOverviewPanel()
	{
		return true;
	}

	public Component buildOverviewPanel( WsdlMockOperation mockOperation )
	{
		JPropertiesTable<WsdlMockOperation> table = new JPropertiesTable<WsdlMockOperation>( "Mock Operation" );
		table = new JPropertiesTable<WsdlMockOperation>( "MockOperation Properties" );
		table.addProperty( "Name", "name", true );
		table.addProperty( "Description", "description", true );
		table.addProperty( "WSDL Operation", "wsdlOperationName", false );
		table.addProperty( "Dispatch Style", "dispatchStyle", false );
		table.setPropertyObject( mockOperation );

		return table;
	}

	@Override
	public DesktopPanel buildDesktopPanel( WsdlMockOperation mockOperation )
	{
		return new WsdlMockOperationDesktopPanel( mockOperation );
	}

	@Override
	public boolean hasDesktopPanel()
	{
		return true;
	}
}
