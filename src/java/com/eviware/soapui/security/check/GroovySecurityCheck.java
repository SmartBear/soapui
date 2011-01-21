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

import javax.swing.JTextField;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.GroovySecurityCheckConfig;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.SecurityTestRunContext;
import com.eviware.soapui.security.log.JSecurityTestRunLog;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.security.monitor.HttpSecurityAnalyser;
import com.eviware.soapui.security.ui.GroovySecurityCheckPanel;
import com.eviware.soapui.security.ui.SecurityCheckConfigPanel;

/**
 * 
 * @author soapui team
 */

public class GroovySecurityCheck extends AbstractSecurityCheck implements
		HttpSecurityAnalyser {
	public static final String SCRIPT_PROPERTY = GroovySecurityCheck.class
			.getName()
			+ "@script";
	public static final String TYPE = "GroovySecurityCheck";
	// if this is a text area document listener doesn't work, WHY? !!
	protected JTextField scriptTextArea;

	private static final String checkTitle = "Configure GroovyScript Check";

	public GroovySecurityCheck(SecurityCheckConfig config, ModelItem parent,
			String icon) {
		super(config, parent, icon);
		if (config == null) {
			config = SecurityCheckConfig.Factory.newInstance();
			GroovySecurityCheckConfig groovyscc = GroovySecurityCheckConfig.Factory
					.newInstance();
			config.setConfig(groovyscc);
		}

	}

	@Override
	protected void execute(TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog) {
		scriptEngine.setScript(getScript());
		scriptEngine.setVariable("testStep", testStep);
		scriptEngine.setVariable("log", SoapUI.ensureGroovyLog());
		scriptEngine.setVariable("context", context);
		scriptEngine.setVariable("status", status);
		scriptEngine.setVariable("executionStrategy", getExecutionStrategy());

		try {
			scriptEngine.run();
		} catch (Exception e) {
			SoapUI.logError(e);
		} finally {
			scriptEngine.clearVariables();
		}
	}

	public void setScript(String script) {
		String old = getScript();
		if (getConfig().getConfig() == null) {
			getConfig().addNewConfig();
		}
		GroovySecurityCheckConfig groovyscc = GroovySecurityCheckConfig.Factory
				.newInstance();
		groovyscc.addNewScript();
		groovyscc.getScript().setStringValue(script);
		getConfig().setConfig(groovyscc);
		notifyPropertyChanged(SCRIPT_PROPERTY, old, script);
	}

	public String getScript() {
		GroovySecurityCheckConfig groovyscc = null;
		if (getConfig().getConfig() != null) {
			groovyscc = (GroovySecurityCheckConfig) getConfig().getConfig();
			if (groovyscc.getScript() != null)
				return groovyscc.getScript().getStringValue();
		}
		return "";
	}

	@Override
	public void analyze(TestStep testStep, SecurityTestRunContext context,
			SecurityTestLogModel securityTestLog) {

	}

	@Override
	public boolean acceptsTestStep(TestStep testStep) {
		return true;
	}

	@Override
	public SecurityCheckConfigPanel getComponent() {
		return new GroovySecurityCheckPanel(this);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void analyzeHttpConnection(MessageExchange messageExchange,
			JSecurityTestRunLog securityTestLog) {
		scriptEngine.setScript(getScript());
		scriptEngine.setVariable("testStep", null);
		scriptEngine.setVariable("log", SoapUI.ensureGroovyLog());
		scriptEngine.setVariable("context", null);
		scriptEngine.setVariable("messageExchange", messageExchange);

		try {
			scriptEngine.run();
		} catch (Exception e) {
			SoapUI.logError(e);
		} finally {
			scriptEngine.clearVariables();
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
