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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import java.awt.Component;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * PanelBuilder for JdbcRequestTestStep
 * 
 * @author dragica.soldo
 */

public class JdbcRequestTestStepPanelBuilder extends EmptyPanelBuilder<JdbcRequestTestStep>
{
	public JdbcRequestTestStepPanelBuilder()
	{
	}

	public DesktopPanel buildDesktopPanel( JdbcRequestTestStep testStep )
	{
		return new JdbcRequestTestStepDesktopPanel( testStep );
	}

	public boolean hasDesktopPanel()
	{
		return true;
	}

	@Override
	public Component buildOverviewPanel( JdbcRequestTestStep modelItem )
	{
		JPropertiesTable<JdbcRequestTestStep> table = buildDefaultProperties( modelItem, "JdbcRequestTestStep Properties" );
		table.addProperty( "Max Rows", "maxRows", true );
		table.addProperty( "Query Timeout", "queryTimeout", true );
		table.addProperty( "Fetch Size", "fetchSize", true );
		table.addProperty( "Discard Response", "discardResponse", JPropertiesTable.BOOLEAN_OPTIONS );
		return table;
	}
}
