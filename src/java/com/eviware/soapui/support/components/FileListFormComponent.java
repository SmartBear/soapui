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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eviware.soapui.support.UISupport;

public class FileListFormComponent extends JPanel implements JFormComponent, ActionListener
{
	private DefaultListModel listModel;
	private JButton addButton;
	private JButton removeButton;
	private JList list;

	public FileListFormComponent( String tooltip )
	{
		listModel = new DefaultListModel();
		list = new JList( listModel );
		list.setToolTipText( tooltip );
		JScrollPane scrollPane = new JScrollPane( list );
		scrollPane.setPreferredSize( new Dimension( 300, 70 ) );
		add( scrollPane, BorderLayout.CENTER );
		Box box = new Box( BoxLayout.Y_AXIS );
		addButton = new JButton( "Add.." );
		addButton.addActionListener( this );
		box.add( addButton );
		box.add( Box.createVerticalStrut( 5 ) );
		removeButton = new JButton( "Remove.." );
		removeButton.addActionListener( this );
		box.add( removeButton );
		box.add( Box.createVerticalGlue() );

		add( box, BorderLayout.EAST );

		list.addListSelectionListener( new ListSelectionListener()
		{

			public void valueChanged( ListSelectionEvent e )
			{
				removeButton.setEnabled( list.getSelectedIndex() != -1 );
			}
		} );

		removeButton.setEnabled( list.getSelectedIndex() != -1 );
	}

	public void setValue( String value )
	{
		listModel.clear();
		String[] files = value.split( ";" );
		for( String file : files )
			if( file.trim().length() > 0 )
				listModel.addElement( file );
	}

	public String getValue()
	{
		Object[] values = listModel.toArray();
		StringBuffer buf = new StringBuffer();
		for( int c = 0; c < values.length; c++ )
		{
			if( c > 0 )
				buf.append( ';' );

			buf.append( values[c] );
		}

		return buf.toString();
	}

	public void actionPerformed( ActionEvent arg0 )
	{
		if( arg0.getSource() == addButton )
		{
			File file = UISupport.getFileDialogs().open( this, "Add file", null, null, null );
			if( file != null )
			{
				listModel.addElement( file.getAbsolutePath() );
			}
		}
		else if( arg0.getSource() == removeButton && list.getSelectedIndex() != -1 )
		{
			Object elm = listModel.getElementAt( list.getSelectedIndex() );
			if( UISupport.confirm( "Remove [" + elm.toString() + "] from list", "Remove" ) )
			{
				listModel.remove( list.getSelectedIndex() );
			}
		}
	}
}
