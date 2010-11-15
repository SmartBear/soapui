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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.eviware.soapui.config.ParameterExposureCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.TestAssertionConfig;
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
import com.eviware.soapui.security.SecurityTestContext;
import com.eviware.soapui.security.log.SecurityTestLog;
import com.eviware.soapui.security.log.SecurityTestLogMessageEntry;
import com.eviware.soapui.support.types.StringToObjectMap;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author soapui team
 */

public class ParameterExposureCheck extends AbstractSecurityCheck {
	ParameterExposureCheckConfig parameterExposureCheckConfig;
	JTextField minimumCharactersTextField;
	
	public static final String TYPE = "ParameterExposureCheck";
	
	public ParameterExposureCheck(SecurityCheckConfig config, ModelItem parent,
			String icon) {
		super(config, parent, icon);
		monitorApplicable = true;
		if( config.getConfig() == null )
		{
			parameterExposureCheckConfig = ( ParameterExposureCheckConfig )config.addNewConfig().changeType(
					ParameterExposureCheckConfig.type );
		}
		else
		{
			parameterExposureCheckConfig = (ParameterExposureCheckConfig) config
				.getConfig().changeType(ParameterExposureCheckConfig.type);
		}
		
		minimumCharactersTextField = new JTextField(((ParameterExposureCheckConfig) config
				.getConfig()).getMinimumLength());
	}

	@Override
	protected void execute(TestStep testStep, SecurityTestContext context,
			SecurityTestLog securityTestLog) {
		if (acceptsTestStep(testStep)) {
			WsdlTestCaseRunner testCaseRunner = new WsdlTestCaseRunner(
					(WsdlTestCase) testStep.getTestCase(),
					new StringToObjectMap());
			testCaseRunner.runTestStepByName(testStep.getName());
		}
		analyze(testStep, context, securityTestLog);
	}

	@Override
	public void analyze(TestStep testStep, SecurityTestContext context,
			SecurityTestLog securityTestLog) {
		if (acceptsTestStep(testStep)) {
			HttpTestRequestStepInterface testStepwithProperties = (HttpTestRequestStepInterface) testStep;
			HttpTestRequestInterface<?> request = testStepwithProperties
					.getTestRequest();
			MessageExchange messageExchange = new HttpResponseMessageExchange(
					request);

			Map<String, TestProperty> params = testStepwithProperties
					.getProperties();

			if (getParamsToCheck().isEmpty()) {
				setParamsToCheck(new ArrayList<String>(params.keySet()));
			}
			for (String paramName : getParamsToCheck()) {
				TestProperty param = params.get(paramName);
				if (param != null
						&& param.getValue().length() >= getMinimumLength()) {
					TestAssertionConfig assertionConfig = TestAssertionConfig.Factory
							.newInstance();
					assertionConfig.setType(SimpleContainsAssertion.ID);

					SimpleContainsAssertion containsAssertion = (SimpleContainsAssertion) TestAssertionRegistry
							.getInstance().buildAssertion(assertionConfig,
									testStepwithProperties);
					containsAssertion.setToken(param.getValue());

					containsAssertion.assertResponse(messageExchange, context);

					if (containsAssertion.getStatus().equals(
							AssertionStatus.VALID)) {
						securityTestLog
								.addEntry(new SecurityTestLogMessageEntry(
										"Parameter " + param.getName()
												+ " is exposed in the response"));
					}
				}
			}
		}
	}

	public void setMinimumLength(int minimumLength) {
		parameterExposureCheckConfig.setMinimumLength(minimumLength);
		minimumCharactersTextField.setText(Integer.toString(minimumLength));
	}

	private int getMinimumLength() {
		return parameterExposureCheckConfig.getMinimumLength();
	}

	public List<String> getParamsToCheck() {
		return parameterExposureCheckConfig.getParamToCheckList();
	}

	public void setParamsToCheck(List<String> params) {
		parameterExposureCheckConfig.setParamToCheckArray(params
				.toArray(new String[0]));
	}

	@Override
	public boolean acceptsTestStep(TestStep testStep) {
		return testStep instanceof HttpTestRequestStep
				|| testStep instanceof RestTestRequestStep;
	}

	@Override
	public JComponent getComponent() {
		JPanel panel = new JPanel(new FormLayout(""));
		panel.add(new JLabel("Minimum Characters:"));
		panel.add(new JTextField(((ParameterExposureCheckConfig) config
				.getConfig()).getMinimumLength()));
		return panel;
	}

	private class MinimumListener implements KeyListener {

		@Override
		public void keyPressed(KeyEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void keyTyped(KeyEvent ke) {
			char c = ke.getKeyChar();
			if (!Character.isDigit(c))
			ke.consume(); 
			setMinimumLength(Integer.parseInt(minimumCharactersTextField.getText()));
		}
	}

}
