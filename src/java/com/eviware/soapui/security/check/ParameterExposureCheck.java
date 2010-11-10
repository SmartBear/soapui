/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.security.check;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTest;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * 
 * @author soapui team
 */

public class ParameterExposureCheck extends AbstractSecurityCheck
{

	public static final String SCRIPT_PROPERTY = ParameterExposureCheck.class.getName() + "@script";

	public ParameterExposureCheck( SecurityCheckConfig config )
	{
		super( config );
		monitorApplicable = true;
	}

	@Override
	protected void execute( TestStep testStep )
	{
		if (testStep instanceof HttpTestRequestStep) {
			WsdlTestCaseRunner testCaseRunner = new WsdlTestCaseRunner( (WsdlTestCase)testStep.getTestCase(), new StringToObjectMap() );
			testCaseRunner.runTestStepByName( testStep.getName() );
		}
		analyze(testStep);		
	}

	@Override
	public void analyze(TestStep testStep) {
		// TODO Auto-generated method stub
		
	}
	
}
