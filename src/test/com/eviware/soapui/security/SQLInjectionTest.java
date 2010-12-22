/**
 * 
 */
package com.eviware.soapui.security;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.security.check.SQLInjectionCheck;
import com.eviware.soapui.security.registry.SecurityCheckRegistry;

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
		securityCheckName = SQLInjectionCheck.TYPE;
	}
	
	@Override
	protected void addSecurityCheckConfig( SecurityCheckConfig securityCheckConfig )
	{

		SQLInjectionCheck sqlCheck = ( SQLInjectionCheck )SecurityCheckRegistry.getInstance().getFactory(
				securityCheckType ).buildSecurityCheck( securityCheckConfig );

		List<String> params = new ArrayList<String>();
		params.add( "q" );
	}

	@Test
	public void testStart()
	{

//		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( createSecurityTest() );
//
//		testRunner.start( false );
//
//		// assertEquals( TestStepResult.TestStepStatus.OK, testRunner.getStatus()
//		// );
//		
//		//TODO: finish
//		assertEquals( true, true );

	}



}
