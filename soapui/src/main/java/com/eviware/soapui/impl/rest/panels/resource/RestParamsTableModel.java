/*
 *  SoapUI, copyright (C) 2004-2013 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.panels.resource;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.DefaultPropertyTableHolderModel;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation;

public class RestParamsTableModel extends DefaultPropertyTableHolderModel<RestParamsPropertyHolder>
{

	private static final String NAME = "Name";
	private static final String VALUE = "Value";
	private static final String STYLE = "Style";

	public static final int NAME_COLUMN_INDEX = 0;
	public static final int VALUE_COLUMN_INDEX = 1;
	public static final int STYLE_COLUMN_INDEX = 2;
	public static final int LOCATION_COLUMN_INDEX = 3;

	public static enum Mode
	{
		MINIMAL( new String[] { NAME, VALUE }, new Class[] { String.class, String.class } ),
		MEDIUM( new String[] { NAME, VALUE, STYLE }, new Class[] { String.class, String.class, ParameterStyle.class } ),
		FULL( COLUMN_NAMES, COLUMN_TYPES );

		private final String[] columnNames;

		private final Class[] columnTypes;

		private Mode( String[] columnNames, Class[] columnTypes )
		{
			this.columnNames = columnNames;
			this.columnTypes = columnTypes;
		}

	}

	static String[] COLUMN_NAMES = new String[] { NAME, "Default value", STYLE, "Level" };
	static Class[] COLUMN_TYPES = new Class[] { String.class, String.class, ParameterStyle.class, ParamLocation.class };

	private Mode mode;

	public RestParamsTableModel( RestParamsPropertyHolder params, Mode mode )
	{
		super( params );
		this.mode = mode;

		if( params.getModelItem() != null )
		{
			params.getModelItem().addPropertyChangeListener( this );
		}
	}

	public RestParamsTableModel( RestParamsPropertyHolder params )
	{
		this( params, Mode.FULL );
	}

	public boolean isInMinimalMode()
	{
		return mode == Mode.MINIMAL;
	}

	@Override
	public int getColumnCount()
	{
		return mode.columnTypes.length;
	}

	@Override
	public String getColumnName( int columnIndex )
	{
		if( isColumnIndexOutOfBound( columnIndex ) )
		{
			return null;
		}
		return mode.columnNames[columnIndex];
	}

	@Override
	public Class<?> getColumnClass( int columnIndex )
	{
		if( isColumnIndexOutOfBound( columnIndex ) )
		{
			return null;
		}
		return mode.columnTypes[columnIndex];
	}

	private boolean isColumnIndexOutOfBound( int columnIndex )
	{
		return columnIndex < 0 || columnIndex >= mode.columnTypes.length;
	}

	@Override
	public boolean isCellEditable( int rowIndex, int columnIndex )
	{
		return true;
	}

	public ParamLocation getParamLocationAt( int rowIndex )
	{
		return getParameterAt( rowIndex ).getParamLocation();
	}


	@Override
	public Object getValueAt( int rowIndex, int columnIndex )
	{
		RestParamProperty prop = getParameterAt( rowIndex );

		switch( columnIndex )
		{
			case NAME_COLUMN_INDEX:
				return prop.getName();
			case VALUE_COLUMN_INDEX:
				return prop.getValue();
			case STYLE_COLUMN_INDEX:
				return mode == Mode.MINIMAL ? null : prop.getStyle();
			case LOCATION_COLUMN_INDEX:
				return mode != Mode.FULL ? null : prop.getParamLocation();
		}

		return null;
	}

	@Override
	public void setValueAt( Object value, int rowIndex, int columnIndex )
	{
		RestParamProperty prop = getParameterAt( rowIndex );

		switch( columnIndex )
		{
			case NAME_COLUMN_INDEX:
				if( propertyExists( value, prop ) )
				{
					return;
				}

				params.renameProperty( prop.getName(), value.toString() );
				return;
			case VALUE_COLUMN_INDEX:
				//if( !prop.getParamLocation().equals( ParamLocation.REQUEST ) )
				//{
				prop.setDefaultValue( value.toString() );
				//}
				prop.setValue( value.toString() );
				return;
			case STYLE_COLUMN_INDEX:
				if( mode != Mode.MINIMAL )
				{
					prop.setStyle( ( ParameterStyle )value );
				}
				return;
			case LOCATION_COLUMN_INDEX:
				if( mode == Mode.FULL )
				{
					if( params.getModelItem() != null && params.getModelItem() instanceof RestRequest )
					{
						this.isLastChangeParameterLevelChange = true;
					}
					params.setParameterLocation( prop, ( ParamLocation )value );
				}
		}
	}

	public RestParamProperty getParameterAt( int selectedRow )
	{
		return ( RestParamProperty )super.getPropertyAtRow( selectedRow );
	}

	public ParamLocation[] getParameterLevels()
	{
		return ParamLocation.values();
	}

	public void setParams( RestParamsPropertyHolder params )
	{
		this.params.removeTestPropertyListener( testPropertyListener );
		this.params = params;
		this.params.addTestPropertyListener( testPropertyListener );

		fireTableDataChanged();
	}

	public void removeProperty( String propertyName )
	{
		params.remove( propertyName );
	}


}
