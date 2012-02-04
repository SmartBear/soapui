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

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestSuite;

/**
 * @author Erik R. Yverling
 * 
 *         Integration test to test the communication between soapUI and the
 *         AlertSite Rest API.
 */
public class TestOnDemandCallerTest
{
	private static final String ALERT_SITE_REDIRECT_URL = "http://www.alertsite.com";
	private static final String LOCATION_CODE = "10|ash.regression.alertsite.com";

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

	@Test
	public void testSendProject() throws Exception
	{
		assertEquals( caller.sendProject( testCase, LOCATION_CODE ), ALERT_SITE_REDIRECT_URL );
	}
}
