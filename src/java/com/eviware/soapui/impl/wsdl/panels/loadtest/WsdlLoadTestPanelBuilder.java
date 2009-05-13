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

package com.eviware.soapui.impl.wsdl.panels.loadtest;

import com.eviware.soapui.impl.EmptyPanelBuilder;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * PanelBuilder for LoadTests
 * 
 * @author Ole.Matzura
 */

public class WsdlLoadTestPanelBuilder extends EmptyPanelBuilder<WsdlLoadTest>
{
	public WsdlLoadTestPanelBuilder()
	{
	}

	public DesktopPanel buildDesktopPanel( WsdlLoadTest loadTest )
	{
		return new WsdlLoadTestDesktopPanel( loadTest );
	}

	public boolean hasDesktopPanel()
	{
		return true;
	}
}