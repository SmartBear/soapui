/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
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

import com.eviware.soapui.config.SQLInjectionCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.SamplerTestStep;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityCheckRequestResult.SecurityCheckStatus;
import com.eviware.soapui.security.fuzzer.Fuzzer;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * This will test whether a targeted web page is vulnerable to reflected XSS
 * attacks
 * 
 * @author soapui team
 */

public class SQLInjectionCheck extends AbstractSecurityCheck implements SensitiveInformationCheckable
{

	public static final String TYPE = "SQLInjectionCheck";

	private Fuzzer sqlFuzzer = Fuzzer.getSQLFuzzer();

	public SQLInjectionCheck( SecurityCheckConfig config, ModelItem parent, String icon, TestStep testStep )
	{
		super( testStep, config, parent, icon );
		if( config == null )
		{
			config = SecurityCheckConfig.Factory.newInstance();
			SQLInjectionCheckConfig pescc = SQLInjectionCheckConfig.Factory.newInstance();
			config.setConfig( pescc );
		}
		if( config.getConfig() == null )
		{
			SQLInjectionCheckConfig pescc = SQLInjectionCheckConfig.Factory.newInstance();
			config.setConfig( pescc );
		}
	}

	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return testStep instanceof SamplerTestStep;
	}

	@Override
	public SecurityCheckConfigPanel getComponent()
	{
		return null;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public void checkForSensitiveInformationExposure( TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog )
	{
		InformationExposureCheck iec = new InformationExposureCheck( testStep, config, null, null );
//		iec.analyze( testStep, context );
	}

	@Override
	protected void execute( TestStep testStep, SecurityTestRunContext context )
	{
		sqlFuzzer.getNextFuzzedTestStep( testStep, getParameters() );

		WsdlTestCaseRunner testCaseRunner = new WsdlTestCaseRunner( ( WsdlTestCase )testStep.getTestCase(),
				new StringToObjectMap() );

		testStep.run( testCaseRunner, context );
	}

//	@Override
//	protected void analyze( TestStep testStep, SecurityTestRunContext context )
//	{
//		if( acceptsTestStep( testStep ) )
//		{
//			// HttpTestRequestStepInterface testStepwithProperties = (
//			// HttpTestRequestStepInterface )testStep;
//			// HttpTestRequestInterface<?> request =
//			// testStepwithProperties.getTestRequest();
//			// MessageExchange messageExchange = new HttpResponseMessageExchange(
//			// request );
//
//			// securityCheckReqResult.setMessageExchange( messageExchange );
//			securityCheckRequestResult.setStatus( SecurityCheckStatus.OK );
//
//		}
//	}

	@Override
	protected boolean hasNext()
	{
		return sqlFuzzer.hasNext();
	}

	@Override
	protected void buildDialog()
	{
		// super.buildDialogOld();
	}

	@Override
	public boolean configure()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
