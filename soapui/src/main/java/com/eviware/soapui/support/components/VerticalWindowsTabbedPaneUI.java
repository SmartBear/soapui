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

package com.eviware.soapui.support.components;

import com.sun.java.swing.plaf.windows.WindowsTabbedPaneUI;

import javax.swing.*;
import java.awt.*;

public class VerticalWindowsTabbedPaneUI extends WindowsTabbedPaneUI
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
