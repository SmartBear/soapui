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

package com.eviware.soapui.testondemand;

import com.eviware.soapui.SoapUISystemProperties;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.google.common.base.Strings;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Erik R. Yverling
 * 
 *         Integration test to test the communication between SoapUI and the
 *         AlertSite Rest API.
 */

public class TOnDemandCallerIT
{
	private static final String FIRST_LOCATION_NAME = "Fort Lauderdale, FL";
	private static final String FIRST_LOCATION_CODE = "10";
	private static final String[] FIRST_SERVER_IP_ADDRESSES = { "10.0.48.17", "127.0.0.1" };

	private static final String SECOND_LOCATION_NAME = "Washington, D.C.";
	private static final String SECOND_LOCATION_CODE = "40";

	private TestOnDemandCaller caller;
	private WsdlTestCase testCase;
	private static final String NOT_THE_RIGHT_HOST = "You need to specify the host name of the test server";

	public final static Logger log = Logger.getLogger( TOnDemandCallerIT.class );

	@Before
	public void setUp() throws Exception
	{
		WsdlProject project = new WsdlProject(TOnDemandCallerIT.class.getResource(
				"/sample-soapui-project.xml").getPath() );
		WsdlTestSuite testSuite = project.getTestSuiteByName( "Test Suite" );
		testCase = testSuite.getTestCaseByName( "Test Conversions" );
		caller = new TestOnDemandCaller();
	}

	@Test
	public void testGetLocations() throws Exception
	{
		if( System.getProperty( SoapUISystemProperties.TEST_ON_DEMAND_HOST ) == null )
		{
			log.warn(NOT_THE_RIGHT_HOST);
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

	@Test
	public void testSendProject() throws Exception
	{
		if( System.getProperty( SoapUISystemProperties.TEST_ON_DEMAND_HOST ) == null )
		{
			log.warn(NOT_THE_RIGHT_HOST);
			return;
		}

		String redirectUrl = caller.sendTestCase( testCase, new Location( FIRST_LOCATION_CODE, FIRST_LOCATION_CODE,
				FIRST_SERVER_IP_ADDRESSES ) );
		assert !Strings.isNullOrEmpty( redirectUrl );
	}
}