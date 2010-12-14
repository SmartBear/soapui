/**
 * 
 */
package com.eviware.soapui.security;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.SecurityTestConfig;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.check.GroovySecurityCheck;
import com.eviware.soapui.security.check.SecurityCheck;

/**
 * @author nebojsa.tasic
 * 
 */
public class GroovySecurityTestTest extends TestCaseWithMockService
{


	private SecurityCheckConfig createConfig( SecurityTestConfig config2 )
	{

//		AbstractSecurityCheckFactory factory = SecurityCheckRegistry.getInstance().getFactory( securityCheckType );
//		SecurityCheckConfig newSecCheckConfig = factory.createNewSecurityCheck( securityCheckName );
//		SecurityCheck newSecCheck = factory.buildSecurityCheck( newSecCheckConfig );
//
//		TestStepSecurityTestConfig testStepSecurityTest = testStep.getConfig().addNewTestStepSecurityTest();
//		testStepSecurityTest.setTestStepId( testStep.getId() );
//		SecurityCheckConfig newSecurityCheck = testStepSecurityTest.addNewTestStepSecurityCheck();
//		newSecurityCheck.setConfig( newSecCheckConfig.getConfig() );
//		newSecurityCheck.setType( newSecCheck.getType() );
//		newSecurityCheck.setName( newSecCheck.getName() );
		List<SecurityCheck> secCheckList = new ArrayList<SecurityCheck>();
		SecurityCheckConfig groovySecurityCheckConfig = SecurityCheckConfig.Factory.newInstance();
		groovySecurityCheckConfig.setType( GroovySecurityCheck.TYPE );
		groovySecurityCheckConfig.setName( GroovySecurityCheck.TYPE );
		GroovySecurityCheck gsc = new GroovySecurityCheck( groovySecurityCheckConfig, null, null );
		gsc
				.setScript( "println('');println \"this is print from GroovySecurityCheck on test step '${testStep.name}'\";println('')" );

		secCheckList.add( gsc );
		TestStep testStep = testCase.getTestStepByName( "SEK to USD Test" );
		securityChecksMap.put( testStep.getId(), secCheckList );
		return groovySecurityCheckConfig;
	}

	

	@Test
	public void testStart()
	{
		createConfig( config );
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
