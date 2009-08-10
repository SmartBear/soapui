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
package com.eviware.soapui.impl.rest.panels.method;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.types.StringList;

public class RestRepresentationsTable extends JPanel implements PropertyChangeListener
{
	private RestMethod restMethod;
	private List<RestRepresentation.Type> types;
	private JTable representationsTable;
	private RepresentationsTableModel tableModel;
	private AddRepresentationAction addRepresentationAction;
	private RemoveRepresentationAction removeRepresentationAction;
	private boolean readOnly;

	public RestRepresentationsTable( RestMethod restMethod, RestRepresentation.Type[] types, boolean readOnly )
	{
		super( new BorderLayout() );
		this.restMethod = restMethod;
		this.types = Arrays.asList( types );
		this.readOnly = readOnly;

		tableModel = new RepresentationsTableModel();
		representationsTable = new JTable( tableModel );
		representationsTable.setRowHeight( 18 );

		add( buildToolbar(), BorderLayout.NORTH );
		add( new JScrollPane( representationsTable ), BorderLayout.CENTER );

		restMethod.addPropertyChangeListener( "representations", this );

	}

	protected JXToolBar buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();
		if( !readOnly )
		{
			addRepresentationAction = new AddRepresentationAction();
			toolbar.addFixed( UISupport.createToolbarButton( addRepresentationAction ) );

			removeRepresentationAction = new RemoveRepresentationAction();
			removeRepresentationAction.setEnabled( false );
			representationsTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
			{
				public void valueChanged( ListSelectionEvent e )
				{
					removeRepresentationAction.setEnabled( representationsTable.getSelectedRow() != -1 );
				}
			} );
			toolbar.addFixed( UISupport.createToolbarButton( removeRepresentationAction ) );
		}

		return toolbar;
	}

	public class RepresentationsTableModel extends AbstractTableModel implements PropertyChangeListener
	{
		private List<RestRepresentation> data = new ArrayList<RestRepresentation>();

		public RepresentationsTableModel()
		{
			initData();
		}

		private void initData()
		{
			if( !data.isEmpty() )
			{
				release();
				data.clear();
			}

			for( RestRepresentation representation : restMethod.getRepresentations() )
			{
				if( types.contains( representation.getType() ) )
				{
					representation.addPropertyChangeListener( this );
					data.add( representation );
				}
			}
		}

		public int getColumnCount()
		{
			return 4;
		}

		public int getRowCount()
		{
			return data.size();
		}

		public Object getValueAt( int rowIndex, int columnIndex )
		{
			RestRepresentation representation = data.get( rowIndex );

			switch( columnIndex )
			{
			case 0 :
				return representation.getType().toString();
			case 1 :
				return representation.getMediaType();
			case 2 :
				return representation.getType().equals( RestRepresentation.Type.REQUEST ) ? "n/a" : representation
						.getStatus().toString();
			case 3 :
				return representation.getElement() == null ? null : representation.getElement().toString();
			}

			return null;
		}

		@Override
		public boolean isCellEditable( int rowIndex, int columnIndex )
		{
			return !readOnly && columnIndex > 0 && columnIndex < 3
					&& !( data.get( rowIndex ).getType().equals( RestRepresentation.Type.REQUEST ) && columnIndex == 2 );
		}

		@Override
		public void setValueAt( Object value, int rowIndex, int columnIndex )
		{
			if( readOnly )
				return;
			RestRepresentation representation = data.get( rowIndex );

			switch( columnIndex )
			{
			case 1 :
				representation.setMediaType( value == null ? "" : value.toString() );
				break;
			case 2 :
			{
				if( value == null )
					value = "";

				String[] items = value.toString().split( " " );
				List<Integer> status = new ArrayList<Integer>();

				for( String item : items )
				{
					try
					{
						if( StringUtils.hasContent( item ) )
							status.add( Integer.parseInt( item.trim() ) );
					}
					catch( NumberFormatException e )
					{
					}
				}

				representation.setStatus( status );
				break;
			}
			}
		}

		@Override
		public String getColumnName( int column )
		{
			switch( column )
			{
			case 0 :
				return "Type";
			case 1 :
				return "Media-Type";
			case 2 :
				return "Status Codes";
			case 3 :
				return "QName";
			}

			return null;
		}

		public void refresh()
		{
			initData();
			fireTableDataChanged();
		}

		public void propertyChange( PropertyChangeEvent evt )
		{
			fireTableDataChanged();
		}

		public void release()
		{
			for( RestRepresentation representation : data )
			{
				representation.removePropertyChangeListener( this );
			}
		}

		public RestRepresentation getRepresentationAtRow( int rowIndex )
		{
			return data.get( rowIndex );
		}
	}

	public RestRepresentation getRepresentationAtRow( int rowIndex )
	{
		return tableModel.getRepresentationAtRow( rowIndex );
	}

	private class AddRepresentationAction extends AbstractAction
	{
		private AddRepresentationAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/add_property.gif" ) );
			putValue( SHORT_DESCRIPTION, "Adds a new Response Representation to this Method" );
		}

		public void actionPerformed( ActionEvent e )
		{
			String type = types.size() == 1 ? types.get( 0 ).toString() : UISupport.prompt(
					"Specify type of Representation to add", "Add Representation", new StringList( types ).toStringArray() );

			if( type != null )
			{
				restMethod.addNewRepresentation( RestRepresentation.Type.valueOf( type ) );
			}
		}
	}

	private class RemoveRepresentationAction extends AbstractAction
	{
		private RemoveRepresentationAction()
		{
			putValue( SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
			putValue( SHORT_DESCRIPTION, "Removes selected Representation from this Method" );
		}

		public void actionPerformed( ActionEvent e )
		{
			if( UISupport.confirm( "Remove selected Representation?", "Remove Representation" ) )
			{
				restMethod
						.removeRepresentation( tableModel.getRepresentationAtRow( representationsTable.getSelectedRow() ) );
			}
		}
	}

	public void propertyChange( PropertyChangeEvent arg0 )
	{
		tableModel.refresh();
	}

	public void release()
	{
		tableModel.release();
	}

	public void refresh()
	{
		tableModel.refresh();
	}

	public int getSelectedRow()
	{
		return representationsTable.getSelectedRow();
	}
}
