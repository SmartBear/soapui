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
import com.eviware.soapui.model.support.TestPropertyUtils;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;

import java.beans.PropertyChangeListener;

public class DirectAccessPropertyHolderTableModel<T extends TestPropertyHolder>
		extends DefaultPropertyHolderTableModel<T>
		implements PropertyHolderTableModel, EnvironmentListener, PropertyChangeListener
{
	protected final InternalTestPropertyListener testPropertyListener;

	public DirectAccessPropertyHolderTableModel( T holder )
	{
		this.params = holder;

		testPropertyListener = new InternalTestPropertyListener();
		holder.addTestPropertyListener( testPropertyListener );
	}

	public int getRowCount()
	{
		return params.getPropertyCount();
	}


	public TestProperty getPropertyAtRow( int rowIndex )
	{
		return params.getPropertyAt( rowIndex );
	}

	@Override
	public void moveProperty( String name, int oldIndex, int newIndex )
	{
		( ( MutableTestPropertyHolder )params ).moveProperty( name, newIndex );
		testPropertyListener.propertyMoved( name, oldIndex, newIndex );
	}

	public void sort()
	{
		TestPropertyUtils.sortProperties( ( ( MutableTestPropertyHolder )params ) );
		fireTableDataChanged();
	}

	protected final class InternalTestPropertyListener implements TestPropertyListener
	{
		public void propertyAdded( String name )
		{
			fireTableDataChanged();
		}

		public void propertyRemoved( String name )
		{
			isLastChangeParameterLevelChange = false;
			fireTableDataChanged();
		}

		public void propertyRenamed( String oldName, String newName )
		{
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
