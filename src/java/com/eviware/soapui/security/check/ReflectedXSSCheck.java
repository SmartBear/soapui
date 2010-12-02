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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.eviware.soapui.config.ParameterExposureCheckConfig;
import com.eviware.soapui.config.ReflectedXSSCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.http.HttpRequest;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.HttpResponseMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestInterface;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStepInterface;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.TestAssertionRegistry;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SimpleContainsAssertion;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.Assertable.AssertionStatus;
import com.eviware.soapui.security.SecurityTestContext;
import com.eviware.soapui.security.log.SecurityTestLogMessageEntry;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * This will test whether a targeted web page is vulnerable to 
 * reflected XSS attacks
 * 
 * @author soapui team
 */

public class ReflectedXSSCheck extends AbstractSecurityCheck  implements SensitiveInformationCheckable
{

	public static final String TYPE = "ReflectedXSSCheck";

	public ReflectedXSSCheck( SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( config, parent, icon );
		if( config == null )
		{
			config = SecurityCheckConfig.Factory.newInstance();
			ReflectedXSSCheckConfig pescc = ReflectedXSSCheckConfig.Factory.newInstance();
			config.setConfig( pescc );
		}
		if( config.getConfig() == null )
		{
			ReflectedXSSCheckConfig pescc = ReflectedXSSCheckConfig.Factory.newInstance();
			config.setConfig( pescc );
		}
	}

	protected void execute( TestStep testStep, SecurityTestContext context, SecurityTestLogModel securityTestLog )
	{
		if( acceptsTestStep( testStep ) )
		{
			WsdlTestCaseRunner testCaseRunner = new WsdlTestCaseRunner( ( WsdlTestCase )testStep.getTestCase(),
					new StringToObjectMap() );

			testStep.run( testCaseRunner, testCaseRunner.getRunContext() );
			analyze( testStep, context, securityTestLog );
		}
	}

	public void analyze( TestStep testStep, SecurityTestContext context, SecurityTestLogModel securityTestLog )
	{
		if( acceptsTestStep( testStep ) )
		{
			HttpTestRequestStepInterface testStepwithProperties = ( HttpTestRequestStepInterface )testStep;
			HttpTestRequestInterface<?> request = testStepwithProperties.getTestRequest();
			MessageExchange messageExchange = new HttpResponseMessageExchange( request );

			Map<String, TestProperty> params;

			// It might be a good idea to refactor HttpRequest and TestRequest to
			// avoid things like this)

			AbstractHttpRequest<?> httpRequest = testStepwithProperties.getHttpRequest();
			if( httpRequest instanceof HttpRequest )
			{
				params = ( ( HttpRequest )httpRequest ).getParams();
			}
			else
			{
				params = ( ( RestRequest )httpRequest ).getParams();
			}

			if( getParamsToCheck().isEmpty() )
			{
				setParamsToCheck( new ArrayList<String>( params.keySet() ) );
			}

			for( String paramName : getParamsToCheck() )
			{
				TestProperty param = params.get( paramName );

			
			}
		}
	}


	/**
	 * Returns the list of parameters that the response will be checked for
	 * 
	 * @return A list of parameter objects
	 */
	public List<String> getParamsToCheck()
	{
		return ( ( ParameterExposureCheckConfig )config.getConfig() ).getParamToCheckList();
	}

	/**
	 * A list of parameters that will be checked in the response
	 * 
	 * @param params
	 */
	public void setParamsToCheck( List<String> params )
	{
		( ( ParameterExposureCheckConfig )config.getConfig() ).setParamToCheckArray( params.toArray( new String[0] ) );
	}

	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return testStep instanceof HttpTestRequestStep || testStep instanceof RestTestRequestStep;
	}

	@Override
	public JComponent getComponent(TestStep testStep)
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
	public void analyze(TestStep testStep, WsdlTestRunContext context,
			SecurityTestLogModel securityTestLog) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void execute(TestStep testStep, WsdlTestRunContext context,
			SecurityTestLogModel securityTestLog) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkForSensitiveInformationExposure( TestStep testStep, WsdlTestRunContext context,
			SecurityTestLogModel securityTestLog )
	{
		InformationExposureCheck iec = new InformationExposureCheck( config, null, null);
		iec.analyze( testStep, context, securityTestLog );
	}
}
