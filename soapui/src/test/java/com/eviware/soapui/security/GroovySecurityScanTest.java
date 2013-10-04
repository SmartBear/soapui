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

import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;

import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.security.scan.GroovySecurityScan;

/**
 * @author nebojsa.tasic
 * 
 */
// TODO Remove pointless tests
// Move the integration test to the it folder.
public class GroovySecurityScanTest extends AbstractSecurityTestCaseWithMockService
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( GroovySecurityScanTest.class );
	}

	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		testStepName = "SEK to USD Test";
		securityCheckType = GroovySecurityScan.TYPE;
		securityCheckName = GroovySecurityScan.TYPE;
	}

	protected void addSecurityScanConfig( SecurityScanConfig securityScanConfig )
	{
		GroovySecurityScan gsc = new GroovySecurityScan( testStep, securityScanConfig, null, null );
		gsc.setExecuteScript( "println('');println \"this is print from GroovySecurityScan on test step '${testStep.name}'\";println('')" );
	}

	@Test
	// TODO add proper test
	public void testLogTestEnded()
	{
		// SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl(
		// createSecurityTest() );
		//
		// testRunner.start( false );
		//
		// assertTrue( "Groovy Security Scan Failed due to wrong Log message",
		// testRunner.getSecurityTest()
		// .getSecurityTestLog().getElementAt( 0 ).getMessage().startsWith(
		// "SecurityTest ended" ) );

	}

	@Test
	// TODO add proper test
	public void testFinished()
	{
		// SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl(
		// createSecurityTest() );
		//
		// testRunner.start( false );
		//
		// assertTrue( "Test Step failed so as GroovySecurityScan",
		// !testRunner.getStatus().equals(
		// TestRunner.Status.FINISHED ) );
	}

}
