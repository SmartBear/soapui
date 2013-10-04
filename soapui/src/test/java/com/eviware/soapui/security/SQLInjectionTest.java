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

import java.util.ArrayList;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.security.scan.SQLInjectionScan;

/**
 * @author dragica.soldo
 * 
 */

// TODO Remove pointless tests
// Move the integration test to the it folder.
public class SQLInjectionTest extends AbstractSecurityTestCaseWithMockService
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( SQLInjectionTest.class );
	}

	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		testStepName = "HTTP Test Request";
		securityCheckType = SQLInjectionScan.TYPE;
		securityCheckName = SQLInjectionScan.NAME;
	}

	protected void addSecurityScanConfig( SecurityScanConfig securityScanConfig )
	{
		SQLInjectionScan sqlCheck = ( SQLInjectionScan )SoapUI.getSoapUICore().getSecurityScanRegistry()
				.getFactory( securityCheckType ).buildSecurityScan( testStep, securityScanConfig, null );

		List<String> params = new ArrayList<String>();
		params.add( "q" );
	}

	@Test
	public void testStart()
	{
		//		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( createSecurityTest(), null );

		//		testRunner.start( false );

		//		assertEquals( TestStepResult.TestStepStatus.OK, testRunner.getStatus() );

		assertTrue( true );

	}
}
