package com.eviware.soapui.security.ui;

import com.eviware.soapui.impl.rest.panels.resource.RestParamsTableModel;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;

public class SecurityParamsTableModel extends RestParamsTableModel {
	public SecurityParamsTableModel( RestParamsPropertyHolder params )
	{
		super(params);
	}
	
	public int getColumnCount()
	{
		return 2;
	}

	@Override
	public String getColumnName( int column )
	{
		switch( column )
		{
		case 0 :
			return "Name";
		case 1 :
			return "Value";
		}

		return null;
	}

	@Override
	public Class<?> getColumnClass( int columnIndex )
	{
		return String.class;
	}

	public Object getValueAt( int rowIndex, int columnIndex )
	{
		RestParamProperty prop = params.getPropertyAt( rowIndex );

		switch( columnIndex )
		{
		case 0 :
			return prop.getName();
		
		case 1 :
			return prop.getValue();
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
			prop.setValue( value.toString() );
			return;
		}
	}

	@Override
	public boolean isCellEditable( int rowIndex, int columnIndex )
	{
		return false;
	}

}
