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
package com.eviware.soapui.model.security;

import javax.swing.table.DefaultTableModel;

import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.security.support.SecurityCheckedParameterImpl;

@SuppressWarnings( "serial" )
public class SensitiveInformationTableModel extends DefaultTableModel
{

	private String[] columnNames = new String[] { "Token", "Description" };
	private MutableTestPropertyHolder holder;

	public MutableTestPropertyHolder getHolder()
	{
		return holder;
	}

	public SensitiveInformationTableModel( MutableTestPropertyHolder holder )
	{
		this.holder = holder;
	}

	@Override
	public int getColumnCount()
	{
		return 2;
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
		TestProperty param = holder.getPropertyList().get( row );
		switch( column )
		{
		case 0 :
			return param.getName();
		case 1 :
			return param.getValue();

		}
		return super.getValueAt( row, column );
	}

	@Override
	public Class<?> getColumnClass( int columnIndex )
	{
		return String.class;
	}

	@Override
	public void setValueAt( Object aValue, int row, int column )
	{
		if( holder.getPropertyList().isEmpty() )
			return;
		SecurityCheckedParameterImpl param = ( SecurityCheckedParameterImpl )holder.getPropertyList().get( row );
		switch( column )
		{
		case 0 :
			param.setLabel( ( String )aValue );
			break;
		case 1 :
			param.setName( ( String )aValue );
			break;
		
		}
	}

	public void addToken( String token, String description )
	{
		holder.setPropertyValue( token, description );
		fireTableDataChanged();
	}

	@Override
	public int getRowCount()
	{
		return holder == null ? 0 : holder.getPropertyList().size();
	}

	public void removeRows( int[] selectedRows )
	{
	//	holder.getPropertyAt( 0).removeParameters( selectedRows );
	}

}
