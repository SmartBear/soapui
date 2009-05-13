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

package com.eviware.soapui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JWindow;

import com.eviware.soapui.support.UISupport;

public class SoapUISplash extends JWindow
{
	private final JFrame frame;

	public SoapUISplash( String fileName, JFrame frame )
	{
		super( frame );
		this.frame = frame;
		JLabel l = new JLabel( new ImageIcon( UISupport.findSplash( fileName ) ) );
		getContentPane().add( l, BorderLayout.CENTER );
		pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension labelSize = l.getPreferredSize();
		setLocation( screenSize.width / 2 - ( labelSize.width / 2 ), screenSize.height / 2 - ( labelSize.height / 2 ) );
		addMouseListener( new MouseAdapter()
		{
			public void mousePressed( MouseEvent e )
			{
				if( SoapUISplash.this.frame.isVisible() )
				{
					setVisible( false );
					dispose();
				}
			}
		} );
		setVisible( true );
	}

	@Override
	public void setVisible( boolean b )
	{
		super.setVisible( b );
		if( !b )
			dispose();
	}
}