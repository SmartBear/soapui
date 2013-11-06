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

package com.eviware.soapui.impl.wsdl.panels.teststeps.amf;

import java.awt.Component;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * PanelBuilder for AMFRequestTestStep
 * 
 * @author nebojsa.tasic
 */

public class AMFRequestTestStepPanelBuilder extends EmptyPanelBuilder<AMFRequestTestStep>
{
	public AMFRequestTestStepPanelBuilder()
	{
	}

	public DesktopPanel buildDesktopPanel( AMFRequestTestStep testStep )
	{
		return new AMFRequestTestStepDesktopPanel( testStep );
	}

	public boolean hasDesktopPanel()
	{
		return true;
	}

	@Override
	public Component buildOverviewPanel( AMFRequestTestStep modelItem )
	{
		JPropertiesTable<AMFRequestTestStep> table = buildDefaultProperties( modelItem, "AMFRequestTestStep Properties" );
		table.addProperty( "Discard Response", "discardResponse", JPropertiesTable.BOOLEAN_OPTIONS );
		return table;
	}
}
