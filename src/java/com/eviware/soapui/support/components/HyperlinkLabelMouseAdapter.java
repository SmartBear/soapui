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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import com.eviware.soapui.support.Tools;

public final class HyperlinkLabelMouseAdapter extends MouseAdapter
{
	private final JComponent label;

	public HyperlinkLabelMouseAdapter( JTextComponent label )
	{
		this.label = label;
	}

	public HyperlinkLabelMouseAdapter( JLabel label )
	{
		this.label = label;
	}

	@Override
	public void mouseClicked( MouseEvent e )
	{
		String text = label instanceof JLabel ? ( ( JLabel )label ).getText() : ( ( JTextComponent )label ).getText();
		Tools.openURL( text );
	}
}