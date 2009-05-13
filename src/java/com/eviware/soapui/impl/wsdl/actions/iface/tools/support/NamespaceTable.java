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
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm.ToolkitType;
import com.eviware.x.impl.swing.AbstractSwingXFormField;

/**
 * Swing JTable for holding Namespace/Package mappings
 * 
 * @author ole.matzura
 */

public class NamespaceTable extends AbstractSwingXFormField<JPanel>
{
	private JTable table;
	private JScrollPane scrollPane;
	private final WsdlInterface iface;
	private NamespaceTableModel namespaceTableModel;
	private boolean returnEmpty;

	public NamespaceTable( WsdlInterface iface )
	{
		super( new JPanel( new BorderLayout() ) );

		this.iface = iface;

		namespaceTableModel = new NamespaceTableModel();
		table = new JTable( namespaceTableModel );
		scrollPane = new JScrollPane( table );
		scrollPane.setPreferredSize( new Dimension( 400, 150 ) );
		getComponent().add( scrollPane, BorderLayout.CENTER );
	}

	public void setReturnEmpty( boolean returnEmpty )
	{
		this.returnEmpty = returnEmpty;
	}

	public JPanel getComponent( ToolkitType toolkitType )
	{
		if( toolkitType == ToolkitType.SWT )
		{
			UISupport.showErrorMessage( "SWT not supported by namespace table" );
			return null;
		}

		return getComponent();
	}

	public void setValue( String value )
	{
		namespaceTableModel.setMappings( StringToStringMap.fromXml( value ) );
	}

	public String getValue()
	{
		return namespaceTableModel.getMappings().toXml();
	}

	private class NamespaceTableModel extends AbstractTableModel
	{
		private List<String> namespaces = new ArrayList<String>();
		private List<String> packages;

		public NamespaceTableModel()
		{
			try
			{
				if( iface != null )
					namespaces.addAll( iface.getWsdlContext().getInterfaceDefinition().getDefinedNamespaces() );
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}

			packages = new ArrayList<String>( Arrays.asList( new String[namespaces.size()] ) );
		}

		public void setMappings( StringToStringMap mapping )
		{
			for( int c = 0; c < namespaces.size(); c++ )
			{
				if( mapping.containsKey( namespaces.get( c ) ) )
				{
					packages.set( c, mapping.get( namespaces.get( c ) ) );
				}
				else
				{
					packages.set( c, "" );
				}
			}

			fireTableDataChanged();
		}

		public int getRowCount()
		{
			return namespaces.size();
		}

		public int getColumnCount()
		{
			return 2;
		}

		public Class<?> getColumnClass( int columnIndex )
		{
			return String.class;
		}

		public String getColumnName( int column )
		{
			return column == 0 ? "Namespace" : "Package";
		}

		public boolean isCellEditable( int rowIndex, int columnIndex )
		{
			return columnIndex == 1;
		}

		public void setValueAt( Object aValue, int rowIndex, int columnIndex )
		{
			if( columnIndex == 1 )
				packages.set( rowIndex, aValue.toString() );
		}

		public Object getValueAt( int rowIndex, int columnIndex )
		{
			if( columnIndex == 0 )
				return namespaces.get( rowIndex );
			else
				return packages.get( rowIndex );
		}

		public StringToStringMap getMappings()
		{
			StringToStringMap result = new StringToStringMap();
			for( int c = 0; c < namespaces.size(); c++ )
			{
				String pkg = packages.get( c );
				if( returnEmpty || ( pkg != null && pkg.trim().length() > 0 ) )
				{
					result.put( namespaces.get( c ), pkg == null ? "" : pkg.trim() );
				}
			}

			return result;
		}
	}

	@Override
	public boolean isMultiRow()
	{
		return true;
	}
}