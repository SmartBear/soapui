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

import java.util.Map;

import javax.swing.JTextField;

import com.eviware.soapui.config.ParameterExposureCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.wsdl.monitor.JProxyServletWsdlMonitorMessageExchange;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityTestRunner;
import com.eviware.soapui.security.log.JSecurityTestRunLog;
import com.eviware.soapui.security.monitor.HttpSecurityAnalyser;
import com.eviware.soapui.security.ui.ParameterExposureCheckPanel;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;

/**
 * This checks whether any parameters sent in the request are included in the
 * response, If they do appear, this is a good parameter to look at as a
 * possible attack vector for XSS
 * 
 * @author soapui team
 */

public class ParameterExposureCheck extends AbstractSecurityCheck implements HttpSecurityAnalyser
{

	protected JTextField minimumCharactersTextField;

	public static final String TYPE = "ParameterExposureCheck";
	public static final int DEFAULT_MINIMUM_CHARACTER_LENGTH = 5;
	private static final String checkTitle = "Configure Parameter Exposure";

	public ParameterExposureCheck( SecurityCheckConfig config, ModelItem parent, String icon, TestStep testStep )
	{
		super( testStep, config, parent, icon );
		if( config == null )
		{
			config = SecurityCheckConfig.Factory.newInstance();
			ParameterExposureCheckConfig pescc = ParameterExposureCheckConfig.Factory.newInstance();
			pescc.setMinimumLength( DEFAULT_MINIMUM_CHARACTER_LENGTH );
			config.setConfig( pescc );
		}
		if( config.getConfig() == null )
		{
			ParameterExposureCheckConfig pescc = ParameterExposureCheckConfig.Factory.newInstance();
			pescc.setMinimumLength( DEFAULT_MINIMUM_CHARACTER_LENGTH );
			config.setConfig( pescc );
		}
	}

	@Override
	protected void execute( SecurityTestRunner securityTestRunner, TestStep testStep, SecurityTestRunContext context )
	{
		if( acceptsTestStep( testStep ) )
		{
			testStep.run( (TestCaseRunner)securityTestRunner, ( TestCaseRunContext )securityTestRunner.getRunContext() );
		}
	}

	// @Override
	// public void analyze( TestStep testStep, SecurityTestRunContext context )
	// {
	// if( acceptsTestStep( testStep ) )
	// {
	// HttpTestRequestStepInterface testStepwithProperties = (
	// HttpTestRequestStepInterface )testStep;
	// HttpTestRequestInterface<?> request =
	// testStepwithProperties.getTestRequest();
	// MessageExchange messageExchange = new HttpResponseMessageExchange( request
	// );
	//
	// RestParamsPropertyHolder params;
	//
	// AbstractHttpRequest<?> httpRequest =
	// testStepwithProperties.getHttpRequest();
	//
	// params = httpRequest.getParams();
	//
	// List<TestProperty> paramsToCheck;
	//
	// paramsToCheck = getParameters().getPropertyList();
	//
	// for( TestProperty parameter : paramsToCheck )
	// {
	// if( parameter != null )
	// {
	// TestProperty testParameter = params.get( parameter.getName() );
	//
	// if( testParameter != null && testParameter.getValue() != null
	// && testParameter.getValue().length() >= getMinimumLength() )
	// {
	// TestAssertionConfig assertionConfig =
	// TestAssertionConfig.Factory.newInstance();
	// assertionConfig.setType( SimpleContainsAssertion.ID );
	//
	// if( SecurityCheckUtil.contains( context, new String(
	// messageExchange.getRawResponseData() ),
	// testParameter.getValue(), false ) )
	// {
	// // TODO refactor through SecurityCheckResult
	// // securityTestLog
	// // .addEntry(new SecurityTestLogMessageEntry(
	// // "The parameter "
	// // + testParameter.getName()
	// // + " with the value \""
	// // + testParameter.getValue()
	// // + "\" is exposed in the response",
	// // messageExchange));
	// securityCheckRequestResult.setStatus( SecurityCheckStatus.FAILED );
	// }
	// }
	// }
	// }
	// // if (getStatus() != Status.FAILED)
	// // setStatus(Status.FINISHED);
	// }
	// // TODO
	// }

	/**
	 * Setting the minimum size that the parameter value will be for it to be
	 * checked
	 * 
	 * @param minimumLength
	 */
	public void setMinimumLength( int minimumLength )
	{
		( ( ParameterExposureCheckConfig )getConfig().getConfig() ).setMinimumLength( minimumLength );
	}

	/**
	 * Get the minimum length for a parameter to be checked
	 * 
	 * @return
	 */
	public int getMinimumLength()
	{
		return ( ( ParameterExposureCheckConfig )getConfig().getConfig() ).getMinimumLength();
	}

	// QUESTION:
	// Why not soap too?
	@Override
	public boolean acceptsTestStep( TestStep testStep )
	{
		return testStep instanceof HttpTestRequestStep || testStep instanceof RestTestRequestStep;
	}

	@Override
	public SecurityCheckConfigPanel getComponent()
	{

		return new ParameterExposureCheckPanel( this );
	}

	// @Override
	// public boolean configure()
	// {
	// if( dialog == null )
	// {
	// buildDialog();
	// }
	//
	// UISupport.showDialog( dialog );
	// return true;
	// }
	//
	// protected void buildDialog()
	// {
	// dialog = new JDialog( UISupport.getMainFrame(), "Parameter Exposure",
	// true
	// );
	// dialog.setContentPane( getComponent( ) );
	// dialog.setSize( 600, 500 );
	// dialog.setModal( true );
	// dialog.pack();
	// }

	@Override
	public String getType()
	{
		return TYPE;
	}

	@Override
	public void analyzeHttpConnection( MessageExchange messageExchange, JSecurityTestRunLog securityTestLog )
	{
		Map<String, String> parameters = ( ( JProxyServletWsdlMonitorMessageExchange )messageExchange )
				.getHttpRequestParameters();
		for( String paramName : parameters.keySet() )
		{

			String paramValue = parameters.get( paramName );

			if( paramValue != null && paramValue.length() >= getMinimumLength() )
			{
				if( messageExchange.getResponseContent().indexOf( paramValue ) > -1 && securityTestLog != null )
				{
					// TODO refactor through SecurityCheckResult
					// securityTestLog.addEntry(new SecurityTestLogMessageEntry(
					// "The parameter " + paramName + " with the value \""
					// + paramValue
					// + "\" is exposed in the response",
					// messageExchange));
				}
			}
		}

	}

	@Override
	public boolean canRun()
	{
		return true;
	}

	@Override
	protected boolean hasNext(TestStep testStep,SecurityTestRunContext context)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getConfigDescription()
	{
		return "Configures parameter exposure security check";
	}

	@Override
	public String getConfigName()
	{
		return "Parameter Exposure Security Check";
	}

	@Override
	public String getHelpURL()
	{
		return "http://www.soapui.org";
	}
}
