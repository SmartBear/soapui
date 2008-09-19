/*
 * soapUI, copyright (C) 2004-2008 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.resolver;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;

import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfer;
import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfersTestStep;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class ChooseAnotherPropertySourceResolver implements Resolver
{
	private boolean resolved;
	private PropertyTransfer badTransfer = null;
	private PropertyTransfersTestStep parent = null;
	private ArrayList<Object> sources = new ArrayList<Object>();
	private ArrayList<String[]> properties = new ArrayList<String[]>();

	public ChooseAnotherPropertySourceResolver(PropertyTransfer propertyTransfer, PropertyTransfersTestStep parent)
	{
		this.badTransfer = propertyTransfer;
		this.parent = parent;

		sources.add(PropertyExpansionUtils.getGlobalProperties());
		properties.add(PropertyExpansionUtils.getGlobalProperties().getPropertyNames());
		sources.add(parent.getTestCase().getTestSuite().getProject());
		properties.add(parent.getTestCase().getTestSuite().getProject().getPropertyNames());
		sources.add(parent.getTestCase().getTestSuite());
		properties.add(parent.getTestCase().getTestSuite().getPropertyNames());
		
		sources.add(parent.getTestCase());
		properties.add(parent.getTestCase().getPropertyNames());

//		for( int c = 0; c < parent.getTestCase().getTestStepCount(); c++ )
//		{
//			WsdlTestStep testStep = parent.getTestCase().getTestStepAt( c );
//			if( testStep == parent )
//				continue;
//			
//			sources.add(testStep);
//			properties.add(testStep.getPropertyNames());
//		}
		
	}

	public String getDescription()
	{
		return "Change source property";
	}

	@Override
	public String toString()
	{
		return getDescription();
	}

	public String getResolvedPath()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isResolved()
	{
		return resolved;
	}

	public boolean resolve()
	{

		PropertyChangeDialog propertyChangeDialog = new PropertyChangeDialog("Choose another property");
		propertyChangeDialog.showAndChoose();

		return resolved;
	}

	@SuppressWarnings("serial")
	private class PropertyChangeDialog extends JDialog
	{

		private JComboBox sourceStepCombo;
		private JComboBox propertiesCombo;
		private JButton okBtn = new JButton(" Ok ");
		private JButton cancelBtn = new JButton(" Cancel ");

		public PropertyChangeDialog(String title)
		{
			super(UISupport.getMainFrame(), title, true);
			init();
		}

		private void init()
		{
			FormLayout layout = new FormLayout("right:pref, 4dlu, 30dlu, 5dlu, 30dlu, min ",
			"min, pref, 4dlu, pref, 4dlu, pref, min");
			CellConstraints cc = new CellConstraints();
			PanelBuilder panel = new PanelBuilder(layout);
			panel.addLabel("Source:", cc.xy(1, 2));
			DefaultComboBoxModel sourceStepComboModel = new DefaultComboBoxModel();
			sourceStepCombo = new JComboBox(sourceStepComboModel);
			sourceStepCombo.setRenderer(new StepComboRenderer());
			for (Object element : sources)
				sourceStepComboModel.addElement(element);

			sourceStepCombo.setSelectedIndex(0);
			panel.add(sourceStepCombo, cc.xyw(3, 2, 3));

			int index = sourceStepCombo.getSelectedIndex();

			propertiesCombo = new JComboBox(properties.get(index));
			panel.addLabel("Property:", cc.xy(1, 4));
			panel.add(propertiesCombo, cc.xyw(3, 4, 3));

			panel.add(okBtn, cc.xy(3, 6));
			panel.add(cancelBtn, cc.xy(5, 6));

			sourceStepCombo.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					int index = sourceStepCombo.getSelectedIndex();
					propertiesCombo.removeAllItems();
					if (properties.get(index).length > 0)
					{
						propertiesCombo.setEnabled(true);
						for (String str : properties.get(index))
							propertiesCombo.addItem(str);
					} else {
						propertiesCombo.setEnabled(false);
					}

				}

			});

			okBtn.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{

					String name;
					TestPropertyHolder sourceStep = (TestPropertyHolder) sourceStepCombo.getSelectedItem();
					if (sourceStep == PropertyExpansionUtils.getGlobalProperties())
						name = PropertyExpansion.GLOBAL_REFERENCE;
					else if (sourceStep == parent.getTestCase().getTestSuite().getProject())
						name = PropertyExpansion.PROJECT_REFERENCE;
					else if (sourceStep == parent.getTestCase().getTestSuite())
						name = PropertyExpansion.TESTSUITE_REFERENCE;
					else if (sourceStep == parent.getTestCase())
						name = PropertyExpansion.TESTCASE_REFERENCE;
					else
						name = sourceStep.getModelItem().getName();

					badTransfer.setSourceStepName(name);

					badTransfer.setSourcePropertyName((String) propertiesCombo.getSelectedItem());

					setVisible(false);
				}

			});

			cancelBtn.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent e)
				{
					setVisible(false);
				}

			});
			
			setLocationRelativeTo(UISupport.getParentFrame(this));
			this.add(panel.getPanel());
		}

		public void showAndChoose()
		{
			this.pack();
			this.setVisible(true);
		}
	}

	@SuppressWarnings("serial")
	private class StepComboRenderer extends DefaultListCellRenderer
	{
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
		{
			Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value instanceof TestModelItem)
			{
				TestModelItem item = (TestModelItem) value;
				setIcon(item.getIcon());
				setText(item.getName()); 
			}
			else if (value == PropertyExpansionUtils.getGlobalProperties())
			{
				setText("Global");
			}

			return result;
		}
	}

}
