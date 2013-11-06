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

import com.eviware.soapui.config.SecurityScanConfig;
import com.eviware.soapui.config.SecurityTestConfig;
import com.eviware.soapui.config.TestStepSecurityTestConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import junit.framework.JUnit4TestAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;

public class AbstractSecurityTestCaseWithMockService
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( AbstractSecurityTestCaseWithMockService.class );
	}

	WsdlTestCase testCase;
	WsdlTestStep testStep;
	SecurityTestConfig config = SecurityTestConfig.Factory.newInstance();
	WsdlMockService mockService;

	String testStepName;
	String securityCheckType;
	String securityCheckName;

	/**
	 * always override this method to call
	 * 
	 * super.setUp();
	 * 
	 * and initialise there three variables testStepName = "SEK to USD Test";
	 * securityCheckType = GroovySecurityCheck.TYPE; securityCheckName =
	 * GroovySecurityCheck.TYPE;
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		WsdlProject project = new WsdlProject(AbstractSecurityTestCaseWithMockService.class.getResource(
				"/sample-soapui-project.xml").getPath() );
		TestSuite testSuite = project.getTestSuiteByName( "Test Suite" );
		testCase = ( WsdlTestCase )testSuite.getTestCaseByName( "Test Conversions" );

		WsdlInterface iface = ( WsdlInterface )project.getInterfaceAt( 0 );

		mockService = project.addNewMockService( "MockService 1" );

		mockService.setPort( 9081 );
		mockService.setPath( "/testmock" );

		WsdlOperation operation = iface.getOperationAt( 0 );
		WsdlMockOperation mockOperation = mockService.addNewMockOperation( operation );
		WsdlMockResponse mockResponse = mockOperation.addNewMockResponse( "Test Response", true );
		mockResponse.setResponseContent( "Tjohoo!" );

		mockService.start();

		String endpoint = "http://" + mockService.getHost() + ":" + mockService.getPort() + mockService.getPath();
		iface.addEndpoint( endpoint );
		List<TestStep> testStepList = testCase.getTestStepList();
		for( TestStep testStep : testStepList )
		{
			if( testStep instanceof WsdlTestRequestStep )
			{
				( ( WsdlTestRequestStep )testStep ).getTestRequest().setEndpoint( endpoint );
			}
			if( testStep instanceof HttpTestRequestStep )
			{
				( ( HttpTestRequestStep )testStep ).getTestRequest().setEndpoint( endpoint );
			}
		}
	}

	@After
	public void tearDown() throws Exception
	{
		if( mockService.getMockRunner().isRunning() )
		{
			mockService.getMockRunner().stop();
		}
	}

	/*
	 * creates SecurityTest
	 */
	protected SecurityTest createSecurityTest()
	{
		SecurityTest securityTest = new SecurityTest( testCase, config );
		SecurityScanConfig securityCheckConfig = addCheckToConfig();
		addSecurityScanConfig( securityCheckConfig );
		return securityTest;
	}

	/*
	 * adds specific config which is ANY TYPE in soapui.xsd implement it by
	 * create specific SecurityTest with constructor
	 */
	protected void addSecurityScanConfig( SecurityScanConfig securityCheckConfig )
	{

	}

	/*
	 * creates basic SecurityScanConfig
	 */
	protected SecurityScanConfig addCheckToConfig()
	{
		testStep = testCase.getTestStepByName( testStepName );

		TestStepSecurityTestConfig testStepSecurityTest = config.addNewTestStepSecurityTest();
		testStepSecurityTest.setTestStepId( testStep.getId() );

		SecurityScanConfig securityCheckConfig = testStepSecurityTest.addNewTestStepSecurityScan();
		securityCheckConfig.setType( securityCheckType );
		securityCheckConfig.setName( securityCheckName );

		return securityCheckConfig;
	}

	@Test
	public void testDummy()
	{
		assertTrue( "Dummy SecurityTest", true );
	}

}
