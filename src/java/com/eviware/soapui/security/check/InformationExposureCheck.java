/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
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
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.TestAssertionConfig;
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
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.Assertable.AssertionStatus;
import com.eviware.soapui.security.log.JSecurityTestRunLog;
import com.eviware.soapui.security.log.SecurityTestLogMessageEntry;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.security.monitor.HttpSecurityAnalyser;
import com.eviware.soapui.support.SecurityCheckUtil;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToObjectMap;

public class InformationExposureCheck extends AbstractSecurityCheck implements HttpSecurityAnalyser
{

	private List<String> exposureList;
	public static final String TYPE = "InformationExposureCheck";

	public InformationExposureCheck( SecurityCheckConfig config, ModelItem parent, String icon )
	{
		super( config, parent, icon );
		if( config == null )
		{
			config = SecurityCheckConfig.Factory.newInstance();
		}
		exposureList = SecurityCheckUtil.entriesList();
	}

	@Override
	protected void execute( TestStep testStep, WsdlTestRunContext context, SecurityTestLogModel securityTestLog )
	{
		if( acceptsTestStep( testStep ) )
		{
			WsdlTestCaseRunner testCaseRunner = new WsdlTestCaseRunner( ( WsdlTestCase )testStep.getTestCase(),
					new StringToObjectMap() );

			testStep.run( testCaseRunner, testCaseRunner.getRunContext() );
			analyze( testStep, context, securityTestLog );
		}
	}

	@Override
	public void analyze( TestStep testStep, WsdlTestRunContext context, SecurityTestLogModel securityTestLog )
	{
		if( acceptsTestStep( testStep ) )
		{
			HttpTestRequestStepInterface testStepwithProperties = ( HttpTestRequestStepInterface )testStep;
			HttpTestRequestInterface<?> request = testStepwithProperties.getTestRequest();
			MessageExchange messageExchange = new HttpResponseMessageExchange( request );

			for( String exposureContent : exposureList )
			{
				if( assertContains( context, testStepwithProperties, messageExchange, exposureContent ).equals(
						AssertionStatus.VALID ) )
				{
					logSecurityInfo( messageExchange, securityTestLog, exposureContent );
				}
			}
		}
	}

	private AssertionStatus assertContains( WsdlTestRunContext context, HttpTestRequestStepInterface testStep,
			MessageExchange messageExchange, String exposureContent )
	{
		TestAssertionConfig assertionConfig = TestAssertionConfig.Factory.newInstance();
		assertionConfig.setType( SimpleContainsAssertion.ID );

		SimpleContainsAssertion containsAssertion = ( SimpleContainsAssertion )TestAssertionRegistry.getInstance()
				.buildAssertion( assertionConfig, testStep );
		containsAssertion.setToken( exposureContent );
		containsAssertion.assertResponse( messageExchange, context );
		return containsAssertion.getStatus();
	}

	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return testStep instanceof HttpTestRequestStep || testStep instanceof RestTestRequestStep;
	}

	@Override
	public JComponent getComponent()
	{
		// if (panel == null) {
		panel = new JPanel( new BorderLayout() );

		form = new SimpleForm();
		form.addSpace( 5 );

		// form.setDefaultTextFieldColumns(40);

		panel.add( form.getPanel() );
		return panel;
	}

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public void analyzeHttpConnection( MessageExchange messageExchange, JSecurityTestRunLog securityTestLog )
	{
		String responseContent = messageExchange.getResponseContent();
		for( String exposureContent : exposureList )
		{
			if( responseContent.contains( exposureContent ) )
			{
				logSecurityInfo( messageExchange, securityTestLog, exposureContent );
			}
		}

	}

	private void logSecurityInfo( MessageExchange messageExchange, JSecurityTestRunLog securityTestLog,
			String exposureContent )
	{
		securityTestLog.addEntry( new SecurityTestLogMessageEntry( "The exposed sensitive information '"
				+ exposureContent + "' is detected in response. ", messageExchange ) );
	}

	private void logSecurityInfo( MessageExchange messageExchange, SecurityTestLogModel securityTestLog,
			String exposureContent )
	{
		securityTestLog.addEntry( new SecurityTestLogMessageEntry( "The exposed sensitive information '"
				+ exposureContent + "' is detected in response. ", messageExchange ) );
	}

	@Override
	public boolean canRun()
	{
		return true;
	}

	@Override
	public boolean configure()
	{
		return false;
	}

}
