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

package com.eviware.soapui.impl.wsdl.support.wss.support;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.eviware.soapui.impl.wsdl.support.wss.entries.WssEntryBase;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.types.StringToStringMap;

public class WSPartsTable extends JPanel
{
	private final List<StringToStringMap> parts;
	private WssEntryBase entry;
	private PartsTableModel partsTableModel;
	private JTable partsTable;
	private JButton removePartButton;

	public WSPartsTable( List<StringToStringMap> parts, WssEntryBase entry )
	{
		super( new BorderLayout() );
		this.parts = parts;
		this.entry = entry;

		partsTableModel = new PartsTableModel();
		partsTable = new JTable( partsTableModel );
		partsTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
		{

			public void valueChanged( ListSelectionEvent e )
			{
				removePartButton.setEnabled( partsTable.getSelectedRow() != -1 );
			}
		} );

		partsTable.getColumnModel().getColumn( 3 ).setCellEditor(
				new DefaultCellEditor( new JComboBox( new String[] { "Content", "Element" } ) ) );

		JScrollPane scrollPane = new JScrollPane( partsTable );
		scrollPane.setBackground( Color.WHITE );
		scrollPane.setOpaque( true );
		add( scrollPane, BorderLayout.CENTER );
		add( buildToolbar(), BorderLayout.NORTH );

		setPreferredSize( new Dimension( 350, 150 ) );
	}

	private Component buildToolbar()
	{
		JXToolBar toolbar = UISupport.createSmallToolbar();

		toolbar.addFixed( UISupport.createToolbarButton( new AddPartAction() ) );
		removePartButton = UISupport.createToolbarButton( new RemovePartAction() );
		toolbar.addFixed( removePartButton );

		return toolbar;
	}

	private class PartsTableModel extends AbstractTableModel
	{
		public int getColumnCount()
		{
			return 4;
		}

		public int getRowCount()
		{
			return parts.size();
		}

		@Override
		public boolean isCellEditable( int rowIndex, int columnIndex )
		{
			return true;
		}

		@Override
		public String getColumnName( int column )
		{
			switch( column )
			{
			case 0 :
				return "ID";
			case 1 :
				return "Name";
			case 2 :
				return "Namespace";
			case 3 :
				return "Encode";
			}

			return null;
		}

		@Override
		public void setValueAt( Object aValue, int rowIndex, int columnIndex )
		{
			StringToStringMap part = parts.get( rowIndex );
			if( aValue == null )
				aValue = "";

			switch( columnIndex )
			{
			case 0 :
				part.put( "name", "" );
				fireTableCellUpdated( rowIndex, 1 );
				part.put( "id", aValue.toString() );
				break;
			case 1 :
				part.put( "id", "" );
				fireTableCellUpdated( rowIndex, 0 );
				part.put( "name", aValue.toString() );
				break;
			case 2 :
				part.put( "id", "" );
				fireTableCellUpdated( rowIndex, 0 );
				part.put( "namespace", aValue.toString() );
				break;
			case 3 :
				part.put( "enc", aValue.toString() );
				break;
			}

			entry.saveConfig();
		}

		public Object getValueAt( int rowIndex, int columnIndex )
		{
			StringToStringMap part = parts.get( rowIndex );

			switch( columnIndex )
			{
			case 0 :
				return part.get( "id" );
			case 1 :
				return part.get( "name" );
			case 2 :
				return part.get( "namespace" );
			case 3 :
				return part.get( "enc" );
			}

			return null;
		}

		public void remove( int row )
		{
			parts.remove( row );
			fireTableRowsDeleted( row, row );
		}

		public void addParts( StringToStringMap map )
		{
			parts.add( map );
			fireTableRowsInserted( parts.size() - 1, parts.size() - 1 );
		}
	}

	private class AddPartAction extends AbstractAction
	{
		public AddPartAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( SHORT_DESCRIPTION, "Adds a new part" );
		}

		public void actionPerformed( ActionEvent e )
		{
			partsTableModel.addParts( new StringToStringMap() );
			entry.saveConfig();
		}
	}

	private class RemovePartAction extends AbstractAction
	{
		public RemovePartAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( SHORT_DESCRIPTION, "Removes the selected part" );
			setEnabled( false );
		}

		public void actionPerformed( ActionEvent e )
		{
			int row = partsTable.getSelectedRow();
			if( row == -1 )
				return;

			if( UISupport.confirm( "Remove selected Part?", "Remove Part" ) )
			{
				partsTableModel.remove( row );
				entry.saveConfig();
			}
		}
	}
}
