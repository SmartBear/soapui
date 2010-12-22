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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.AMFRequestTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.Securable;
import com.eviware.soapui.security.log.SecurityTestLogModel;
import com.eviware.soapui.security.panels.SecurityChecksPanel;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.scripting.SoapUIScriptEngine;
import com.eviware.soapui.support.scripting.SoapUIScriptEngineRegistry;
import com.jgoodies.forms.builder.ButtonBarBuilder;

public abstract class AbstractSecurityCheck extends SecurityCheck {
	// configuration of specific request modification
	protected SecurityCheckConfig config;
	protected String startupScript;
	protected String tearDownScript;
	protected SoapUIScriptEngine scriptEngine;
	private boolean disabled = false;
	protected JPanel panel;
	protected SimpleForm form;
	protected JDialog dialog;
	private boolean configureResult;
	private JPanel parameterSelector;

	// private
	public AbstractSecurityCheck(SecurityCheckConfig config, ModelItem parent,
			String icon, Securable securable) {
		super(config, parent, icon, securable);
		this.config = config;
		this.startupScript = config.getSetupScript() != null ? config
				.getSetupScript().getStringValue() : "";
		this.tearDownScript = config.getTearDownScript() != null ? config
				.getTearDownScript().getStringValue() : "";
		scriptEngine = SoapUIScriptEngineRegistry.create(this);
	}

	// TODO check if should exist and what to do with securable
	public AbstractSecurityCheck(SecurityCheckConfig config, ModelItem parent,
			String icon) {
		super(config, parent, icon, null);
		this.config = config;
		this.startupScript = config.getSetupScript() != null ? config
				.getSetupScript().getStringValue() : "";
		this.tearDownScript = config.getTearDownScript() != null ? config
				.getTearDownScript().getStringValue() : "";
		scriptEngine = SoapUIScriptEngineRegistry.create(this);
	}

	abstract protected void execute(TestStep testStep,
			WsdlTestRunContext context, SecurityTestLogModel securityTestLog);

	@Override
	abstract public void analyze(TestStep testStep, WsdlTestRunContext context,
			SecurityTestLogModel securityTestLog);

	@Override
	public void run(TestStep testStep, WsdlTestRunContext context,
			SecurityTestLogModel securityTestLog) {
		runStartupScript(testStep);
		execute(testStep, context, securityTestLog);
		sensitiveInfoCheck(testStep, context, securityTestLog);
		runTearDownScript(testStep);
	}

	private void sensitiveInfoCheck(TestStep testStep,
			WsdlTestRunContext context, SecurityTestLogModel securityTestLog) {
		if (this instanceof SensitiveInformationCheckable) {
			((SensitiveInformationCheckable) this)
					.checkForSensitiveInformationExposure(testStep, context,
							securityTestLog);
		}
	}

	@Override
	public boolean configure() {
		if (dialog == null) {
			buildDialog();
		}

		UISupport.showDialog(dialog);
		return configureResult;
	}

	protected void buildDialog() {
		dialog = new JDialog(UISupport.getMainFrame(), getTitle(), true);
		JPanel fullPanel = new JPanel();
		fullPanel.setPreferredSize(new Dimension(300, 300));
		JPanel contentPanel = (JPanel) getComponent();
		contentPanel.setPreferredSize(new Dimension(300, 200));
		ButtonBarBuilder builder = new ButtonBarBuilder();

		ShowOnlineHelpAction showOnlineHelpAction = new ShowOnlineHelpAction(
				HelpUrls.XPATHASSERTIONEDITOR_HELP_URL);
		builder.addFixed(UISupport.createToolbarButton(showOnlineHelpAction));
		builder.addGlue();

		JButton okButton = new JButton(new OkAction());
		builder.addFixed(okButton);
		builder.addRelatedGap();
		builder.addFixed(new JButton(new CancelAction()));

		builder.setBorder(BorderFactory.createEmptyBorder(1, 5, 5, 5));

		contentPanel.add(builder.getPanel(), BorderLayout.SOUTH);

		JInspectorPanel parameter = JInspectorPanelFactory.build(
				getParameterSelector(), SwingConstants.BOTTOM);

		JSplitPane splitPane = UISupport.createVerticalSplit(new JScrollPane(
				contentPanel), new JScrollPane(parameter.getComponent()));

		fullPanel.add(splitPane, BorderLayout.CENTER);

		dialog.setContentPane(splitPane);
		dialog.setModal(true);
		dialog.pack();
		UISupport.initDialogActions(dialog, showOnlineHelpAction, okButton);

	}

	private JComponent getParameterSelector() {
		parameterSelector = new JPanel(new BorderLayout());
		parameterSelector.setPreferredSize(new Dimension(300, 100));
		parameterSelector.setLayout(new BoxLayout(parameterSelector,
				BoxLayout.Y_AXIS));

		AbstractHttpRequest<?> request = null;
		if (getTestStep() instanceof HttpTestRequestStep) {
			request = ((HttpTestRequestStep) getTestStep()).getHttpRequest();
		} else if (getTestStep() instanceof RestTestRequestStep) {
			request = ((RestTestRequestStep) getTestStep()).getHttpRequest();
		} else if (getTestStep() instanceof WsdlTestRequestStep) {
			request = ((WsdlTestRequestStep) getTestStep()).getTestRequest();
		}

		parameterSelector.add(new JLabel("Select the Parameters that this test will apply to"));
		if (request != null) {
			for (String param : request.getParams().keySet()) {
				ParamPanel paramPanel = new ParamPanel(param, getParamsToCheck().contains(param));
				parameterSelector.add(paramPanel, BorderLayout.WEST);
			}
		}

		parameterSelector.add(Box.createVerticalGlue());
		return parameterSelector;
	}

	public class OkAction extends AbstractAction {
		public OkAction() {
			// TODO save the config
			super("Save");
			configureResult = true;
		}

		public void actionPerformed(ActionEvent arg0) {
			List<String> params = new ArrayList<String>();
			for (Component comp : parameterSelector.getComponents()) {
				if (comp instanceof ParamPanel) {
					if (((ParamPanel) comp).isSelected()) {
						params.add(((ParamPanel) comp).getParamName());
					}
				}
			}
			setParamsToCheck(params);
			dialog.setVisible(false);
		}
	}

	public class CancelAction extends AbstractAction {
		public CancelAction() {
			super("Cancel");
			configureResult = false;
		}

		public void actionPerformed(ActionEvent arg0) {
			dialog.setVisible(false);
		}
	}

	private void runTearDownScript(TestStep testStep) {
		scriptEngine.setScript(tearDownScript);
		scriptEngine.setVariable("testStep", testStep);
		scriptEngine.setVariable("log", SoapUI.ensureGroovyLog());

		try {
			scriptEngine.run();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			scriptEngine.clearVariables();
		}

	}

	private void runStartupScript(TestStep testStep) {
		scriptEngine.setScript(startupScript);
		scriptEngine.setVariable("testStep", testStep);
		scriptEngine.setVariable("log", SoapUI.ensureGroovyLog());
		// scriptEngine.setVariable( "context", context );

		try {
			scriptEngine.run();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			scriptEngine.clearVariables();
		}
	}

	@Override
	public SecurityCheckConfig getConfig() {
		return config;
	}

	@Override
	public List<? extends ModelItem> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		return config.getDescription();
	}

	@Override
	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		return config.getId();
	}

	@Override
	public String getName() {
		return config.getName();
	}

	@Override
	public void setName(String arg0) {
		config.setName(arg0);
	}

	@Override
	public ModelItem getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;

	}

	@Override
	public String getTitle() {
		return "";
	}

	public static boolean isSecurable(TestStep testStep) {
		if (testStep != null && testStep instanceof HttpTestRequestStep) {
			return true;
		} else {
			return false;
		}
	}

	public List<String> getParamsToCheck() {
		return config.getParamsToCheckList();
	}

	public void setParamsToCheck(List<String> params) {
		config.setParamsToCheckArray(params.toArray(new String[1]));
	}
}
