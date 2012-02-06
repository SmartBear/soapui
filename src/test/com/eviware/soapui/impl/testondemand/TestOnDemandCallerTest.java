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

package com.eviware.soapui.impl.testondemand;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;

import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;

/**
 * @author Erik R. Yverling
 * 
 *         Integration test to test the communication between soapUI and the
 *         AlertSite Rest API.
 */
public class TestOnDemandCallerTest
{
	private static final String FIRST_LOCATION_NAME = "Fort%20Lauderdale,%20FL";
	private static final String FIRST_LOCATION_CODE = "10|ash.regression.alertsite.com";

	private static final String SECOND_LOCATION_NAME = "Washington,%20D.C.";
	private static final String SECOND_LOCATION_CODE = "40|latte.regression.alertsite.com";

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( TestOnDemandCallerTest.class );
	}

	TestOnDemandCaller caller = new TestOnDemandCaller();
	private WsdlTestCase testCase;

	@Before
	public void setUp() throws Exception
	{
		WsdlProject project = new WsdlProject( "src" + File.separatorChar + "test-resources" + File.separatorChar
				+ "sample-soapui-project.xml" );
		WsdlTestSuite testSuite = project.getTestSuiteByName( "Test Suite" );
		testCase = testSuite.getTestCaseByName( "Test Conversions" );
	}

	@Ignore
	@Test
	public void testGetLocations() throws Exception
	{
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
	}
}
