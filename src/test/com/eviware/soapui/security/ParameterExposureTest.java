/**
 * 
 */
package com.eviware.soapui.security;

import org.junit.Before;
import org.junit.Test;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.security.check.ParameterExposureCheck;
import com.eviware.soapui.security.registry.SecurityCheckRegistry;

/**
 * @author dragica.soldo
 * 
 */
public class ParameterExposureTest extends AbstractSecurityTestCaseWithMockService
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
		securityCheckType = ParameterExposureCheck.TYPE;
		securityCheckName = ParameterExposureCheck.TYPE;
	}

	@Override
	protected void addSecurityCheckConfig( SecurityCheckConfig securityCheckConfig )
	{

		SecurityCheckRegistry.getInstance().getFactory(
				securityCheckType ).buildSecurityCheck( securityCheckConfig );

	}

	@Test
	public void testStart()
	{

		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( createSecurityTest() );

		testRunner.start( false );
		//TODO: finish
		assertEquals( true, true );

	}

}
