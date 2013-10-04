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

package com.eviware.soapui.impl.wsdl.testcase;

import com.eviware.soapui.impl.wsdl.support.AbstractTestCaseRunner;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * WSDL TestCase Runner - runs all steps in a testcase and collects performance
 * data
 * 
 * @author Ole.Matzura
 */

public class WsdlTestCaseRunner extends AbstractTestCaseRunner<WsdlTestCase, WsdlTestRunContext>
{

	@SuppressWarnings( "unchecked" )
	public WsdlTestCaseRunner( WsdlTestCase testCase, StringToObjectMap properties )
	{
		super( testCase, properties );
	}

	public WsdlTestRunContext createContext( StringToObjectMap properties )
	{
		return new WsdlTestRunContext( this, properties, this.getTestCase() );
	}

	@Override
	protected int runCurrentTestStep( WsdlTestRunContext runContext, int currentStepIndex )
	{
		TestStep currentStep = runContext.getCurrentStep();
		if( !currentStep.isDisabled() )
		{
			TestStepResult stepResult = runTestStep( currentStep, true, true );
			if( stepResult == null )
				return -2;

			if( !isRunning() )
				return -2;

			if( getGotoStepIndex() != -1 )
			{
				currentStepIndex = getGotoStepIndex() - 1;
				gotoStep( -1 );
			}
		}

		runContext.setCurrentStep( currentStepIndex + 1 );
		return currentStepIndex;
	}

	@Override
	public WsdlTestCase getTestCase()
	{
		return getTestRunnable();
	}

	@Override
	protected void failTestRunnableOnErrors( WsdlTestRunContext runContext )
	{
		if( runContext.getProperty( TestCaseRunner.Status.class.getName() ) == TestCaseRunner.Status.FAILED
				&& getTestCase().getFailTestCaseOnErrors() )
		{
			fail( "Failing due to failed test step" );
		}
	}
}
