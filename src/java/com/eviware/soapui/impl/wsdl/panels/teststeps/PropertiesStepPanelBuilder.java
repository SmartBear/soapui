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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import javax.swing.JPanel;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlPropertiesTestStep;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * PanelBuilder for WsdlPropertiesTestStep
 * 
 * @author Ole.Matzura
 */

public class PropertiesStepPanelBuilder extends EmptyPanelBuilder<WsdlPropertiesTestStep>
{
	public PropertiesStepPanelBuilder()
	{
	}

	public DesktopPanel buildDesktopPanel( WsdlPropertiesTestStep testStep )
	{
		return new PropertiesStepDesktopPanel( testStep );
	}

	public boolean hasDesktopPanel()
	{
		return true;
	}

	public JPanel buildOverviewPanel( WsdlPropertiesTestStep testStep )
	{
		JPropertiesTable<WsdlPropertiesTestStep> table = new JPropertiesTable<WsdlPropertiesTestStep>(
				"PropertiesStep Properties" );

		table.addProperty( "Name", "name", true );
		table.addProperty( "Description", "description", true );
		table.addProperty( "Create Missing on Load", "createMissingOnLoad", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Save before Load", "saveFirst", JPropertiesTable.BOOLEAN_OPTIONS );
		table.addProperty( "Discard Values on Save", "discardValuesOnSave", JPropertiesTable.BOOLEAN_OPTIONS );
		table.setPropertyObject( testStep );

		return table;
	}

	public boolean hasOverviewPanel()
	{
		return true;
	}
}
