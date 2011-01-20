package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.eviware.soapui.support.components.SimpleForm;

public abstract class SecurityCheckConfigPanel extends JPanel {
	protected SimpleForm form;
	
	public SecurityCheckConfigPanel(BorderLayout borderLayout) {
		super(borderLayout);
	}

	public abstract void save();
}
