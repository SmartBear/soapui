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

package com.eviware.soapui.security;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.settings.Settings;
import com.eviware.soapui.model.support.AbstractSubmitContext;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * Context information for a securitytest run session
 */

public class SecurityTestRunContext extends AbstractSubmitContext<TestModelItem> implements TestCaseRunContext
{

	private int currentCheckIndex;

	public SecurityTestRunContext( SecurityTestRunnerImpl testRunner, StringToObjectMap properties )
	{
		super( testRunner.getSecurityTest(), properties );
		this.testRunner = testRunner;
	}

	private final TestCaseRunner testRunner;
	private int currentStepIndex;
	private TestCase testCase;

//	public SecurityTestRunContext( TestStep testStep )
//	{
//		super( testStep );
//
//		testRunner = null;
//		testCase = testStep.getTestCase();
//		currentStepIndex = testCase.getIndexOfTestStep( testStep );
//	}

	public int getCurrentCheckIndex()
	{
		return currentCheckIndex;
	}

	public void setCurrentCheckIndex( int currentCheckIndex )
	{
		this.currentCheckIndex = currentCheckIndex;
	}

	public TestStep getCurrentStep()
	{
		if( currentStepIndex < 0 || currentStepIndex >= getTestCase().getTestStepCount() )
			return null;

		return getTestCase().getTestStepAt( currentStepIndex );
	}

	@Override
	public void setProperty( String name, Object value )
	{
		super.setProperty( name, value, getTestCase() );
	}

	public int getCurrentStepIndex()
	{
		return currentStepIndex;
	}

	public void setCurrentStep( int index )
	{
		currentStepIndex = index;
	}

	public TestCaseRunner getTestRunner()
	{
		return testRunner;
	}

	public Object getProperty( String testStepName, String propertyName )
	{
		TestStep testStep = getTestCase().getTestStepByName( testStepName );
		return testStep == null ? null : testStep.getPropertyValue( propertyName );
	}

	public TestCase getTestCase()
	{
		return testRunner == null ? testCase : testRunner.getTestCase();
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
		WsdlTestCase testCase = ( WsdlTestCase )getTestCase();
		TestStep testStep = currentStepIndex >= 0 && currentStepIndex < testCase.getTestStepCount() ? testCase
				.getTestStepAt( currentStepIndex ) : null;

		return getProperty( name, testStep, testCase );
	}

	public void reset()
	{
		resetProperties();
		currentStepIndex = 0;
	}

	public String expand( String content )
	{
		return PropertyExpander.expandProperties( this, content );
	}

	public Settings getSettings()
	{
		return testCase == null ? SoapUI.getSettings() : testCase.getSettings();
	}

}
