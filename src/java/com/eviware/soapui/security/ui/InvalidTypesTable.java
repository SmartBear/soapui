/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import com.eviware.soapui.config.InvalidSecurityCheckConfig;
import com.eviware.soapui.config.SchemaTypeForSecurityCheckConfig;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;

/**
 * Table for handling schema types for InvalidTypes Security Check
 * 
 * @author robert
 * 
 */
public class InvalidTypesTable extends JPanel
{

	private InvalidTypeTableModel model;
	private JXTable table;
	private JXToolBar toolbar;

	public InvalidTypesTable( InvalidSecurityCheckConfig invalidTypeConfig )
	{
		this.model = new InvalidTypeTableModel( invalidTypeConfig );
		init();
	}

	private void init()
	{
		setLayout( new BorderLayout() );
		toolbar = UISupport.createToolbar();

		toolbar.add( UISupport.createToolbarButton( new AddNewTypeAction() ) );
		toolbar.add( UISupport.createToolbarButton( new RemoveTypeAction() ) );
		toolbar.addGlue();

		add( toolbar, BorderLayout.NORTH );
		table = new JXTable( model );
		add( new JScrollPane( table ), BorderLayout.CENTER );

	}

	private class RemoveTypeAction extends AbstractAction
	{

		public RemoveTypeAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Removes type from security check" );
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			model.removeRows( table.getSelectedRows() );
		}

	}

	private class AddNewTypeAction extends AbstractAction
	{

		public AddNewTypeAction()
		{
			putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( Action.SHORT_DESCRIPTION, "Adds new type to use in security check" );
		}

		@Override
		public void actionPerformed( ActionEvent arg0 )
		{
			model.addNewType();
		}

	}

	private class InvalidTypeTableModel extends AbstractTableModel
	{

		private InvalidSecurityCheckConfig data;
		private String[] columns = { "Type Name", "Type Value" };

		public InvalidTypeTableModel( InvalidSecurityCheckConfig invalidTypeConfig )
		{
			this.data = invalidTypeConfig;
		}

		public void removeRows( int[] selectedRows )
		{
			List toRemove = new ArrayList();
			for( int index : selectedRows )
				toRemove.add( data.getTypesListList().get( index ) );
			data.getTypesListList().removeAll( toRemove );
			fireTableDataChanged();
		}

		public void addNewType()
		{
			SchemaTypeForSecurityCheckConfig newtype = data.addNewTypesList();
			newtype.setType( -1 );
			newtype.setValue( "fill with new value" );

			fireTableDataChanged();
		}

		@Override
		public boolean isCellEditable( int rowIndex, int columnIndex )
		{
			return true;
		}

		@Override
		public void setValueAt( Object aValue, int rowIndex, int columnIndex )
		{
			SchemaTypeForSecurityCheckConfig paramType = data.getTypesListList().get( rowIndex );

			if( columnIndex == 0 )
				try {
					paramType.setType( Integer.parseInt( ( String )aValue ) );
				} catch(NumberFormatException e) {
					UISupport.showErrorMessage( "Type have be integer!" );
				}
				else
					paramType.setValue( ( String )aValue );

			fireTableDataChanged();
		}

		@Override
		public String getColumnName( int column )
		{
			return columns[column];
		}

		@Override
		public int getColumnCount()
		{
			return 2;
		}

		@Override
		public int getRowCount()
		{
			if( data.getTypesListList() == null )
				return 0;
			return data.getTypesListList().size();
		}

		@Override
		public Object getValueAt( int rowIndex, int columnIndex )
		{
			if( columnIndex == 0 )
			{
				// XmlOptions options = new XmlOptions();
				// options.setDocumentType( data.getTypesListList().get( rowIndex
				// ).getType() );
				// XmlObject.Factory.newDomImplementation(XmlOptions)
				return data.getTypesListList().get( rowIndex ).getType();
			}
			else
				return data.getTypesListList().get( rowIndex ).getValue();
		}

	}
}
