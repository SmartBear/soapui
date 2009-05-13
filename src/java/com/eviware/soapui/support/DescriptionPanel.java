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
package com.eviware.soapui.support;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.eviware.soapui.support.swing.GradientPanel;

public class DescriptionPanel extends GradientPanel
{
	private JLabel titleLabel;
	private JLabel descriptionLabel;

	public DescriptionPanel( String title, String description, ImageIcon icon )
	{
		super( new BorderLayout() );
		setBackground( UIManager.getColor( "control" ) );
		setForeground( Color.WHITE );
		setBorder( BorderFactory.createCompoundBorder( BorderFactory.createMatteBorder( 0, 0, 1, 0, Color.DARK_GRAY ),
				BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) ) );

		descriptionLabel = new JLabel();
		setDescription( description );

		JPanel innerPanel = new JPanel( new BorderLayout() );
		innerPanel.add( descriptionLabel, BorderLayout.CENTER );
		innerPanel.setOpaque( false );

		if( title != null )
		{
			descriptionLabel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 0, 0 ) );
			titleLabel = new JLabel( "<html><div style=\"font-size: 9px\"><b>" + title + "</b></div></html>" );
			innerPanel.add( titleLabel, BorderLayout.NORTH );
		}
		add( innerPanel, BorderLayout.CENTER );

		if( icon != null )
		{
			JLabel iconLabel = new JLabel( icon );
			iconLabel.setBorder( BorderFactory.createEmptyBorder( 0, 10, 0, 0 ) );
			add( iconLabel, BorderLayout.EAST );
		}
	}

	public void setTitle( String title )
	{
		titleLabel.setText( "<html><div style=\"font-size: 9px\"><b>" + title + "</b></div></html>" );
	}

	public void setDescription( String description )
	{
		descriptionLabel.setText( "<html><div style=\"font-size: 9px\">" + description + "</div></html>" );
	}
}
