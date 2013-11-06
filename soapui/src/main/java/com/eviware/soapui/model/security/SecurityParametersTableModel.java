/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.model.security;

import javax.swing.table.DefaultTableModel;

import com.eviware.soapui.security.support.SecurityCheckedParameterHolder;
import com.eviware.soapui.security.support.SecurityCheckedParameterImpl;

@SuppressWarnings( "serial" )
public class SecurityParametersTableModel extends DefaultTableModel
{

	private String[] columnNames = new String[] { "Label", "Name", "XPath", "Enabled" };
	private SecurityCheckedParameterHolder holder;

	public SecurityParametersTableModel( SecurityCheckedParameterHolder holder )
	{
		this.holder = holder;
	}

	@Override
	public int getColumnCount()
	{
		return 4;
	}

	@Override
	public String getColumnName( int column )
	{
		return columnNames[column];
	}

	@Override
	public boolean isCellEditable( int row, int column )
	{
		return column != 1;

	}

	@Override
	public Object getValueAt( int row, int column )
	{
		SecurityCheckedParameter param = holder.getParameterList().get( row );
		switch( column )
		{
		case 0 :
			return param.getLabel();
		case 1 :
			return param.getName();
		case 2 :
			return param.getXpath();
		case 3 :
			return param.isChecked();
		}
		return super.getValueAt( row, column );
	}

	@Override
	public Class<?> getColumnClass( int columnIndex )
	{
		return columnIndex == 3 ? Boolean.class : columnIndex == 2 ? String.class : Object.class;
	}

	@Override
	public void setValueAt( Object aValue, int row, int column )
	{
		if( holder.getParameterList().isEmpty() )
			return;
		SecurityCheckedParameterImpl param = ( SecurityCheckedParameterImpl )holder.getParameterList().get( row );
		switch( column )
		{
		case 0 :
			param.setLabel( ( String )aValue );
			break;
		case 1 :
			param.setName( ( String )aValue );
			break;
		case 2 :
			param.setXpath( ( String )aValue );
			break;
		case 3 :
			param.setChecked( ( Boolean )aValue );
		}
	}

	public boolean addParameter( String label, String name, String xpath )
	{
		if( holder.addParameter( label, name, xpath, true ) )
		{
			fireTableDataChanged();
			return true;
		}
		else
			return false;
	}

	@Override
	public int getRowCount()
	{
		return holder == null ? 0 : holder.getParameterList().size();
	}

	public void removeRows( int[] selectedRows )
	{
		holder.removeParameters( selectedRows );
	}

}
