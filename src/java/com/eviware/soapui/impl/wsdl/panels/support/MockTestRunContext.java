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

package com.eviware.soapui.impl.wsdl.panels.support;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractSubmitContext;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;

/**
 * Dummy TestRunContext used when executing TestSteps one by one
 * 
 * @author ole.matzura
 */

public class MockTestRunContext extends AbstractSubmitContext<ModelItem> implements TestCaseRunContext
{
	private final MockTestRunner mockTestRunner;
	private final WsdlTestStep testStep;

	public MockTestRunContext( MockTestRunner mockTestRunner, WsdlTestStep testStep )
	{
		super( testStep == null ? mockTestRunner.getTestCase() : testStep );
		this.mockTestRunner = mockTestRunner;
		this.testStep = testStep;
		setProperty( "log", mockTestRunner.getLog() );
		mockTestRunner.setMockRunContext( this );
	}

	public TestStep getCurrentStep()
	{
		return testStep;
	}

	@Override
	public void setProperty( String name, Object value )
	{
		super.setProperty( name, value, getTestCase() );
	}

	public int getCurrentStepIndex()
	{
		return testStep == null ? -1 : testStep.getTestCase().getIndexOfTestStep( testStep );
	}

	public TestCaseRunner getTestRunner()
	{
		return mockTestRunner;
	}

	@Override
	public Object get( Object key )
	{
		if( "currentStep".equals( key ) )
			return getCurrentStep();

		if( "currentStepIndex".equals( key ) )
			return getCurrentStepIndex();

		if( "settings".equals( key ) )
			return getSettings();

		if( "testCase".equals( key ) )
			return getTestCase();

		if( "testRunner".equals( key ) )
			return getTestRunner();

		Object result = getProperty( key.toString() );

		if( result == null )
		{
			result = super.get( key );
		}

		return result;
	}

	@Override
	public Object put( String key, Object value )
	{
		Object oldValue = get( key );
		setProperty( key, value );
		return oldValue;
	}

	public Object getProperty( String name )
	{
		return getProperty( name, testStep, testStep == null ? null : ( WsdlTestCase )testStep.getTestCase() );
	}

	public Object getProperty( String testStepName, String propertyName )
	{
		TestStep ts = testStep == null ? null : testStep.getTestCase().getTestStepByName( testStepName );
		return ts == null ? null : ts.getPropertyValue( propertyName );
	}

	public TestCase getTestCase()
	{
		return testStep == null ? null : testStep.getTestCase();
	}

	public Settings getSettings()
	{
		return testStep == null ? null : testStep.getSettings();
	}
}