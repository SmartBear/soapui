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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.security.scan.SQLInjectionCheck;

/**
 * @author dragica.soldo
 * 
 */
public class SQLInjectionTest extends AbstractSecurityTestCaseWithMockService
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
		securityCheckType = SQLInjectionCheck.TYPE;
		securityCheckName = SQLInjectionCheck.NAME;
	}

	@Override
	protected void addSecurityCheckConfig( SecurityCheckConfig securityCheckConfig )
	{

		SQLInjectionCheck sqlCheck = ( SQLInjectionCheck )SoapUI.getSoapUICore().getSecurityCheckRegistry()
				.getFactory( securityCheckType ).buildSecurityCheck( testStep, securityCheckConfig, null );

		List<String> params = new ArrayList<String>();
		params.add( "q" );
	}

	@Test
	public void testStart()
	{

		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( createSecurityTest(), null );

		testRunner.start( false );

		// assertEquals( TestStepResult.TestStepStatus.OK,
		testRunner.getStatus();
		// );

		// TODO: finish
		assertEquals( true, true );

	}
}
