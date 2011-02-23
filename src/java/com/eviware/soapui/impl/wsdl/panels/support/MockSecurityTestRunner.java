package com.eviware.soapui.impl.wsdl.panels.support;

import org.apache.log4j.Logger;

import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepFactory;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestStepRegistry;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.TestAssertion;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityCheckResult;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.check.AbstractSecurityCheck;

public class MockSecurityTestRunner extends AbstractMockTestRunner<SecurityTest> implements SecurityTestRunner
{

	private SecurityTest securityTest;

	public MockSecurityTestRunner( SecurityTest modelItem )
	{
		super( modelItem, null );
	}

	public MockSecurityTestRunner( SecurityTest modelItem, Logger logger )
	{
		super( modelItem, logger );
		this.securityTest = modelItem;
	}

	@Override
	public SecurityTest getSecurityTest()
	{
		return securityTest;
	}

	@Override
	public SecurityCheckResult runTestStepSecurityCheck( SecurityTestRunContext runContext, TestStep testStep,
			AbstractSecurityCheck securityCheck )
	{
		return securityCheck.run( cloneForSecurityCheck( ( WsdlTestStep )testStep ), runContext );
	}
	
	private TestStep cloneForSecurityCheck( WsdlTestStep sourceTestStep )
	{
		WsdlTestStep clonedTestStep = null;
		TestStepConfig testStepConfig = ( TestStepConfig )sourceTestStep.getConfig().copy();
		WsdlTestStepFactory factory = WsdlTestStepRegistry.getInstance().getFactory( testStepConfig.getType() );
		if( factory != null )
		{
			clonedTestStep = factory.buildTestStep( securityTest.getTestCase(), testStepConfig, false );
			if( clonedTestStep instanceof Assertable )
			{
				for( TestAssertion assertion : ( ( Assertable )clonedTestStep ).getAssertionList() )
				{
					( ( Assertable )clonedTestStep ).removeAssertion( assertion );
				}
			}
		}
		return clonedTestStep;
	}

}
