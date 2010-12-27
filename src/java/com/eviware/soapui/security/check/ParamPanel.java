package com.eviware.soapui.security.check;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class ParamPanel extends JPanel {

	private JCheckBox checkbox;
	private String name;
	private String xpath;
	
	public ParamPanel(String param, boolean selected) {
		super( new BorderLayout() );
		name = param;

		checkbox = new JCheckBox(param);
		checkbox.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		checkbox.setSelected(selected);
		
		add(checkbox, BorderLayout.WEST);
	}
	
	public ParamPanel(String param, boolean selected, String xpath) {
		super( new BorderLayout() );
		name = param;

		checkbox = new JCheckBox(param);
		checkbox.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		checkbox.setSelected(selected);
		
		this.xpath = xpath;
		
		add(checkbox, BorderLayout.WEST);
	}

	public boolean isSelected() {
		return checkbox.isSelected();
	}

	public String getParamName() {
		if (xpath != null)
			return xpath;
		return name;
	}

}
