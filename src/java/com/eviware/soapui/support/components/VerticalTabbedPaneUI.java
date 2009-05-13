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

package com.eviware.soapui.support.components;

import java.awt.FontMetrics;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

public class VerticalTabbedPaneUI extends MetalTabbedPaneUI
{
	protected void installDefaults()
	{
		super.installDefaults();

		textIconGap = 0;
		tabInsets = new Insets( 0, 0, 0, 0 );
	}

	protected int calculateTabWidth( int tabPlacement, int tabIndex, FontMetrics metrics )
	{
		Icon icon = getIconForTab( tabIndex );
		return icon == null ? 2 : icon.getIconWidth() + 2;
	}
}