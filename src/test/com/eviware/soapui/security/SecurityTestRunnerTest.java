/**
 * 
 */
package com.eviware.soapui.security;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.SecurityTestConfig;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.testsuite.TestRunner.Status;
import com.eviware.soapui.security.check.GroovySecurityCheck;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.support.TestCaseWithJetty;

/**
 * @author dragica.soldo
 * 
 */
public class SecurityTestRunnerTest extends TestCaseWithJetty
{

	WsdlTestCase testCase;
	SecurityTestConfig config = SecurityTestConfig.Factory.newInstance();
	HashMap<String, List<SecurityCheck>> securityChecksMap = new HashMap<String, List<SecurityCheck>>();
	WsdlMockService mockService;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		WsdlProject project = new WsdlProject( "src" + File.separatorChar + "test-resources" + File.separatorChar
				+ "sample-soapui-project.xml" );
		TestSuite testSuite = project.getTestSuiteByName( "Test Suite" );
		List<SecurityCheck> secCheckList = new ArrayList();
		GroovySecurityCheck gsc = new GroovySecurityCheck( SecurityCheckConfig.Factory.newInstance(), null, null );
		gsc.setScript( "log.info testStep" );
		secCheckList.add( gsc );
		securityChecksMap.put( "SEK to USD Test", secCheckList );
		testCase = ( WsdlTestCase )testSuite.getTestCaseByName( "Test Conversions" );

		WsdlInterface iface = ( WsdlInterface )project.getInterfaceAt( 0 );

		mockService = ( WsdlMockService )project.addNewMockService( "MockService 1" );

		mockService.setPort( 9081 );
		mockService.setPath( "/testmock" );

		WsdlOperation operation = ( WsdlOperation )iface.getOperationAt( 0 );
		WsdlMockOperation mockOperation = ( WsdlMockOperation )mockService.addNewMockOperation( operation );
		WsdlMockResponse mockResponse = mockOperation.addNewMockResponse( "Test Response", true );
		mockResponse.setResponseContent( "Tjohoo!" );

		mockService.start();

		String endpoint = "http://localhost:9081//testmock";
		iface.addEndpoint( endpoint );
		List<TestStep> testStepList = testCase.getTestStepList();
		for( TestStep testStep : testStepList )
		{
			if( testStep instanceof WsdlTestRequestStep )
			{
				( ( WsdlTestRequestStep )testStep ).getTestRequest().setEndpoint( endpoint );
				// System.out.print( "endpoint:" +
				// ((WsdlTestRequestStep)testStep).getTestRequest().getEndpoint() );
			}
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
		if( mockService.getMockRunner().isRunning() )
		{
			mockService.getMockRunner().stop();
		}
	}

	@Test
	public void testStart()
	{
		// WsdlTestRequestStep testStep = ( WsdlTestRequestStep )
		// testCase.getTestStepByName("SEK to USD Test");

		SecurityTest securityTest = new SecurityTest( testCase, config );
		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( securityTest );
		// SecurityTestContext testRunContext = new SecurityTestContext(
		// testRunner );

		testRunner.start();

		assertEquals( TestStepResult.TestStepStatus.OK, testRunner.getStatus() );
		// assertEquals( Status.RUNNING, testRunner.getStatus() );

	}

}
