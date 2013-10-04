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
import com.eviware.soapui.impl.wsdl.teststeps.ManualTestStep;
import com.eviware.soapui.support.components.JPropertiesTable;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * PanelBuilder for WsdlGotoTestStep
 * 
 * @author Ole.Matzura
 */

public class ManualTestStepPanelBuilder extends EmptyPanelBuilder<ManualTestStep>
{
	public ManualTestStepPanelBuilder()
	{
	}

	@Override
	public Component buildOverviewPanel( ManualTestStep modelItem )
	{
		JPropertiesTable<ManualTestStep> table = buildDefaultProperties( modelItem, "Step Properties" );
		return table;
	}

	@Override
	public DesktopPanel buildDesktopPanel( ManualTestStep modelItem )
	{
		return new ManualTestStepDesktopPanel( modelItem );
	}

	@Override
	public boolean hasDesktopPanel()
	{
		return true;
	}
}
