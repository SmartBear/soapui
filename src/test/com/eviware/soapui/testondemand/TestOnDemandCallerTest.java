/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.testondemand;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.eviware.soapui.SoapUISystemProperties;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.google.common.base.Strings;

/**
 * @author Erik R. Yverling
 * 
 *         Integration test to test the communication between soapUI and the
 *         AlertSite Rest API.
 */

// FIXME We need to add the soapui.testondemand.host system property to Hudson to be able to run this
public class TestOnDemandCallerTest
{
	private static final String FIRST_LOCATION_NAME = "Fort Lauderdale, FL";
	private static final String FIRST_LOCATION_CODE = "10|ash.regression.alertsite.com";

	private static final String SECOND_LOCATION_NAME = "Washington, D.C.";
	private static final String SECOND_LOCATION_CODE = "40|latte.regression.alertsite.com";

	private TestOnDemandCaller caller;
	private WsdlTestCase testCase;

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( TestOnDemandCallerTest.class );
	}

	@Before
	public void setUp() throws Exception
	{
		WsdlProject project = new WsdlProject( "src" + File.separatorChar + "test-resources" + File.separatorChar
				+ "sample-soapui-project.xml" );
		WsdlTestSuite testSuite = project.getTestSuiteByName( "Test Suite" );
		testCase = testSuite.getTestCaseByName( "Test Conversions" );
		caller = new TestOnDemandCaller();
	}

	@Ignore
	@Test
	public void testGetLocations() throws Exception
	{
		if( System.getProperty( SoapUISystemProperties.TEST_ON_DEMAND_HOST ) == null )
		{
			return;
		}

		List<Location> locations = caller.getLocations();

		Location firstLocation = locations.get( 0 );
		assertEquals( firstLocation.getName(), FIRST_LOCATION_NAME );
		assertEquals( firstLocation.getCode(), FIRST_LOCATION_CODE );

		Location secondLocation = locations.get( 1 );
		assertEquals( secondLocation.getName(), SECOND_LOCATION_NAME );
		assertEquals( secondLocation.getCode(), SECOND_LOCATION_CODE );
	}

	@Ignore
	@Test
	public void testSendProject() throws Exception
	{
		if( System.getProperty( SoapUISystemProperties.TEST_ON_DEMAND_HOST ) == null )
		{
			return;
		}

		String redirectUrl = caller.sendTestCase( testCase, new Location( FIRST_LOCATION_CODE, SECOND_LOCATION_NAME ) );
		assert !Strings.isNullOrEmpty( redirectUrl );
	}
}
