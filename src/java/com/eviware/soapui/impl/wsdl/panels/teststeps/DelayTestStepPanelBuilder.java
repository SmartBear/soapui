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

import java.awt.Component;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlDelayTestStep;
import com.eviware.soapui.support.components.JPropertiesTable;

/**
 * PanelBuilder for WsdlGotoTestStep
 * 
 * @author Ole.Matzura
 */

public class DelayTestStepPanelBuilder extends EmptyPanelBuilder<WsdlDelayTestStep>
{
	public DelayTestStepPanelBuilder()
	{
	}

	@Override
	public Component buildOverviewPanel( WsdlDelayTestStep modelItem )
	{
		JPropertiesTable<WsdlDelayTestStep> table = buildDefaultProperties( modelItem, "Delay Properties" );
		table.addProperty( "Delay", "delayString", true );
		return table;
	}
}
