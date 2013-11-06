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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringList;

public class StringListFormComponent extends JPanel implements JFormComponent, ActionListener
{
	private DefaultListModel listModel;
	private String defaultValue = null;
	private JButton addButton;
	private JButton removeButton;
	private JList list;
	private JButton editButton;
	private Box buttonBox;
	private List<JButton> buttons = new ArrayList<JButton>();

	public StringListFormComponent( String tooltip )
	{
		this( tooltip, false, null );
	}

	public StringListFormComponent( String tooltip, boolean editOnly )
	{
		this( tooltip, editOnly, null );
	}

	public StringListFormComponent( String tooltip, boolean editOnly, String defaultValue )
	{
		super( new BorderLayout() );

		this.defaultValue = defaultValue;
		listModel = new DefaultListModel();
		list = new JList( listModel );
		list.setToolTipText( tooltip );
		JScrollPane scrollPane = new JScrollPane( list );
		scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		scrollPane.setPreferredSize( new Dimension( 300, 70 ) );
		add( scrollPane, BorderLayout.CENTER );
		buttonBox = new Box( BoxLayout.Y_AXIS );
		buttonBox.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 0 ) );

		if( !editOnly )
		{
			addButton = new JButton( "Add.." );
			addButton.addActionListener( this );
			buttonBox.add( addButton );
			buttonBox.add( Box.createVerticalStrut( 5 ) );
		}

		editButton = new JButton( "Edit.." );
		editButton.addActionListener( this );
		buttons.add( editButton );
		buttonBox.add( editButton );

		if( !editOnly )
		{
			buttonBox.add( Box.createVerticalStrut( 5 ) );
			removeButton = new JButton( "Remove.." );
			removeButton.addActionListener( this );
			buttonBox.add( removeButton );
			buttons.add( removeButton );
		}

		add( buttonBox, BorderLayout.EAST );

		list.addListSelectionListener( new ListSelectionListener()
		{

			public void valueChanged( ListSelectionEvent e )
			{
				setButtonState();
			}
		} );

		setButtonState();
	}

	public void addButton( Action action, boolean requireSelection )
	{
		buttonBox.add( Box.createVerticalStrut( 5 ) );
		JButton button = new JButton( action );
		buttonBox.add( button );

		if( requireSelection )
		{
			buttons.add( button );
			setButtonState();
		}
	}

	public void setValue( String value )
	{
		String[] oldData = getData();
		listModel.clear();

		try
		{
			StringList stringList = StringList.fromXml( value );

			String[] files = stringList.toStringArray();
			for( String file : files )
				if( file.trim().length() > 0 )
					listModel.addElement( file );

			firePropertyChange( "data", oldData, getData() );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	public String getValue()
	{
		StringList result = new StringList( listModel.toArray() );
		return result.toXml();
	}

	public JList getList()
	{
		return list;
	}

	public void actionPerformed( ActionEvent e )
	{
		String[] oldData = getData();

		if( e.getSource() == addButton )
		{
			String value = UISupport.prompt( "Specify value to add", "Add..", defaultValue );
			if( value != null )
			{
				listModel.addElement( value );
				firePropertyChange( "options", oldData, getData() );
			}
		}
		else
		{
			int selectedIndex = list.getSelectedIndex();

			if( e.getSource() == removeButton && selectedIndex != -1 )
			{
				Object elm = listModel.getElementAt( selectedIndex );
				if( UISupport.confirm( "Remove [" + elm.toString() + "] from list", "Remove" ) )
				{
					listModel.remove( selectedIndex );
					firePropertyChange( "options", oldData, getData() );
				}
			}
			else if( e.getSource() == editButton && selectedIndex != -1 )
			{
				String elm = ( String )listModel.getElementAt( selectedIndex );
				String value = UISupport.prompt( "Specify value", "Edit..", elm );

				if( value != null )
				{
					listModel.setElementAt( value, selectedIndex );
					firePropertyChange( "options", oldData, getData() );
				}
			}
		}
	}

	public void setButtonState()
	{
		boolean b = list.getSelectedIndex() != -1;
		for( JButton button : buttons )
			button.setEnabled( b );
	}

	public String[] getData()
	{
		String[] result = new String[listModel.size()];
		for( int c = 0; c < result.length; c++ )
			result[c] = ( String )listModel.get( c );

		return result;
	}

	public void setData( String[] strings )
	{
		String[] oldData = getData();

		listModel.clear();
		if( strings != null )
		{
			for( String str : strings )
			{
				listModel.addElement( str );
			}
		}

		firePropertyChange( "options", oldData, getData() );
	}

	public String[] getOptions()
	{
		return getData();
	}

	public void setOptions( String[] options )
	{
		setData( options );
	}

	public void setEnabled( boolean b )
	{
		addButton.setEnabled( b );
		list.setEnabled( b );
		if( b )
			setButtonState();
	}

	public void addItem( String valueOf )
	{
		listModel.addElement( valueOf );
	}
}
