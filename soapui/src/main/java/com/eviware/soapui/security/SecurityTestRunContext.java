/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security;

import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.security.result.SecurityTestStepResult;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * Context information for a securitytest run session
 * 
 * @author dragica.soldo
 */

public class SecurityTestRunContext extends WsdlTestRunContext
{

	// holds currentScan index on TestStep level
	private int currentScanIndex;

	private TestStepResult originalTestStepResult;

	// holds currentScan index out of summary number of scans on a SecurityTest
	// level
	private int currentScanOnSecurityTestIndex;
	private SecurityTest securityTest;

	public SecurityTestRunContext( TestCaseRunner testRunner, StringToObjectMap properties )
	{
		super( testRunner, properties, ( ( SecurityTestRunnerImpl )testRunner ).getSecurityTest() );
		if( testRunner instanceof SecurityTestRunnerImpl )
		{
			securityTest = ( ( SecurityTestRunnerImpl )testRunner ).getSecurityTest();
		}
		// this.testRunner = testRunner;
	}

	public int getCurrentScanOnSecurityTestIndex()
	{
		return currentScanOnSecurityTestIndex;
	}

	public void setCurrentScanOnSecurityTestIndex( int currentScanOnSecurityTestIndex )
	{
		this.currentScanOnSecurityTestIndex = currentScanOnSecurityTestIndex;
	}

	/**
	 * Holds result of SecurityScans on a TestStep level
	 */
	private SecurityTestStepResult currentSecurityStepResult;

	public int getCurrentScanIndex()
	{
		return currentScanIndex;
	}

	public void setCurrentScanIndex( int currentScanIndex )
	{
		this.currentScanIndex = currentScanIndex;
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

	protected TestStepResult getOriginalTestStepResult()
	{
		return originalTestStepResult;
	}

	protected void setOriginalTestStepResult( TestStepResult originalTestStepResult )
	{
		this.originalTestStepResult = originalTestStepResult;
	}

	public SecurityScan getCurrentScan()
	{
		int testStepScanCount = 0;
		if( securityTest != null )
		{
			testStepScanCount = securityTest.getSecurityScanCount();
		}
		if( currentScanIndex < 0 || currentScanIndex >= testStepScanCount )
			return null;

		if( securityTest != null )
		{
			return securityTest.getTestStepSecurityScanAt( getCurrentStep().getId(), getCurrentScanIndex() );
		}
		return null;
	}

}
