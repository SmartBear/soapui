/*
 *  soapUI, copyright (C) 2004-2008 eviware.com
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.resolver;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public abstract class ChangeOperationResolver implements Resolver
{

	private boolean resolved = false;
	private WsdlTestStep testStep;
	private WsdlProject project;
	private Operation pickedOperation;

	public ChangeOperationResolver(WsdlTestStep testStep)
	{
		this.testStep = testStep;
		this.project = testStep.getTestCase().getTestSuite().getProject();

	}

	public String getResolvedPath()
	{
		return "";
	}

	public boolean isResolved()
	{
		return resolved;
	}

	public boolean resolve()
	{

		PropertyChangeDialog pDialog = new PropertyChangeDialog("Choose operation");
		pDialog.showAndChoose();
		resolved = update();
		return true;
	}

	public abstract boolean update();

	public String getDescription()
	{
		return "Resolve: Choose another operation";
	}

	@Override
	public String toString()
	{
		return getDescription();
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
			panel.addLabel("Interface:", cc.xy(1, 2));

			List<Interface> ifaces = project.getInterfaceList();
			DefaultComboBoxModel sourceStepComboModel = new DefaultComboBoxModel();
			sourceStepCombo = new JComboBox(sourceStepComboModel);
			sourceStepCombo.setRenderer(new InterfaceComboRenderer());
			for (Interface element : ifaces)
				sourceStepComboModel.addElement(element);

			sourceStepCombo.setSelectedIndex(0);
			panel.add(sourceStepCombo, cc.xyw(3, 2, 3));


			propertiesCombo = new JComboBox(((Interface) sourceStepCombo.getSelectedItem()).getOperationList().toArray());
			propertiesCombo.setRenderer(new OperationComboRender());

			panel.addLabel("Operation:", cc.xy(1, 4));
			panel.add(propertiesCombo, cc.xyw(3, 4, 3));

			panel.add(okBtn, cc.xy(3, 6));
			panel.add(cancelBtn, cc.xy(5, 6));

			sourceStepCombo.addActionListener(new ActionListener()
			{

				public void actionPerformed(ActionEvent e)
				{
					Interface iface = project.getInterfaceByName(((Interface) sourceStepCombo.getSelectedItem()).getName());
					propertiesCombo.removeAllItems();
					if (iface != null)
					{
						propertiesCombo.setEnabled(true);
						for (Operation op : iface.getOperationList())
							propertiesCombo.addItem(op);
					}
					else
					{
						propertiesCombo.setEnabled(false);
					}

				}

			});

			okBtn.addActionListener(new ActionListener()
			{

				public void actionPerformed(ActionEvent e)
				{

					pickedOperation = (Operation) propertiesCombo.getSelectedItem();

					setVisible(false);
				}

			});

			cancelBtn.addActionListener(new ActionListener()
			{

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
	private class InterfaceComboRenderer extends DefaultListCellRenderer
	{
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
		{
			Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value instanceof Interface)
			{
				Interface item = (Interface) value;
				setIcon(item.getIcon());
				setText(item.getName());
			}

			return result;
		}
	}

	@SuppressWarnings("serial")
	private class OperationComboRender extends DefaultListCellRenderer
	{

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus)
		{
			Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value instanceof Operation)
			{
				Operation item = (Operation) value;
				setText(item.getName());
			}

			return result;
		}

	}
	
	public Operation getPickedOperation()
	{
		return pickedOperation;
	}

}
