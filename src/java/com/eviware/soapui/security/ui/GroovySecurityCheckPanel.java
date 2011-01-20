package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JTextField;

import com.eviware.soapui.security.check.GroovySecurityCheck;
import com.eviware.soapui.support.components.SimpleForm;

public class GroovySecurityCheckPanel extends SecurityCheckConfigPanel {
	protected static final String SCRIPT_FIELD = "Script";

	private GroovySecurityCheck groovyCheck;

	public GroovySecurityCheckPanel(GroovySecurityCheck secCheck) {
		super(new BorderLayout());

		groovyCheck = secCheck;

		form = new SimpleForm();
		form.addSpace(5);

		// form.setDefaultTextFieldColumns( 50 );

		JTextField scriptTextArea = form.appendTextField(SCRIPT_FIELD,
				"Script to use");
		scriptTextArea.setSize(new Dimension(400, 600));
		scriptTextArea.setText(secCheck.getScript());

		add(form.getPanel());
	}

	@Override
	public void save() {
		String scriptStr = form.getComponentValue(SCRIPT_FIELD);

		groovyCheck.setScript(scriptStr);

	}

}
