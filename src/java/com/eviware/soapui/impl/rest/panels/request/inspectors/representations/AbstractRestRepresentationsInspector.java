/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

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

package com.eviware.soapui.impl.rest.panels.request.inspectors.representations;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.types.StringList;

public abstract class AbstractRestRepresentationsInspector extends AbstractXmlInspector implements
		PropertyChangeListener
{
	private JPanel mainPanel;
	private final RestRequest request;
	private JTable representationsTable;
	private RepresentationsTableModel tableModel;
	private AbstractRestRepresentationsInspector.AddRepresentationAction addRepresentationAction;
	private AbstractRestRepresentationsInspector.RemoveRepresentationAction removeRepresentationAction;
	private List<RestRepresentation.Type> types;

	protected AbstractRestRepresentationsInspector( RestRequest request, String name, String description,
			RestRepresentation.Type[] types )
	{
		super( name, description, true, RestRepresentationsInspectorFactory.INSPECTOR_ID );
		this.request = request;
		this.types = Arrays.asList( types );

		request.addPropertyChangeListener( "representations", this );
		updateLabel();
	}

	public JComponent getComponent()
	{
		if( mainPanel == null )
		{
			buildUI();
		}

		return mainPanel;
	}

	protected void buildUI()
	{
		mainPanel = new JPanel( new BorderLayout() );
		tableModel = new RepresentationsTableModel();
		representationsTable = new JTable( tableModel );
		representationsTable.setRowHeight( 18 );
		mainPanel.add( buildToolbar(), BorderLayout.NORTH );
		mainPanel.add( new JScrollPane( representationsTable ), BorderLayout.CENTER );

		representationsTable.getSelectionModel().addListSelectionListener( new ListSelectionListener()
		{
			public void valueChanged( ListSelectionEvent e )
			{
				removeRepresentationAction.setEnabled( representationsTable.getSelectedRow() != -1 );
			}
		} );
	}

	protected JXToolBar buildToolbar()
	{
		JXToolBar toolbar = UISupport.createToolbar();

		addRepresentationAction = new AbstractRestRepresentationsInspector.AddRepresentationAction();
		toolbar.addFixed( UISupport.createToolbarButton( addRepresentationAction ) );
		removeRepresentationAction = new AbstractRestRepresentationsInspector.RemoveRepresentationAction();
		toolbar.addFixed( UISupport.createToolbarButton( removeRepresentationAction ) );

		return toolbar;
	}

	public RestRequest getRequest()
	{
		return request;
	}

	@Override
	public boolean isEnabledFor( EditorView<XmlDocument> view )
	{
		return !view.getViewId().equals( RawXmlEditorFactory.VIEW_ID );
	}

	public boolean beforeSubmit( Submit submit, SubmitContext context )
	{
		return true;
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

			for( RestRepresentation representation : request.getRepresentations( null, null ) )
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
			return columnIndex > 0 && columnIndex < 3
					&& !( data.get( rowIndex ).getType().equals( RestRepresentation.Type.REQUEST ) && columnIndex == 2 );
		}

		@Override
		public void setValueAt( Object value, int rowIndex, int columnIndex )
		{
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

	@Override
	public void release()
	{
		tableModel.release();
		request.removePropertyChangeListener( "representations", this );
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		tableModel.refresh();
		updateLabel();
	}

	private void updateLabel()
	{
		int cnt = 0;
		for( RestRepresentation representation : request.getRepresentations() )
		{
			if( types.contains( representation.getType() ) )
				cnt++ ;
		}

		setTitle( "Representations (" + cnt + ")" );
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
				request.addNewRepresentation( RestRepresentation.Type.valueOf( type ) );
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
				request.removeRepresentation( tableModel.getRepresentationAtRow( representationsTable.getSelectedRow() ) );
			}
		}
	}

}