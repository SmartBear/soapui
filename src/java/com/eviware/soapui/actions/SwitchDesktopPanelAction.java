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

package com.eviware.soapui.actions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.ui.JDesktopPanelsList;
import com.eviware.soapui.ui.desktop.DesktopPanel;

public class SwitchDesktopPanelAction extends AbstractAction
{
	private JDialog dialog;
	private final JDesktopPanelsList desktopPanelsList;

	public SwitchDesktopPanelAction( JDesktopPanelsList desktopPanelsList )
	{
		super( "Switch Window" );
		this.desktopPanelsList = desktopPanelsList;

		putValue( SHORT_DESCRIPTION, "Prompts to switch to an open editor window" );
		putValue( ACCELERATOR_KEY, UISupport.getKeyStroke( "menu W" ) );
	}

	public void actionPerformed( ActionEvent e )
	{
		if( dialog == null )
		{
			desktopPanelsList.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );

			dialog = new JDialog( UISupport.getMainFrame(), "Switch Window", false );
			dialog.getContentPane().add( UISupport.buildDescription( null, "Select the window to switch to below", null ),
					BorderLayout.NORTH );
			dialog.getContentPane().add( desktopPanelsList, BorderLayout.CENTER );

			UISupport.initDialogActions( null, dialog );
			dialog.addWindowListener( new WindowAdapter()
			{
				@Override
				public void windowOpened( WindowEvent e )
				{
					initOnOpen();
				}

				private void initOnOpen()
				{
					SwingUtilities.invokeLater( new Runnable()
					{

						public void run()
						{
							desktopPanelsList.getDesktopPanelsList().requestFocus();
							if( desktopPanelsList.getDesktopPanels().size() > 0 )
								desktopPanelsList.getDesktopPanelsList().setSelectedIndex( 0 );
						}
					} );
				}

				@Override
				public void windowDeactivated( WindowEvent e )
				{
					dialog.setVisible( false );
				}

				@Override
				public void windowLostFocus( WindowEvent e )
				{
					dialog.setVisible( false );
				}

			} );
			dialog.addMouseListener( new MouseAdapter()
			{
				@Override
				public void mouseClicked( MouseEvent e )
				{
					dialog.setVisible( false );
				}
			} );

			desktopPanelsList.getDesktopPanelsList().addKeyListener( new KeyAdapter()
			{
				@Override
				public void keyPressed( KeyEvent e )
				{
					if( e.getKeyChar() == '\n' )
					{
						DesktopPanel dp = ( DesktopPanel )desktopPanelsList.getDesktopPanelsList().getSelectedValue();
						if( dp != null )
						{
							UISupport.showDesktopPanel( dp );
							dialog.setVisible( false );
						}
					}
				}
			} );

			desktopPanelsList.getDesktopPanelsList().addMouseListener( new MouseAdapter()
			{

				@Override
				public void mouseClicked( MouseEvent e )
				{
					if( e.getClickCount() > 1 )
					{
						DesktopPanel dp = ( DesktopPanel )desktopPanelsList.getDesktopPanelsList().getSelectedValue();
						if( dp != null )
						{
							UISupport.showDesktopPanel( dp );
							dialog.setVisible( false );
						}
					}
				}
			} );
		}

		dialog.setSize( new Dimension( 300, 120 + desktopPanelsList.getItemsCount() * 20 ) );

		UISupport.showDialog( dialog );
	}
}