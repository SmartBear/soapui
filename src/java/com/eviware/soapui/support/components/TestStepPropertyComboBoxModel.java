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

package com.eviware.soapui.support.components;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.support.TestPropertyListenerAdapter;

public class TestStepPropertyComboBoxModel extends AbstractListModel implements ComboBoxModel
{
	private WsdlTestStep testStep;
	private List<String> names;
	private String selectedName;
	private InternalTestPropertyListener testStepListener = new InternalTestPropertyListener();

	public TestStepPropertyComboBoxModel( WsdlTestStep testStep )
	{
		this.testStep = testStep;

		names = new ArrayList<String>();

		if( testStep != null )
		{
			names.addAll( Arrays.asList( testStep.getPropertyNames() ) );
			testStep.addTestPropertyListener( testStepListener );
		}
	}

	public void release()
	{
		if( testStep != null )
			testStep.removeTestPropertyListener( testStepListener );
	}

	public WsdlTestStep getTestStep()
	{
		return testStep;
	}

	public void setTestStep( WsdlTestStep testStep )
	{
		if( this.testStep != null )
		{
			this.testStep.removeTestPropertyListener( testStepListener );
		}

		int sz = names.size();
		if( sz > 0 )
		{
			names.clear();
			fireIntervalRemoved( this, 0, sz - 1 );
		}

		this.testStep = testStep;
		if( testStep != null )
		{
			testStep.addTestPropertyListener( testStepListener );
			names.addAll( Arrays.asList( testStep.getPropertyNames() ) );
			if( !names.isEmpty() )
			{
				fireIntervalAdded( this, 0, names.size() - 1 );
			}
		}

		setSelectedItem( null );
	}

	public Object getElementAt( int index )
	{
		return names.get( index );
	}

	public int getSize()
	{
		return names.size();
	}

	private final class InternalTestPropertyListener extends TestPropertyListenerAdapter
	{
		@Override
		public void propertyAdded( String name )
		{
			names.add( name );
			fireIntervalAdded( TestStepPropertyComboBoxModel.this, names.size() - 1, names.size() - 1 );
		}

		@Override
		public void propertyRemoved( String name )
		{
			int ix = names.indexOf( name );
			if( ix >= 0 )
			{
				names.remove( ix );
				fireIntervalRemoved( TestStepPropertyComboBoxModel.this, ix, ix );

				if( name.equals( selectedName ) )
					setSelectedItem( null );
			}
		}

		@Override
		public void propertyRenamed( String oldName, String newName )
		{
			int ix = names.indexOf( oldName );
			fireContentsChanged( TestStepPropertyComboBoxModel.this, ix, ix );

			if( oldName.equals( selectedName ) )
				setSelectedItem( newName );
		}

		@Override
		public void propertyMoved( String name, int oldIndex, int newIndex )
		{
			fireContentsChanged( TestStepPropertyComboBoxModel.this, 0, getSize() - 1 );
		}

	}

	public Object getSelectedItem()
	{
		return selectedName;
	}

	public void setSelectedItem( Object anItem )
	{
		if( anItem == null && selectedName == null )
			return;

		if( anItem != null && selectedName != null && anItem.equals( selectedName ) )
			return;

		selectedName = anItem == null ? null : anItem.toString();

		fireContentsChanged( this, -1, -1 );
	}
}
