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

package com.eviware.soapui.model.tree.nodes.support;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuiteListener;
import com.eviware.soapui.support.UISupport;

/**
 * ModelItem for TestSteps node
 * 
 * @author ole.matzura
 */

public class WsdlTestStepsModelItem extends EmptyModelItem
{
	private TestCase testCase;
	private TestSuiteListener listener = new InternalTestSuiteListener();

	public WsdlTestStepsModelItem( TestCase testCase )
	{
		super( createLabel( testCase ), UISupport.createImageIcon( "/teststeps.gif" ) );
		this.testCase = testCase;

		testCase.getTestSuite().addTestSuiteListener( listener );
	}

	private static String createLabel( TestCase testCase )
	{
		return "Test Steps (" + testCase.getTestStepCount() + ")";
	}

	public Settings getSettings()
	{
		return testCase.getSettings();
	}

	@Override
	public String getName()
	{
		return createLabel( testCase );
	}

	public WsdlTestCase getTestCase()
	{
		return ( WsdlTestCase )testCase;
	}

	@Override
	public void release()
	{
		super.release();
		testCase.getTestSuite().removeTestSuiteListener( listener );
	}

	public void updateLabel()
	{
		setName( createLabel( testCase ) );
	}

	public class InternalTestSuiteListener extends TestSuiteListenerAdapter implements TestSuiteListener
	{
		@Override
		public void testStepAdded( TestStep testStep, int index )
		{
			if( testStep.getTestCase() == testCase )
				updateLabel();
		}

		@Override
		public void testStepRemoved( TestStep testStep, int index )
		{
			if( testStep.getTestCase() == testCase )
				updateLabel();
		}

		@Override
		public void testCaseRemoved( TestCase testCase )
		{
			if( testCase == WsdlTestStepsModelItem.this.testCase )
			{
				testCase.getTestSuite().removeTestSuiteListener( listener );
			}
		}
	}

}
