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

import static org.junit.Assert.assertTrue;
import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.security.scan.CrossSiteScriptingScan;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * @author dragica.soldo
 * 
 */


// TODO Remove pointless tests
// Move the integration test to the it folder.
public class CrossSiteScriptingTest extends AbstractSecurityTestCaseWithMockService
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( CrossSiteScriptingTest.class );
	}

	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		testStepName = "HTTP Test Request";
		securityCheckType = CrossSiteScriptingScan.TYPE;
		securityCheckName = CrossSiteScriptingScan.NAME;
	}

	@Override
	protected void addSecurityScanConfig( SecurityScanConfig securityScanConfig )
	{
		SoapUI.getSoapUICore().getSecurityScanRegistry().getFactory( securityCheckType )
				.buildSecurityScan( testStep, securityScanConfig, null );
	}

	@Test
	// TODO add proper test 
	public void testParameterShouldBeExposed()
	{
		//		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( createSecurityTest(), new StringToObjectMap() );
		//
		//		testRunner.start( false );
		// String message =
		// testRunner.getSecurityTest().getSecurityTestLog().getElementAt( 0
		// ).getMessage();
		// assertTrue( message, message.contains( "is exposed in the response" )
		// );
		assertTrue( true );
	}

	@Test
	// TODO add proper test
	public void testLogTestEnded()
	{
		//		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( createSecurityTest(), new StringToObjectMap() );
		//
		//		testRunner.start( false );
		//		try
		//		{
		// String message =
		// testRunner.getSecurityTest().getSecurityTestLog().getElementAt( 1
		// ).getMessage();
		// assertTrue(
		// "Security Scan Failed because there is more than one expected warning in the log!",
		// message
		// .startsWith( "SecurityTest ended" ) );
		//		}
		//		catch( IndexOutOfBoundsException ioobe )
		//		{
		//			SoapUI.log( "ignoring exception: " + ioobe.getMessage() );
		//		}
		assertTrue( true );
	}

	@Test
	public void testFinished()
	{
		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( createSecurityTest(), new StringToObjectMap() );

		testRunner.start( false );

		assertTrue( "Test Step failed so as SecurityScan",
				!testRunner.getStatus().equals( SecurityTestRunner.Status.FINISHED.toString() ) );
	}

}
