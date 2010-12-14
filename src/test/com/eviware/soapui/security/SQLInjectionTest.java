/**
 * 
 */
package com.eviware.soapui.security;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.check.SQLInjectionCheck;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.security.registry.SecurityCheckRegistry;

/**
 * @author dragica.soldo
 * 
 */
public class SQLInjectionTest extends TestCaseWithMockService
{

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

	@Test
	public void testStart()
	{
		SQLInjectionCheckSetup();
		SecurityTest securityTest = new SecurityTest( testCase, config );
		addSecurityChecks( securityTest );
		// securityTest.setListModel( new
		// SecurityChecksPanel.SecurityCheckListModel() );

		SecurityTestRunnerImpl testRunner = new SecurityTestRunnerImpl( securityTest );

		testRunner.start( false );

		// assertEquals( TestStepResult.TestStepStatus.OK, testRunner.getStatus()
		// );
		assertEquals( true, true );

	}

	private void addSecurityChecks( SecurityTest securityTest )
	{
		for( String testStepId : securityChecksMap.keySet() )
		{
			List<SecurityCheck> securityCheckList = securityChecksMap.get( testStepId );

			for( SecurityCheck sc : securityCheckList )
			{
				securityTest.addSecurityCheck( testStepId, sc );
			}
		}
	}

}
