/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.panels;

import java.awt.Component;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * PanelBuilder for SecurityTests
 * 
 * @author dragica.soldo
 */

public class SecurityTestPanelBuilder<T extends SecurityTest> extends EmptyPanelBuilder<T>
{
	public SecurityTestPanelBuilder()
	{
	}

	public DesktopPanel buildDesktopPanel( T securityTest )
	{
		return new SecurityTestDesktopPanel( securityTest );
	}

	public boolean hasDesktopPanel()
	{
		return true;
	}

	public Component buildOverviewPanel( T modelItem )
	{
		JPropertiesTable<SecurityTest> table = new JPropertiesTable<SecurityTest>( "SecurityTest Properties", modelItem );

		table.addProperty( "Name", "name", true );

		table.setPropertyObject( modelItem );

		return table;
	}
}
