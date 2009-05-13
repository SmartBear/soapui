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

import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.TestSuiteListenerAdapter;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestSuiteListener;
import com.eviware.soapui.support.UISupport;

/**
 * ModelItem for LoadTests node
 * 
 * @author ole.matzura
 */

public class WsdlLoadTestsModelItem extends EmptyModelItem
{
	private TestCase testCase;
	private TestSuiteListener listener = new InternalTestSuiteListener();

	public WsdlLoadTestsModelItem( TestCase testCase )
	{
		super( createLabel( testCase ), UISupport.createImageIcon( "/loadtests.gif" ) );
		this.testCase = testCase;

		testCase.getTestSuite().addTestSuiteListener( listener );
	}

	private static String createLabel( TestCase testCase )
	{
		return "Load Tests (" + testCase.getLoadTestCount() + ")";
	}

	public Settings getSettings()
	{
		return testCase.getSettings();
	}

	@Override
	public void release()
	{
		super.release();
		testCase.getTestSuite().removeTestSuiteListener( listener );
	}

	@Override
	public String getName()
	{
		return createLabel( testCase );
	}

	public void updateLabel()
	{
		setName( createLabel( testCase ) );
	}

	public class InternalTestSuiteListener extends TestSuiteListenerAdapter implements TestSuiteListener
	{
		@Override
		public void loadTestAdded( LoadTest loadTest )
		{
			if( loadTest.getTestCase() == testCase )
				updateLabel();
		}

		@Override
		public void loadTestRemoved( LoadTest loadTest )
		{
			if( loadTest.getTestCase() == testCase )
				updateLabel();
		}

		@Override
		public void testCaseRemoved( TestCase testCase )
		{
			if( testCase == WsdlLoadTestsModelItem.this.testCase )
			{
				testCase.getTestSuite().removeTestSuiteListener( listener );
			}
		}
	}
}
