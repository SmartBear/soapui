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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.Document;

import com.eviware.soapui.config.ParameterExposureCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.monitor.JProxyServletWsdlMonitorMessageExchange;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCaseRunner;
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
import com.eviware.soapui.security.SecurityCheckRequestResult;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.SecurityCheckRequestResult.SecurityCheckStatus;
import com.eviware.soapui.security.log.JSecurityTestRunLog;
import com.eviware.soapui.security.log.SecurityTestLogMessageEntry;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.security.monitor.HttpSecurityAnalyser;
import com.eviware.soapui.security.ui.ParameterExposureCheckPanel;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * This checks whether any parameters sent in the request are included in the
 * response, If they do appear, this is a good parameter to look at as a
 * possible attack vector for XSS
 * 
 * @author soapui team
 */

public class ParameterExposureCheck extends AbstractSecurityCheck implements
		HttpSecurityAnalyser {

	protected JTextField minimumCharactersTextField;

	public static final String TYPE = "ParameterExposureCheck";
	public static final int DEFAULT_MINIMUM_CHARACTER_LENGTH = 5;
	private static final String checkTitle = "Configure Parameter Exposure";

	public ParameterExposureCheck(SecurityCheckConfig config, ModelItem parent,
			String icon) {
		super(config, parent, icon);
		if (config == null) {
			config = SecurityCheckConfig.Factory.newInstance();
			ParameterExposureCheckConfig pescc = ParameterExposureCheckConfig.Factory
					.newInstance();
			pescc.setMinimumLength(DEFAULT_MINIMUM_CHARACTER_LENGTH);
			config.setConfig(pescc);
		}
		if (config.getConfig() == null) {
			ParameterExposureCheckConfig pescc = ParameterExposureCheckConfig.Factory
					.newInstance();
			pescc.setMinimumLength(DEFAULT_MINIMUM_CHARACTER_LENGTH);
			config.setConfig(pescc);
		}
	}

	@Override
	protected SecurityCheckRequestResult execute(TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog, SecurityCheckRequestResult securityChekResult) {
		if (acceptsTestStep(testStep)) {
			WsdlTestCaseRunner testCaseRunner = new WsdlTestCaseRunner(
					(WsdlTestCase) testStep.getTestCase(),
					new StringToObjectMap());

			testStep.run(testCaseRunner, testCaseRunner.getRunContext());
			analyze(testStep, context, securityTestLog, null);
		}
		//TODO
		return null;
	}

	@Override
	public SecurityCheckRequestResult analyze(TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog, SecurityCheckRequestResult securityCheckResult) {
		if (acceptsTestStep(testStep)) {
			HttpTestRequestStepInterface testStepwithProperties = (HttpTestRequestStepInterface) testStep;
			HttpTestRequestInterface<?> request = testStepwithProperties
					.getTestRequest();
			MessageExchange messageExchange = new HttpResponseMessageExchange(
					request);

			Map<String, TestProperty> params;

			AbstractHttpRequest<?> httpRequest = testStepwithProperties
					.getHttpRequest();
			params = httpRequest.getParams();

			if (getParamsToCheck().isEmpty()) {
				setParamsToCheck(new ArrayList<String>(params.keySet()));
			}

			for (String paramName : getParamsToCheck()) {
				if (paramName != null) {
					TestProperty param = params.get(paramName);

					if (param != null && param.getValue() != null
							&& param.getValue().length() >= getMinimumLength()) {
						TestAssertionConfig assertionConfig = TestAssertionConfig.Factory
								.newInstance();
						assertionConfig.setType(SimpleContainsAssertion.ID);

						SimpleContainsAssertion containsAssertion = (SimpleContainsAssertion) TestAssertionRegistry
								.getInstance().buildAssertion(assertionConfig,
										testStepwithProperties);
						containsAssertion.setToken(param.getValue());

						containsAssertion.assertResponse(messageExchange,
								context);

						if (containsAssertion.getStatus().equals(
								AssertionStatus.VALID)) {
							securityTestLog
									.addEntry(new SecurityTestLogMessageEntry(
											"The parameter "
													+ param.getName()
													+ " with the value \""
													+ param.getValue()
													+ "\" is exposed in the response",
											messageExchange));
							securityCheckResult.setStatus(SecurityCheckStatus.FAILED);
						}
					}
				}
			}
//			if (getStatus() != Status.FAILED)
//				setStatus(Status.FINISHED);
		}
		//TODO
		return null;
	}

	/**
	 * Setting the minimum size that the parameter value will be for it to be
	 * checked
	 * 
	 * @param minimumLength
	 */
	public void setMinimumLength(int minimumLength) {
		((ParameterExposureCheckConfig) config.getConfig())
				.setMinimumLength(minimumLength);
	}

	/**
	 * Get the minimum length for a parameter to be checked
	 * 
	 * @return
	 */
	public int getMinimumLength() {
		return ((ParameterExposureCheckConfig) config.getConfig())
				.getMinimumLength();
	}

	@Override
	public boolean acceptsTestStep(TestStep testStep) {
		return testStep instanceof HttpTestRequestStep
				|| testStep instanceof RestTestRequestStep;
	}

	@Override
	public SecurityCheckConfigPanel getComponent() {
	
		return new ParameterExposureCheckPanel(this);
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
	public String getType() {
		return TYPE;
	}

	@Override
	public void analyzeHttpConnection(MessageExchange messageExchange,
			JSecurityTestRunLog securityTestLog) {
		Map<String, String> parameters = ((JProxyServletWsdlMonitorMessageExchange) messageExchange)
				.getHttpRequestParameters();
		for (String paramName : parameters.keySet()) {

			String paramValue = parameters.get(paramName);

			if (paramValue != null && paramValue.length() >= getMinimumLength()) {
				if (messageExchange.getResponseContent().indexOf(paramValue) > -1
						&& securityTestLog != null) {
					securityTestLog.addEntry(new SecurityTestLogMessageEntry(
							"The parameter " + paramName + " with the value \""
									+ paramValue
									+ "\" is exposed in the response",
							messageExchange));
				}
			}
		}

	}

	@Override
	public boolean canRun() {
		return true;
	}

	@Override
	public String getTitle() {
		return checkTitle;
	}

}
