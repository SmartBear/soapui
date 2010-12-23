package com.eviware.soapui.security.check;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.model.testsuite.TestPropertyListener;

public class SecurityCheckParameterSelector extends JPanel implements
		TestPropertyListener {
	
	List<String> paramsToCheck;
	
	public SecurityCheckParameterSelector(AbstractHttpRequest<?> request, List<String> paramsToCheck) {
		super(new BorderLayout());
		request.addTestPropertyListener(this);
		setPreferredSize(new Dimension(300, 100));
		setLayout(new BoxLayout(this,
				BoxLayout.Y_AXIS));

		add(new JLabel("Select the Parameters that this test will apply to"));
		if (request != null) {
			for (String param : request.getParams().keySet()) {
				ParamPanel paramPanel = new ParamPanel(param, paramsToCheck.contains(param));
				paramPanel.setMaximumSize(new Dimension(700, 30));
				add(paramPanel, BorderLayout.WEST);
			}
		}

		add(Box.createVerticalGlue());
	}

	@Override
	public void propertyAdded(String name) {
		ParamPanel paramPanel = new ParamPanel(name, false);
		paramPanel.setMaximumSize(new Dimension(700, 30));
		add(paramPanel, BorderLayout.WEST);
	}

	@Override
	public void propertyMoved(String name, int oldIndex, int newIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void propertyRemoved(String name) {
		for (Component comp : getComponents()) {
			if (comp instanceof ParamPanel) {
				if (((ParamPanel) comp).getParamName().equals(name)) {
					remove(comp);
					break;
				}
			}
		}

	}

	@Override
	public void propertyRenamed(String oldName, String newName) {
		for (Component comp : getComponents()) {
			if (comp instanceof ParamPanel) {
				if (((ParamPanel) comp).getParamName().equals(oldName)) {
					comp.setName(newName);
					break;
				}
			}
		}

	}

	@Override
	public void propertyValueChanged(String name, String oldValue,
			String newValue) {
		// TODO Auto-generated method stub

	}

}
