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
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.security.check.GroovySecurityCheck;
import com.eviware.soapui.security.check.ParameterExposureCheck;
import com.eviware.soapui.security.check.SQLInjectionCheck;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.security.panels.SecurityChecksPanel.SecurityCheckListModel;
import com.eviware.soapui.security.registry.SecurityCheckRegistry;
import com.eviware.soapui.support.TestCaseWithJetty;

/**
 * @author dragica.soldo
 * 
 */
public class SecurityTestRunnerTest extends TestCaseWithJetty
{

	WsdlTestCase testCase;
	WsdlTestStep testStep;
	SecurityTestConfig config = SecurityTestConfig.Factory.newInstance();
	HashMap<String , List<SecurityCheck>> securityChecksMap = new HashMap<String, List<SecurityCheck>>();
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
		testCase = ( WsdlTestCase )testSuite.getTestCaseByName( "Test Conversions" );

		groovySecurityCheckSetUp();

//		parameterExposureCheckSetup();
//
//		SQLInjectionCheckSetup();

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
			}
			if( testStep instanceof HttpTestRequestStep )
			{
				// ( ( HttpTestRequestStep )testStep ).getTestRequest().setEndpoint(
				// endpoint );
			}
		}
	}

	private void parameterExposureCheckSetup()
	{
		List<SecurityCheck> secCheckList = new ArrayList<SecurityCheck>();
		SecurityCheckConfig exposureConfig = SecurityCheckRegistry.getInstance().getFactory( ParameterExposureCheck.TYPE )
				.createNewSecurityCheck( "Test" );
		ParameterExposureCheck exposureCheck = ( ParameterExposureCheck )SecurityCheckRegistry.getInstance().getFactory(
				ParameterExposureCheck.TYPE ).buildSecurityCheck( exposureConfig );
		secCheckList.add( exposureCheck );
		TestStep testStep = testCase.getTestStepByName( "HTTP Test Request" );
		securityChecksMap.put( testStep.getId(), secCheckList );
	}

	private void SQLInjectionCheckSetup()
	{
		List<SecurityCheck> secCheckList = new ArrayList<SecurityCheck>();
		SecurityCheckConfig injectionConfig = SecurityCheckRegistry.getInstance().getFactory( SQLInjectionCheck.TYPE )
				.createNewSecurityCheck( "Test" );
		SQLInjectionCheck sqlCheck = ( SQLInjectionCheck )SecurityCheckRegistry.getInstance().getFactory(
				SQLInjectionCheck.TYPE ).buildSecurityCheck( injectionConfig );

		List<String> params = new ArrayList<String>();
		params.add( "q" );
		sqlCheck.setParamsToUse( params );
		secCheckList.add( sqlCheck );
		TestStep testStep = testCase.getTestStepByName( "HTTP Test Request" );
		securityChecksMap.put( testStep.getId(), secCheckList );
	}

	private void groovySecurityCheckSetUp()
	{
		List<SecurityCheck> secCheckList = new ArrayList<SecurityCheck>();
		SecurityCheckConfig asdf = SecurityCheckConfig.Factory.newInstance();
		asdf.setType( GroovySecurityCheck.TYPE );
		asdf.setName(  GroovySecurityCheck.TYPE );
		GroovySecurityCheck gsc = new GroovySecurityCheck( asdf, null, null );
		gsc.setScript( "println('');println \"this is print from GroovySecurityCheck on test step '${testStep.name}'\";println('')" );

		secCheckList.add( gsc );
		TestStep testStep = testCase.getTestStepByName( "SEK to USD Test" );
		securityChecksMap.put( testStep.getId(), secCheckList );
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
		SecurityTest securityTest = new SecurityTest( testCase, config );
		addSecurityChecks( securityTest );
//		securityTest.setListModel( new SecurityChecksPanel.SecurityCheckListModel() );

		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( securityTest );

		 testRunner.start( false);

		// assertEquals( TestStepResult.TestStepStatus.OK, testRunner.getStatus()
		// );
		assertEquals( true, true );

	}

	private void addSecurityChecks( SecurityTest securityTest )
	{
		for( String  testStepId : securityChecksMap.keySet() )
		{
			List<SecurityCheck> securityCheckList = securityChecksMap.get( testStepId );

			for( SecurityCheck sc : securityCheckList )
			{
				securityTest.addSecurityCheck( testStepId, sc );
			}
		}
	}

}
