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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;

import com.eviware.soapui.support.Tools;

public class JHyperlinkLabel extends JLabel
{
	private Color underlineColor = null;

	public JHyperlinkLabel( String label )
	{
		super( label );

		setForeground( Color.BLUE.darker() );
		setCursor( new Cursor( Cursor.HAND_CURSOR ) );
		addMouseListener( new HyperlinkLabelMouseAdapter() );
	}

	@Override
	protected void paintComponent( Graphics g )
	{
		super.paintComponent( g );

		g.setColor( underlineColor == null ? getForeground() : underlineColor );

		Insets insets = getInsets();

		int left = insets.left;
		if( getIcon() != null )
			left += getIcon().getIconWidth() + getIconTextGap();

		g.drawLine( left, getHeight() - 1 - insets.bottom, ( int )getPreferredSize().getWidth() - insets.right,
				getHeight() - 1 - insets.bottom );
	}

	public class HyperlinkLabelMouseAdapter extends MouseAdapter
	{
		@Override
		public void mouseClicked( MouseEvent e )
		{
			Tools.openURL( getText() );
		}
	}

	public Color getUnderlineColor()
	{
		return underlineColor;
	}

	public void setUnderlineColor( Color underlineColor )
	{
		this.underlineColor = underlineColor;
	}
}