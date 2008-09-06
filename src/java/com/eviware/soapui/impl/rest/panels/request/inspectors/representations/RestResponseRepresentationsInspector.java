/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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

import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import org.jdesktop.swingx.JXTable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class RestResponseRepresentationsInspector extends AbstractXmlInspector implements PropertyChangeListener
{
	private JPanel mainPanel;
	private final RestRequest request;
	private JXTable representationsTable;
	private ResponseRepresentationsTableModel tableModel;

	protected RestResponseRepresentationsInspector( RestRequest request )
	{
		super( "Rep", "Response Representations", true, RestRepresentationsInspectorFactory.INSPECTOR_ID );
		this.request = request;
		
		request.addPropertyChangeListener("representations", this);
	}

	public JComponent getComponent()
	{
		if( mainPanel == null )
		{
			mainPanel = new JPanel( new BorderLayout() );
			tableModel = new ResponseRepresentationsTableModel();
			representationsTable = new JXTable( tableModel );
			mainPanel.add( new JScrollPane( representationsTable ), BorderLayout.CENTER );
		}

		return mainPanel;
	}
	
	@Override
	public boolean isEnabledFor( EditorView<XmlDocument> view )
	{
		return !view.getViewId().equals( RawXmlEditorFactory.VIEW_ID );
	}
	
	public class ResponseRepresentationsTableModel extends AbstractTableModel
	{
		List<RestRepresentation> data = new ArrayList<RestRepresentation>();
		
		public ResponseRepresentationsTableModel()
		{
			initData();
		}
		
		private void initData()
		{
			data.clear();
			
			for( RestRepresentation representation : request.getRepresentations( null, null ))
			{
				if( representation.getType() != RestRepresentation.Type.REQUEST )
					data.add( representation );
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

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			RestRepresentation representation = data.get( rowIndex );
			
			switch( columnIndex )
			{
			case 0 : return representation.getType().toString();
			case 1 : return representation.getMediaType();
			case 2 : return representation.getStatus().toString();
         case 3 : return representation.getElement() == null ? null : representation.getElement().toString();
			}
			
			return null;
		}
		

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return columnIndex > 0 && columnIndex < 3;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex)
		{
			RestRepresentation representation = data.get( rowIndex );
			
			switch( columnIndex )
			{
			case 1 : representation.setMediaType(value == null ? "" : value.toString()); break;
			case 2 : 
				{
					if( value == null )
						value = "";
					
					String[] items = value.toString().split( "," );
					List<Integer> status = new ArrayList<Integer>();
					
					for( String item : items )
					{
						try
						{
							if( StringUtils.hasContent(item))
								status.add( Integer.parseInt( item.trim() ));
						}
						catch (NumberFormatException e)
						{
						}
					}
					
					representation.setStatus( status );
					break;
				}
			}
		}

		@Override
		public String getColumnName(int column)
		{
			switch( column )
			{
			case 0 : return "Type";
			case 1 : return "Media-Type";
			case 2 : return "Status Codes";
         case 3 : return "QName";
         }
			
			return null;
		}

		public void refresh()
		{
			initData();
			fireTableDataChanged();
		}
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		tableModel.refresh();
	}
}
