/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
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

import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import com.eviware.soapui.model.testsuite.TestPropertyListener;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.util.HashMap;
import java.util.Map;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation;

public class RestParamsTableModel extends AbstractTableModel implements TableModel, TestPropertyListener
{
	public static final int PARAM_LOCATION_COLUMN_INDEX = 3;
	protected RestParamsPropertyHolder params;

	static String[] COLUMN_NAMES = new String[] { "Name", "Default value", "Style", "Level" };
	static Class[] COLUMN_TYPES = new Class[] { String.class, String.class, ParameterStyle.class, ParamLocation.class };

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
		return 4;
	}

	@Override
	public String getColumnName( int columnIndex )
	{
		if( isColumnIndexOutOfBound( columnIndex ) )
		{
			return null;
		}
		return COLUMN_NAMES[columnIndex];
	}

	@Override
	public Class<?> getColumnClass( int columnIndex )
	{
		if( isColumnIndexOutOfBound( columnIndex ) )
		{
			return null;
		}
		return COLUMN_TYPES[columnIndex];
	}

	private boolean isColumnIndexOutOfBound( int columnIndex )
	{
		return ( columnIndex < 0 ) || ( columnIndex > 3 );
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

	public ParamLocation getParamLocationAt( int rowIndex )
	{
		return ( ParamLocation )getValueAt( rowIndex, PARAM_LOCATION_COLUMN_INDEX );
	}


	public Object getValueAt( int rowIndex, int columnIndex )
	{
		RestParamProperty prop = params.getPropertyAt( rowIndex );

		switch( columnIndex )
		{
			case 0:
				return prop.getName();
			case 1:
				return prop.getValue();
			case 2:
				return prop.getStyle();
			case 3:
				return prop.getParamLocation();
		}

		return null;
	}

	@Override
	public void setValueAt( Object value, int rowIndex, int columnIndex )
	{
		RestParamProperty prop = params.getPropertyAt( rowIndex );

		switch( columnIndex )
		{
			case 0:
				params.renameProperty( prop.getName(), value.toString() );
				return;
			case 1:
				if( !prop.getParamLocation().equals( ParamLocation.REQUEST ) )
				{
					prop.setDefaultValue( value.toString() );
				}
				prop.setValue( value.toString() );
				return;
			case 2:
				prop.setStyle( ( ParameterStyle )value );
				return;
			case 3:
				prop.setParamLocation(  ( ParamLocation )value );
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

	public ParameterStyle[] getParameterStylesForEdit()
	{
		return new ParameterStyle[] { ParameterStyle.QUERY, ParameterStyle.TEMPLATE, ParameterStyle.HEADER,
				ParameterStyle.MATRIX, ParameterStyle.PLAIN };
	}

	public ParamLocation[] getParameterLevels()
	{
		return ParamLocation.values();
	}

	public void setParams( RestParamsPropertyHolder params )
	{
		this.params.removeTestPropertyListener( this );
		this.params = params;
		this.params.addTestPropertyListener( this );

		fireTableDataChanged();
	}

	public void removeProperty( String propertyName )
	{
		params.remove( propertyName );
	}
}
