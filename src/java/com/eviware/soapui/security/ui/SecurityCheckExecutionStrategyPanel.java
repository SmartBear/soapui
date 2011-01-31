package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.eviware.soapui.security.check.AbstractSecurityCheck;

public class SecurityCheckExecutionStrategyPanel extends JPanel {
	private JRadioButton separateButton;
	private JRadioButton singleButton;
	
	public SecurityCheckExecutionStrategyPanel(String strategy) {
		super(new BorderLayout());
		setLayout(new BoxLayout(this,
				BoxLayout.Y_AXIS));
		ButtonGroup executionStrategyGroup = new ButtonGroup();
		separateButton = new JRadioButton( AbstractSecurityCheck.SEPARATE_REQUEST_STRATEGY, strategy.equals(AbstractSecurityCheck.SEPARATE_REQUEST_STRATEGY) );
		separateButton.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		singleButton = new JRadioButton( AbstractSecurityCheck.SINGLE_REQUEST_STRATEGY, strategy.equals(AbstractSecurityCheck.SINGLE_REQUEST_STRATEGY) );
		singleButton.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		executionStrategyGroup.add( separateButton );
		executionStrategyGroup.add( singleButton );
		add( separateButton );
		add( singleButton );
	}
	
	public String getExecutionStrategy() {
		if (singleButton.isSelected()) {
			return AbstractSecurityCheck.SINGLE_REQUEST_STRATEGY;
		} else {
			return AbstractSecurityCheck.SEPARATE_REQUEST_STRATEGY;
		}
	}

}
