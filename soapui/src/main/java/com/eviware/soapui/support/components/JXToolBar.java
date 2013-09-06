/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import com.eviware.soapui.support.UISupport;

public class JXToolBar extends JToolBar
{

	public static final int STANDARD_COMPONENT_HEIGHT = 18;

	public <T extends JComponent> T addFixed( T component )
	{
		if( !( component instanceof JButton ) )
			UISupport.setPreferredHeight( component, STANDARD_COMPONENT_HEIGHT );

		Dimension preferredSize = component.getPreferredSize();
		component.setMinimumSize( preferredSize );
		component.setMaximumSize( preferredSize );

		add( component );

		return component;
	}

	public Component add( Component component )
	{
		if( !( component instanceof AbstractButton ) )
			UISupport.setPreferredHeight( component, STANDARD_COMPONENT_HEIGHT );

		return super.add( component );
	}

	public <T extends JComponent> T addWithOnlyMinimumHeight( T component )
	{
		if( !( component instanceof JButton ) )
		{
			Dimension minimumSize = component.getMinimumSize();
			component.setMinimumSize( new Dimension( minimumSize.width, STANDARD_COMPONENT_HEIGHT ) );
		}
		super.add( component );
		return component;
	}

	public void addGlue()
	{
		add( Box.createHorizontalGlue() );
	}

	public void addRelatedGap()
	{
		addSpace( 3 );
	}

	public void addUnrelatedGap()
	{
		addSeparator();
	}

	public void addLabeledFixed( String string, JComponent component )
	{
		addFixed( new JLabel( string ) );
		addSeparator( new Dimension( 3, 3 ) );
		addFixed( component );
	}

	public void addSpace( int i )
	{
		addSeparator( new Dimension( i, 1 ) );
	}
}
