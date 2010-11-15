/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.eviware.soapui.impl.wsdl.teststeps.PropertyTransfer;
import com.eviware.soapui.security.check.GroovySecurityCheck;
import com.eviware.soapui.security.check.SecurityCheck;
import com.eviware.soapui.security.monitor.MonitorSecurityTest;
import com.eviware.soapui.security.registry.SecurityCheckFactory;
import com.eviware.soapui.security.registry.SecurityCheckRegistry;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.components.JInspectorPanelFactory;
import com.eviware.soapui.support.components.JXToolBar;

/**
 * 
 * @author dragica.soldo
 * 
 */
public class SecurityTestsMonitorDesktopPanel extends JPanel {
	private final MonitorSecurityTest monitorSecurityTest;
	private DefaultListModel listModel;
	private JEditorPane securityCheckConfigPanel;
	private JList securityChecksList;
	private JButton copyButton;
	private JButton deleteButton;
	private JButton renameButton;
	private JToggleButton disableButton;
	private JInspectorPanel inspectorPanel;
	JSplitPane splitPane;

	public SecurityTestsMonitorDesktopPanel(MonitorSecurityTest securityTest) {
		// super( securityTest );
		this.monitorSecurityTest = securityTest;
		// componentEnabler = new MonitorSecurityCheckEnabler(
		// securityTest.getTestCase() );

		buildUI();

	}

	protected void buildUI() {
		splitPane = UISupport.createHorizontalSplit();

		listModel = new DefaultListModel();

		// for( int c = 0; c < transferStep.getTransferCount(); c++ )
		// {
		// String name = transferStep.getTransferAt( c ).getName();
		// if( transferStep.getTransferAt( c ).isDisabled() )
		// name += " (disabled)";
		//
		// listModel.addElement( name );
		// }

		securityChecksList = new JList(listModel);
		securityChecksList
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		securityChecksList
				.addListSelectionListener(new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent arg0) {
						securityCheckConfigPanel = (JEditorPane) buildConfigPanel();
				}
				});
		// componentEnabler.add( securityTestsList );

		JScrollPane listScrollPane = new JScrollPane(securityChecksList);
		UISupport.addTitledBorder(listScrollPane, "Security Checks");

		JPanel p = new JPanel(new BorderLayout());
		p.add(listScrollPane, BorderLayout.CENTER);
		p.add(createPropertiesToolbar(), BorderLayout.NORTH);

		splitPane.setLeftComponent(p);

		splitPane.setPreferredSize(new Dimension(550, 400));
		// configPanel = ( ( SecurityCheck
		// )securityChecksList.getSelectedValue()
		// ).getComponent();
		securityCheckConfigPanel = (JEditorPane) buildConfigPanel();
		splitPane.setRightComponent(securityCheckConfigPanel);
		splitPane.setResizeWeight(0.1);
		splitPane.setDividerLocation(120);
		inspectorPanel = JInspectorPanelFactory.build(splitPane);
		add(inspectorPanel.getComponent(), BorderLayout.CENTER);

	}

	public SecurityCheck getCurrentSecurityCheck() {
		int ix = securityChecksList.getSelectedIndex();
		return ix == -1 ? null : monitorSecurityTest.getSecurityCheckAt(ix);
	}

	protected JXToolBar createPropertiesToolbar() {
		JXToolBar toolbar = UISupport.createSmallToolbar();
		toolbar.addFixed(UISupport.createToolbarButton(new AddAction()));
		deleteButton = UISupport.createToolbarButton(new DeleteAction());
		deleteButton.setEnabled(false);
		toolbar.addFixed(deleteButton);
		copyButton = UISupport.createToolbarButton(new CopyAction());
		copyButton.setEnabled(false);
		toolbar.addFixed(copyButton);
		renameButton = UISupport.createToolbarButton(new RenameAction());
		renameButton.setEnabled(false);
		toolbar.addFixed(renameButton);

		disableButton = new JToggleButton(new DisableAction());
		disableButton.setPreferredSize(UISupport.TOOLBAR_BUTTON_DIMENSION);
		disableButton.setSelectedIcon(UISupport
				.createImageIcon("/bullet_red.png"));
		toolbar.addSeparator();
		toolbar.addFixed(disableButton);

		return toolbar;
	}

	private JComponent buildConfigPanel() {
		// securityCheckConfigPanel = UISupport.addTitledBorder( new JPanel( new
		// BorderLayout() ), "Configuration" );
		// securityCheckConfigPanel.setPreferredSize( new Dimension( 330, 400 )
		// );
		securityCheckConfigPanel = new JEditorPane();
		securityCheckConfigPanel.setText("currently no security checks");
		// panel.add( securityCheckConfigPanel );
		if (securityChecksList != null
				&& securityChecksList.getSelectedValue() != null) {
			SecurityCheck selected = monitorSecurityTest
					.getSecurityCheckByName((String) securityChecksList
							.getSelectedValue());
			securityCheckConfigPanel.removeAll();
			securityCheckConfigPanel.add(selected.getComponent());
		}
		securityCheckConfigPanel.revalidate();
		return securityCheckConfigPanel;
	}

	/**
	 * Listen to selection changes in transfer list and update controls
	 * accordingly
	 */

	private final class SecurityCheckListSelectionListener implements
			ListSelectionListener {
		private SecurityCheck securityCheck;

		public void valueChanged(ListSelectionEvent e) {

			if (securityCheck != null) {
				// securityCheck.removePropertyChangeListener(
				// transferPropertyChangeListener );
			}

			securityCheck = getCurrentSecurityCheck();
			securityCheckConfigPanel = (JEditorPane) securityCheck
					.getComponent();
		}
	}

	private final class AddAction extends AbstractAction {
		public AddAction() {
			putValue(Action.SHORT_DESCRIPTION, "Adds a new Property Transfer");
			putValue(Action.SMALL_ICON, UISupport
					.createImageIcon("/add_property.gif"));
		}

		public void actionPerformed(ActionEvent e) {
			String[] availableChecksNames = SecurityCheckRegistry.getInstance()
					.getAvailableSecurityChecksNames();
			String name = UISupport.prompt("Specify name for security check",
					"Add SecurityCheck", availableChecksNames);
			if (name == null || name.trim().length() == 0)
				return;

			monitorSecurityTest.addSecurityCheck(name, name);

			listModel.addElement(name);
			securityChecksList.setSelectedIndex(listModel.getSize() - 1);
		}
	}

	private final class CopyAction extends AbstractAction {
		public CopyAction() {
			putValue(Action.SHORT_DESCRIPTION,
					"Copies the selected Property Transfer");
			putValue(Action.SMALL_ICON, UISupport
					.createImageIcon("/clone_request.gif"));
		}

		public void actionPerformed(ActionEvent e) {
			int ix = securityChecksList.getSelectedIndex();
			SecurityCheck config = monitorSecurityTest.getSecurityCheckAt(ix);

			String name = UISupport.prompt("Specify name for SecurityCheck",
					"Copy SecurityCheck", config.getName());
			if (name == null || name.trim().length() == 0)
				return;

			SecurityCheck securityCheck = monitorSecurityTest.addSecurityCheck(
					name, name);
			securityCheck.setDisabled(config.isDisabled());

			listModel.addElement(name);
			securityChecksList.setSelectedIndex(listModel.getSize() - 1);
		}
	}

	private final class DeleteAction extends AbstractAction {
		public DeleteAction() {
			putValue(Action.SMALL_ICON, UISupport
					.createImageIcon("/remove_property.gif"));
			putValue(Action.SHORT_DESCRIPTION,
					"Deletes the selected Property Transfer");
		}

		public void actionPerformed(ActionEvent e) {
			if (UISupport
					.confirm("Delete selected transfer", "Delete Transfer")) {
				securityChecksList.setSelectedIndex(-1);

				int ix = securityChecksList.getSelectedIndex();
				monitorSecurityTest.removeSecurityCheckAt(ix);
				listModel.remove(ix);

				if (listModel.getSize() > 0) {
					securityChecksList.setSelectedIndex(ix > listModel
							.getSize() - 1 ? listModel.getSize() - 1 : ix);
				}
			}
		}
	}

	private final class RenameAction extends AbstractAction {
		public RenameAction() {
			putValue(Action.SMALL_ICON, UISupport
					.createImageIcon("/rename.gif"));
			putValue(Action.SHORT_DESCRIPTION,
					"Renames the selected Property Transfer");
		}

		public void actionPerformed(ActionEvent e) {
			SecurityCheck securityCheck = getCurrentSecurityCheck();

			String newName = UISupport.prompt(
					"Specify new name for security check",
					"Rename SecurityCheck", securityCheck.getName());

			if (newName != null && !securityCheck.getName().equals(newName)) {
				listModel.setElementAt(newName, securityChecksList
						.getSelectedIndex());
				securityCheck.setName(newName);
			}
		}
	}

	private final class DisableAction extends AbstractAction {
		public DisableAction() {
			putValue(Action.SMALL_ICON, UISupport
					.createImageIcon("/bullet_green.png"));
			putValue(Action.SHORT_DESCRIPTION,
					"Disables the selected Property Transfer");
		}

		public void actionPerformed(ActionEvent e) {
			SecurityCheck securityCheck = getCurrentSecurityCheck();
			securityCheck.setDisabled(disableButton.isSelected());

			String name = securityCheck.getName();
			if (securityCheck.isDisabled())
				name += " (disabled)";

			listModel.setElementAt(name, securityChecksList.getSelectedIndex());
		}
	}

}
