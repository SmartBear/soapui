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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.support;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.eviware.soapui.support.UISupport;
import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * Action to display the contents of a generated configuration file
 * 
 * @author ole.matzura
 */

public abstract class ShowConfigFileAction extends AbstractAction
{
	private ContentDialog dialog;
	private final String title;
	private final String description;

	public ShowConfigFileAction( String title, String description )
	{
		super( "Show Config" );

		this.title = title;
		this.description = description;
	}

	public void actionPerformed( ActionEvent e )
	{
		if( dialog == null )
			dialog = new ContentDialog( title, description );

		dialog.showDialog();
	}

	protected abstract String getConfigFile();

	public class ContentDialog extends JDialog
	{
		private JTextArea contentArea;

		public ContentDialog( String title, String description ) throws HeadlessException
		{
			super( UISupport.getMainFrame() );
			setTitle( title );
			setModal( true );

			getContentPane().setLayout( new BorderLayout() );
			JLabel label = new JLabel( description );
			label.setBorder( BorderFactory.createEmptyBorder( 10, 10, 0, 10 ) );
			getContentPane().add( label, BorderLayout.NORTH );
			getContentPane().add( buildContent(), BorderLayout.CENTER );

			ButtonBarBuilder builder = ButtonBarBuilder.createLeftToRightBuilder();
			builder.addGlue();
			JButton closeButton = new JButton( new CloseAction() );
			builder.addFixed( closeButton );

			builder.setBorder( BorderFactory.createEmptyBorder( 0, 10, 10, 10 ) );
			getContentPane().add( builder.getPanel(), BorderLayout.SOUTH );

			pack();

			UISupport.initDialogActions( this, null, closeButton );
		}

		public void showDialog()
		{
			contentArea.setText( getConfigFile() );
			setVisible( true );
		}

		private Component buildContent()
		{
			contentArea = new JTextArea();
			contentArea.setEditable( false );
			contentArea.setBackground( Color.WHITE );
			JScrollPane scrollPane = new JScrollPane( contentArea );
			scrollPane.setPreferredSize( new Dimension( 500, 300 ) );

			return UISupport.wrapInEmptyPanel( scrollPane, BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
		}

		private final class CloseAction extends AbstractAction
		{
			public CloseAction()
			{
				super( "Close" );
			}

			public void actionPerformed( ActionEvent e )
			{
				setVisible( false );
			}
		}
	}
}
