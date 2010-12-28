/**
 * 
 */
package com.eviware.soapui.security;

import org.junit.Before;
import org.junit.Test;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.testsuite.TestRunner;
import com.eviware.soapui.security.check.GroovySecurityCheck;

/**
 * @author nebojsa.tasic
 * 
 */
public class GroovySecurityCheckTest extends AbstractSecurityTestCaseWithMockService
{

	/**
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		testStepName = "SEK to USD Test";
		securityCheckType = GroovySecurityCheck.TYPE;
		securityCheckName = GroovySecurityCheck.TYPE;
	}

	

	protected void addSecurityCheckConfig( SecurityCheckConfig securityCheckConfig )
	{
		GroovySecurityCheck gsc = new GroovySecurityCheck( securityCheckConfig, null, null );
		gsc
				.setScript( "println('');println \"this is print from GroovySecurityCheck on test step '${testStep.name}'\";println('')" );
	}
	
	
	
	@Test
	public void testLogTestEnded()
	{
//		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( createSecurityTest() );
//
//		testRunner.start( false );
//
//		assertTrue( "Groovy Security Check Failed due to wrong Log message", testRunner.getSecurityTest()
//				.getSecurityTestLog().getElementAt( 0 ).getMessage().startsWith( "SecurityTest ended" ) );

	}

	@Test
	public void testFinished()
	{
//		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( createSecurityTest() );
//
//		testRunner.start( false );
//
//		assertTrue( "Test Step failed so as GroovySecurityCheck", !testRunner.getStatus().equals(
//				TestRunner.Status.FINISHED ) );

	}

	


}
