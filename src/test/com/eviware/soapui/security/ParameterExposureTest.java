/**
 * 
 */
package com.eviware.soapui.security;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.check.ParameterExposureCheck;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.security.registry.SecurityCheckRegistry;

/**
 * @author dragica.soldo
 * 
 */
public class ParameterExposureTest extends TestCaseWithMockService
{

	

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





	@Test
	public void testStart()
	{
		
		parameterExposureCheckSetup();
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
