/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.panels.resource;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import com.eviware.soapui.model.testsuite.TestPropertyListener;

public class RestParamsTableModel extends AbstractTableModel implements TableModel, TestPropertyListener
{
	protected RestParamsPropertyHolder params;

	public RestParamsTableModel( RestParamsPropertyHolder params )
	{
		this.params = params;

		params.addTestPropertyListener( this );
	}

	public void release()
	{
		params.removeTestPropertyListener( this );
	}

	public int getColumnCount()
	{
		return 3;
	}

	@Override
	public String getColumnName( int column )
	{
		switch( column )
		{
		case 0 :
			return "Name";
		case 1 :
			return "Default value";
		case 2 :
			return "Style";
		}

		return null;
	}

	@Override
	public Class<?> getColumnClass( int columnIndex )
	{
		return columnIndex < 2 ? String.class : ParameterStyle.class;
	}

	@Override
	public boolean isCellEditable( int rowIndex, int columnIndex )
	{
		return true;
	}

	public int getRowCount()
	{
		return params.getPropertyCount();
	}

	public Object getValueAt( int rowIndex, int columnIndex )
	{
		RestParamProperty prop = params.getPropertyAt( rowIndex );

		switch( columnIndex )
		{
		case 0 :
			return prop.getName();
		case 1 :
			return prop.getDefaultValue();
			// case 1 : return StringUtils.hasContent(prop.getValue()) ?
			// prop.getValue() : prop.getDefaultValue();
		case 2 :
			return prop.getStyle();
		}

		return null;
	}

	@Override
	public void setValueAt( Object value, int rowIndex, int columnIndex )
	{
		RestParamProperty prop = params.getPropertyAt( rowIndex );

		switch( columnIndex )
		{
		case 0 :
			params.renameProperty( prop.getName(), value.toString() );
			return;
		case 1 :
			prop.setDefaultValue( value.toString() );
			prop.setValue( value.toString() );
			return;
		case 2 :
			prop.setStyle( ( ParameterStyle )value );
			return;
		}
	}

	public RestParamProperty getParameterAt( int selectedRow )
	{
		return params.getPropertyAt( selectedRow );
	}

	public void propertyAdded( String name )
	{
		fireTableDataChanged();
	}

	public void propertyRemoved( String name )
	{
		fireTableDataChanged();
	}

	public void propertyRenamed( String oldName, String newName )
	{
		fireTableDataChanged();
	}

	public void propertyValueChanged( String name, String oldValue, String newValue )
	{
		fireTableCellUpdated( params.getPropertyIndex( name ), 1 );
	}

	public void propertyMoved( String name, int oldIndex, int newIndex )
	{
		fireTableDataChanged();
	}

	public void setParams( RestParamsPropertyHolder params )
	{
		this.params.removeTestPropertyListener( this );
		this.params = params;
		this.params.addTestPropertyListener( this );

		fireTableDataChanged();
	}
}