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

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.security.SecurityCheck;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.security.result.SecurityTestStepResult;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * Context information for a securitytest run session
 * 
 * @author dragica.soldo
 */

public class SecurityTestRunContext extends WsdlTestRunContext
{

	// holds currentCheck index on TestStep level
	private int currentCheckIndex;

	// holds currentCheck index out of summary number of checks on a SecurityTest
	// level
	private int currentCheckOnSecurityTestIndex;
	private SecurityTest securityTest;

	public SecurityTestRunContext( TestCaseRunner testRunner, StringToObjectMap properties )
	{
		super( testRunner, properties );
		if( testRunner instanceof SecurityTestRunnerImpl )
		{
			securityTest = ( ( SecurityTestRunnerImpl )testRunner ).getSecurityTest();
		}
		// this.testRunner = testRunner;
	}

	public int getCurrentCheckOnSecurityTestIndex()
	{
		return currentCheckOnSecurityTestIndex;
	}

	public void setCurrentCheckOnSecurityTestIndex( int currentCheckOnSecurityTestIndex )
	{
		this.currentCheckOnSecurityTestIndex = currentCheckOnSecurityTestIndex;
	}

	/**
	 * Holds result of SecurityChecks on a TestStep level
	 */
	private SecurityTestStepResult currentSecurityStepResult;

	public int getCurrentCheckIndex()
	{
		return currentCheckIndex;
	}

	public void setCurrentCheckIndex( int currentCheckIndex )
	{
		this.currentCheckIndex = currentCheckIndex;
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

	public void setCurrentSecurityStepResult( SecurityTestStepResult result )
	{
		currentSecurityStepResult = result;
	}

	public SecurityTestStepResult getCurrentSecurityStepResult()
	{
		return currentSecurityStepResult;
	}

	public SecurityCheck getCurrentCheck()
	{
		int testStepCheckCount = 0;
		if( securityTest != null )
		{
			testStepCheckCount = securityTest.getSecurityCheckCount();
		}
		if( currentCheckIndex < 0 || currentCheckIndex >= testStepCheckCount )
			return null;

		if( securityTest != null )
		{
			return securityTest.getTestStepSecurityCheckAt( getCurrentStep().getId(), getCurrentCheckIndex() );
		}
		return null;
	}

}
