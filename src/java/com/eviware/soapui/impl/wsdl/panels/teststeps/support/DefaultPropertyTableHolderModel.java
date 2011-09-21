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

package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import javax.swing.table.AbstractTableModel;

import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.environment.EnvironmentListener;
import com.eviware.soapui.model.environment.Property;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringList;

public class DefaultPropertyTableHolderModel extends AbstractTableModel implements PropertyHolderTableModel,
		EnvironmentListener
{
	private StringList names = new StringList();
	private final TestPropertyHolder holder;

	public DefaultPropertyTableHolderModel( TestPropertyHolder holder )
	{
		this.holder = holder;
		names = new StringList( getPropertyNames() );
	}

	public String[] getPropertyNames()
	{
		return holder.getPropertyNames();
	}

	public int getRowCount()
	{
		return names.size();
	}

	public int getColumnCount()
	{
		return 2;
	}

	@Override
	public void fireTableDataChanged()
	{
		names = new StringList( getPropertyNames() );
		super.fireTableDataChanged();
	}

	public String getColumnName( int columnIndex )
	{
		switch( columnIndex )
		{
		case 0 :
			return "Name";
		case 1 :
			return "Value";
		}

		return null;
	}

	public boolean isCellEditable( int rowIndex, int columnIndex )
	{
		if( columnIndex == 0 )
		{
			return holder instanceof MutableTestPropertyHolder;
		}

		return !holder.getProperty( names.get( rowIndex ) ).isReadOnly();
	}

	public void setValueAt( Object aValue, int rowIndex, int columnIndex )
	{
		TestProperty property = holder.getProperty( names.get( rowIndex ) );
		switch( columnIndex )
		{
		case 0 :
		{
			if( holder instanceof MutableTestPropertyHolder )
			{
				TestProperty prop = holder.getProperty( aValue.toString() );
				if( prop != null && prop != property )
				{
					UISupport.showErrorMessage( "Property name exists!" );
					return;
				}
				( ( MutableTestPropertyHolder )holder ).renameProperty( property.getName(), aValue.toString() );
			}
			break;
		}
		case 1 :
		{
			property.setValue( aValue.toString() );
			break;
		}
		}
	}

	@Override
	public Class<?> getColumnClass( int columnIndex )
	{
		return String.class;
	}

	public TestProperty getPropertyAtRow( int rowIndex )
	{
		return holder.getProperty( names.get( rowIndex ) );
	}

	public Object getValueAt( int rowIndex, int columnIndex )
	{
		TestProperty property = holder.getProperty( names.get( rowIndex ) );
		if( property == null )
			return null;

		switch( columnIndex )
		{
		case 0 :
			return property.getName();
		case 1 :
			return property.getValue();
		}

		return null;
	}

	public void propertyValueChanged( Property property )
	{
		fireTableDataChanged();
	}

}
