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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestStep;

public class TestStepComboBoxModel extends AbstractListModel implements ComboBoxModel
{
	private final WsdlTestCase testCase;
	private WsdlTestStep selectedStep;
	private int selectedStepIndex = -1;
	private TestStepNameListener testStepNameListener = new TestStepNameListener();
	private InternalTestSuiteListener testSuiteListener;

	public TestStepComboBoxModel( WsdlTestCase testCase )
	{
		this.testCase = testCase;

		testSuiteListener = new InternalTestSuiteListener();
		testCase.getTestSuite().addTestSuiteListener( testSuiteListener );
	}

	public void release()
	{
		testCase.getTestSuite().removeTestSuiteListener( testSuiteListener );
	}

	public Object getElementAt( int index )
	{
		return testCase.getTestStepAt( index ).getName();
	}

	public int getSize()
	{
		return testCase.getTestStepCount();
	}

	private final class InternalTestSuiteListener extends TestSuiteListenerAdapter
	{
		@Override
		public void testStepAdded( TestStep testStep, int index )
		{
			if( testStep.getTestCase() == testCase )
				fireIntervalAdded( TestStepComboBoxModel.this, index, index );
		}

		@Override
		public void testStepMoved( TestStep testStep, int fromIndex, int offset )
		{
			if( testStep.getTestCase() == testCase )
				fireContentsChanged( TestStepComboBoxModel.this, fromIndex, fromIndex + offset );
		}

		@Override
		public void testStepRemoved( TestStep testStep, int index )
		{
			if( testStep.getTestCase() == testCase )
				fireIntervalRemoved( TestStepComboBoxModel.this, index, index );

			if( index == selectedStepIndex )
				setSelectedItem( null );
		}
	}

	public Object getSelectedItem()
	{
		return selectedStep == null ? null : selectedStep.getName();
	}

	public void setSelectedItem( Object anItem )
	{
		if( selectedStep != null )
			selectedStep.removePropertyChangeListener( testStepNameListener );

		selectedStep = testCase.getTestStepByName( ( String )anItem );
		if( selectedStep != null )
		{
			selectedStep.addPropertyChangeListener( WsdlTestStep.NAME_PROPERTY, testStepNameListener );
			selectedStepIndex = testCase.getIndexOfTestStep( selectedStep );
		}
		else
			selectedStepIndex = -1;

		fireContentsChanged( this, -1, -1 );
	}

	/**
	 * Listen for testStep name changes and modify comboBox model accordingly
	 */

	private final class TestStepNameListener implements PropertyChangeListener
	{
		public void propertyChange( PropertyChangeEvent evt )
		{
			Object oldItem = evt.getOldValue();
			int stepIndex = testCase.getTestStepIndexByName( ( String )oldItem );
			if( stepIndex != -1 )
			{
				fireContentsChanged( TestStepComboBoxModel.this, stepIndex, stepIndex );

				if( selectedStep != null && testCase.getIndexOfTestStep( selectedStep ) == stepIndex )
					fireContentsChanged( this, -1, -1 );
			}
		}
	}

	public WsdlTestStep getSelectedStep()
	{
		return selectedStep;
	}
}
