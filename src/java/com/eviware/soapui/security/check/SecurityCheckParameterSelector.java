package com.eviware.soapui.security.check;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.model.testsuite.TestPropertyListener;

public class SecurityCheckParameterSelector extends JPanel implements
		TestPropertyListener {
	public static final String SINGLE_REQUEST_STRATEGY = "A single request with all the parameters";
	public static final String SEPARATE_REQUEST_STRATEGY =  "Seperate request for each parameter";
	
	private List<String> paramsToCheck;
	private JRadioButton separateButton;
	private JRadioButton singleButton;
	
	public SecurityCheckParameterSelector(AbstractHttpRequest<?> request, List<String> paramsToCheck) {
		super(new BorderLayout());
		request.addTestPropertyListener(this);
		setPreferredSize(new Dimension(300, 300));
		setLayout(new BoxLayout(this,
				BoxLayout.Y_AXIS));

		add(new JLabel("Select the Parameters that this test will apply to"));
		
		// create execution strategy panel
		ButtonGroup executionStrategyGroup = new ButtonGroup();
		separateButton = new JRadioButton( SEPARATE_REQUEST_STRATEGY, true );
		separateButton.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		singleButton = new JRadioButton( SINGLE_REQUEST_STRATEGY );
		singleButton.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
		executionStrategyGroup.add( separateButton );
		executionStrategyGroup.add( singleButton );
		
		JPanel executionPanel = new JPanel( new GridLayout( 2, 1 ) );
		executionPanel.add( separateButton );
		executionPanel.add( singleButton );
		executionPanel.setBorder( BorderFactory.createTitledBorder( "Execution Strategy" ) );
		add(executionPanel);

		if (request != null) {
			for (String param : request.getParams().keySet()) {
				ParamPanel paramPanel = new ParamPanel(param, paramsToCheck.contains(param));
				paramPanel.setMaximumSize(new Dimension(700, 30));
				add(paramPanel, BorderLayout.WEST);
			}
		}

		add(Box.createVerticalGlue());
	}
	
	public String getExecutionStrategy() {
		if (singleButton.isSelected()) {
			return SEPARATE_REQUEST_STRATEGY;
		} else {
			return SINGLE_REQUEST_STRATEGY;
		}
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
