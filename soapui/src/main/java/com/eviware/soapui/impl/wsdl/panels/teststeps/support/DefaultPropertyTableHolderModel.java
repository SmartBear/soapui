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

package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.environment.EnvironmentListener;
import com.eviware.soapui.model.environment.Property;
import com.eviware.soapui.model.support.TestPropertyUtils;
import com.eviware.soapui.model.testsuite.EvaluatedOnReadTestProperty;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringList;

import javax.swing.table.AbstractTableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;

public class DefaultPropertyTableHolderModel<T extends TestPropertyHolder> extends AbstractTableModel implements PropertyHolderTableModel,
		EnvironmentListener, PropertyChangeListener
{
	protected final InternalTestPropertyListener testPropertyListener;
	protected StringList paramNameIndex = new StringList();
	protected T params;
	protected boolean isLastChangeParameterLevelChange = false;

	public DefaultPropertyTableHolderModel( T holder )
	{
		this.params = holder;
		buildParamNameIndex( holder );

		testPropertyListener = new InternalTestPropertyListener();
		holder.addTestPropertyListener( testPropertyListener );
	}

	protected void buildParamNameIndex( TestPropertyHolder holder )
	{
		paramNameIndex = new StringList( getPropertyNames() );
	}

	protected String[] getPropertyNames()
	{
		return params.getPropertyNames();
	}

	public void release()
	{
		params.removeTestPropertyListener( testPropertyListener );
	}

	public int getRowCount()
	{
		return paramNameIndex.size();
	}

	public int getColumnCount()
	{
		return 2;
	}

	public String getColumnName( int columnIndex )
	{
		switch( columnIndex )
		{
			case 0:
				return "Name";
			case 1:
				return "Value";
		}

		return null;
	}

	public boolean isCellEditable( int rowIndex, int columnIndex )
	{
		if( columnIndex == 0 )
		{
			return params instanceof MutableTestPropertyHolder;
		}

		return !getPropertyAtRow( rowIndex ).isReadOnly();
	}

	@Override
	public Class<?> getColumnClass( int columnIndex )
	{
		return String.class;
	}

	@Override
	public void setValueAt( Object aValue, int rowIndex, int columnIndex )
	{
		TestProperty property = getPropertyAtRow( rowIndex );
		switch( columnIndex )
		{
			case 0:
			{
				if( params instanceof MutableTestPropertyHolder )
				{
					if( propertyExists( aValue, property ) )
					{
						return;
					}
					( ( MutableTestPropertyHolder )params ).renameProperty( property.getName(), aValue.toString() );

				}
				break;
			}
			case 1:
			{
				property.setValue( aValue.toString() );
				break;
			}
		}
	}

	protected boolean propertyExists( Object aValue, TestProperty property )
	{
		TestProperty prop = params.getProperty( aValue.toString() );

		if( prop != null && prop != property )
		{
			UISupport.showErrorMessage( "Property name exists!" );
			return true;
		}

		return false;
	}


	public TestProperty getPropertyAtRow( int rowIndex )
	{
		return params.getProperty( paramNameIndex.get( rowIndex ) );
	}

	public Object getValueAt( int rowIndex, int columnIndex )
	{
		TestProperty property = getPropertyAtRow( rowIndex );
		if( property == null )
			return null;

		switch( columnIndex )
		{
			case 0:
				return property.getName();
			case 1:
				if( property instanceof EvaluatedOnReadTestProperty )
				{
					return ( ( EvaluatedOnReadTestProperty )property ).getCurrentValue();
				}
				return property.getValue();
		}

		return null;
	}

	@Override
	public void propertyValueChanged( Property property )
	{
		fireTableDataChanged();
	}

	public void propertyMoved()
	{
		fireTableDataChanged();
	}

	@Override
	public void propertyChange( PropertyChangeEvent evt )
	{
		fireTableDataChanged();
	}

	@Override
	public void moveProperty( String name, int oldIndex, int newIndex )
	{
		( ( MutableTestPropertyHolder )params ).moveProperty( name, newIndex );
		String valueAtNewindex = paramNameIndex.get( newIndex );
		paramNameIndex.set( newIndex, name );
		paramNameIndex.set( oldIndex, valueAtNewindex );
		testPropertyListener.propertyMoved( name, oldIndex, newIndex );
	}

	public void sort()
	{
		Collections.sort( paramNameIndex );
		TestPropertyUtils.sortProperties( ( ( MutableTestPropertyHolder )params ) );
		fireTableDataChanged();
	}

	protected final class InternalTestPropertyListener implements TestPropertyListener
	{


		public void propertyAdded( String name )
		{
			if( !paramNameIndex.contains( name ) )
			{
				paramNameIndex.add( name );
			}
			fireTableDataChanged();
		}

		public void propertyRemoved( String name )
		{
			if( !isLastChangeParameterLevelChange )
			{
				paramNameIndex.remove( name );
			}
			isLastChangeParameterLevelChange = false;
			fireTableDataChanged();
		}

		public void propertyRenamed( String oldName, String newName )
		{
			int paramIndex = paramNameIndex.indexOf( oldName );
			if( paramIndex < 0 )
			{
				return;
			}
			paramNameIndex.set( paramIndex, newName );
			fireTableDataChanged();
		}

		public void propertyValueChanged( String name, String oldValue, String newValue )
		{
			fireTableDataChanged();
		}

		public void propertyMoved( String name, int oldIndex, int newIndex )
		{
			fireTableDataChanged();
		}
	}

}
