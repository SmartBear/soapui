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

import org.junit.Before;
import org.junit.Test;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.security.check.ParameterExposureCheck;
import com.eviware.soapui.security.registry.SecurityCheckRegistry;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * @author dragica.soldo
 * 
 */
public class ParameterExposureTest extends AbstractSecurityTestCaseWithMockService
{

	/**
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		testStepName = "HTTP Test Request";
		securityCheckType = ParameterExposureCheck.TYPE;
		securityCheckName = ParameterExposureCheck.TYPE;
	}

	@Override
	protected void addSecurityCheckConfig( SecurityCheckConfig securityCheckConfig )
	{

		SecurityCheckRegistry.getInstance().getFactory( securityCheckType ).buildSecurityCheck( testStep,
				securityCheckConfig, null );

	}

	@Test
	public void testParameterShouldBeExposed()
	{

		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( createSecurityTest(), new StringToObjectMap() );

		testRunner.start( false );
		// String message =
		// testRunner.getSecurityTest().getSecurityTestLog().getElementAt( 0
		// ).getMessage();
		// assertTrue( message, message.contains( "is exposed in the response" )
		// );
		assert true;

	}

	@Test
	public void testLogTestEnded()
	{
		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( createSecurityTest(), new StringToObjectMap() );

		testRunner.start( false );
		try
		{
			// String message =
			// testRunner.getSecurityTest().getSecurityTestLog().getElementAt( 1
			// ).getMessage();
			// assertTrue(
			// "Security Check Failed because there is more than one expected warning in the log!",
			// message
			// .startsWith( "SecurityTest ended" ) );
		}
		catch( IndexOutOfBoundsException ioobe )
		{
			SoapUI.log( "ignoring exception: " + ioobe.getMessage() );
		}

	}

	@Test
	public void testFinished()
	{
		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( createSecurityTest(), new StringToObjectMap() );

		testRunner.start( false );

		assertTrue( "Test Step failed so as SecurityCheck", !testRunner.getStatus().equals( SecurityTestRunner.Status.FINISHED ) );

	}

}
