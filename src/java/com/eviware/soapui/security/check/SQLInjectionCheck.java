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

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.lang.StringUtils;

import com.eviware.soapui.config.SQLInjectionCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.HttpResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestInterface;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStepInterface;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.SamplerTestStep;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.fuzzer.Fuzzer;
import com.eviware.soapui.security.log.SecurityTestLogMessageEntry;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.support.components.SimpleForm;
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
	private static final int MINIMUM_STRING_DISTANCE = 50;

	public SQLInjectionCheck( SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( config, parent, icon );
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

	protected void execute( TestStep testStep, WsdlTestRunContext context, SecurityTestLogModel securityTestLog )
	{
		if( acceptsTestStep( testStep ) )
		{
			WsdlTestCaseRunner testCaseRunner = new WsdlTestCaseRunner( ( WsdlTestCase )testStep.getTestCase(),
					new StringToObjectMap() );
			testStep.run( testCaseRunner, testCaseRunner.getRunContext() );

			HttpTestRequestInterface<?> request = ( ( HttpTestRequestStepInterface )testStep ).getTestRequest();
			String originalResponse = request.getResponse().getContentAsXml();

			for( String param : getParamsToCheck() )
			{
				Fuzzer sqlFuzzer = Fuzzer.getSQLFuzzer();

				while( sqlFuzzer.hasNext() )
				{
					sqlFuzzer.getNextFuzzedTestStep( testStep, param );
					testStep.run( testCaseRunner, testCaseRunner.getRunContext() );
					HttpTestRequestInterface<?> lastRequest = ( ( HttpTestRequestStepInterface )testStep ).getTestRequest();

					if( StringUtils
							.getLevenshteinDistance( originalResponse, lastRequest.getResponse().getContentAsString() ) > MINIMUM_STRING_DISTANCE )
					{
						securityTestLog
								.addEntry( new SecurityTestLogMessageEntry( "Possible SQL Injection Vulnerability Detected",
										new HttpResponseMessageExchange( lastRequest ) ) );
					}
					analyze( testStep, context, securityTestLog );

					// maybe this fuzzer can be implemented to wrap the security
					// check not vice versa

				}

			}
		}
	}

	public void analyze( TestStep testStep, WsdlTestRunContext context, SecurityTestLogModel securityTestLog )
	{
		// TODO: Make this test more extensive
		HttpTestRequestInterface<?> lastRequest = ( ( HttpTestRequestStepInterface )testStep ).getTestRequest();
		if( lastRequest.getResponseContentAsString().indexOf( "SQL Error" ) > -1 )
		{
			securityTestLog.addEntry( new SecurityTestLogMessageEntry( "SQL Error displayed in response",
					new HttpResponseMessageExchange( lastRequest ) ) );
		}
	}

	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return testStep instanceof SamplerTestStep;
	}

	@Override
	public JComponent getComponent()
	{
		// if (panel == null) {
		panel = new JPanel( new BorderLayout() );

		form = new SimpleForm();
		form.addSpace( 5 );

		panel.add( form.getPanel() );
		return panel;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public void checkForSensitiveInformationExposure( TestStep testStep, WsdlTestRunContext context,
			SecurityTestLogModel securityTestLog )
	{
		InformationExposureCheck iec = new InformationExposureCheck( config, null, null );
		iec.analyze( testStep, context, securityTestLog );
	}
}
