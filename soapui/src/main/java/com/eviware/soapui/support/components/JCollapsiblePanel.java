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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.eviware.soapui.support.UISupport;

public class JCollapsiblePanel extends JPanel
{
	private static final String HIGHLIGHT_SIGN = "* ";

	private ImageIcon minusIcon;
	private ImageIcon plusIcon;

	private JPanel contentPanel;
	private JXToolBar toolbar;
	private ToggleAction toggleAction;
	private JLabel titleLabel;
	private String nonHighlightedTitle;

	public JCollapsiblePanel( JPanel contentPanel, String title )
	{
		super( new BorderLayout() );
		this.contentPanel = contentPanel;
		minusIcon = UISupport.createImageIcon( "/button1.gif" );
		plusIcon = UISupport.createImageIcon( "/button2.gif" );

		add( contentPanel, BorderLayout.CENTER );
		add( startToolbar( title ), BorderLayout.NORTH );
	}

	public JCollapsiblePanel( String title )
	{
		this( new JPanel(), title );
	}

	private JXToolBar startToolbar( String title )
	{
		toolbar = UISupport.createToolbar();
		toolbar.setBorder( null );
		toolbar.setPreferredSize( new Dimension( 22, 22 ) );

		toggleAction = new ToggleAction();
		JButton toggleButton = new JButton( toggleAction );
		toggleButton.setBorder( null );
		toggleButton.setPreferredSize( new Dimension( 15, 15 ) );

		toolbar.addSpace( 3 );
		toolbar.addFixed( toggleButton );
		toolbar.addSpace( 3 );

		if( title != null )
		{
			nonHighlightedTitle = title;
			titleLabel = new JLabel( title );
			titleLabel.setPreferredSize( new Dimension( 500, 0 ) );
			titleLabel.setFont( titleLabel.getFont().deriveFont( Font.BOLD ) );
			toolbar.addFixed( titleLabel );
			toolbar.addSpace( 3 );
		}

		return toolbar;
	}

	public boolean isExpanded()
	{
		return toggleAction.getValue( Action.SMALL_ICON ) == getMinusIcon();
	}

	public void setExpanded( boolean expanded )
	{
		if( !expanded )
		{
			toggleAction.setShow();
		}
		else
		{
			toggleAction.setHide();
		}

		contentPanel.setVisible( expanded );
		refresh();
	}

	private void refresh()
	{
		contentPanel.revalidate();
		if( contentPanel.getParent() instanceof JComponent )
			( ( JComponent )contentPanel.getParent() ).revalidate();
	}

	public void setContentPanel( JPanel panel )
	{
		remove( contentPanel );
		add( panel, BorderLayout.CENTER );
		contentPanel = panel;

		refresh();
	}

	private class ToggleAction extends AbstractAction
	{
		public ToggleAction()
		{
			setHide();
		}

		public void setHide()
		{
			putValue( Action.SMALL_ICON, getMinusIcon() );
			putValue( Action.SHORT_DESCRIPTION, "Hides the content of this block" );
		}

		public void setShow()
		{
			putValue( Action.SMALL_ICON, getPlusIcon() );
			putValue( Action.SHORT_DESCRIPTION, "Shows the content of this block" );
		}

		public void actionPerformed( ActionEvent e )
		{
			setExpanded( !isExpanded() );
		}
	}

	public JXToolBar getToolbar()
	{
		return toolbar;
	}

	public ImageIcon getMinusIcon()
	{
		return minusIcon;
	}

	public void setMinusIcon( ImageIcon minusIcon )
	{
		this.minusIcon = minusIcon;
	}

	public ImageIcon getPlusIcon()
	{
		return plusIcon;
	}

	public void setPlusIcon( ImageIcon plusIcon )
	{
		this.plusIcon = plusIcon;
	}

	public void setHighlightedTitle( boolean highlightedTitle )
	{
		if( highlightedTitle )
		{
			if( !titleLabel.getText().startsWith( HIGHLIGHT_SIGN ) )
			{
				titleLabel.setText( HIGHLIGHT_SIGN + nonHighlightedTitle );
			}
		}
		else
		{
			titleLabel.setText( nonHighlightedTitle );
		}
	}

	public void setTitle( String title )
	{
		titleLabel.setText( title );
	}
}