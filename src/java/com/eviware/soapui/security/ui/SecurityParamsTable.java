package com.eviware.soapui.security.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBeans;

import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTableModel;

import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder.ParameterStyle;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.components.StringListFormComponent;
import com.jgoodies.binding.PresentationModel;

public class SecurityParamsTable extends JPanel {
	protected RestParamsPropertyHolder params;
	protected RestParamsPropertyHolder requestParams;
	protected SecurityParamsTableModel paramsTableModel;
	protected JTable paramsTable;
	protected AddParamAction addParamAction = new AddParamAction();
	protected RemoveParamAction removeParamAction = new RemoveParamAction();
	protected ClearParamsAction clearParamsAction = new ClearParamsAction();
	private PresentationModel<RestParamProperty> paramDetailsModel;
	private SimpleBindingForm detailsForm;

	public SecurityParamsTable(RestParamsPropertyHolder params,
			RestParamsPropertyHolder requestParams) {
		super(new BorderLayout());
		this.params = params;
		this.requestParams = requestParams;
		init(params);
	}

	protected SecurityParamsTableModel createTableModel(
			RestParamsPropertyHolder params) {
		return new SecurityParamsTableModel(params);
	}

	protected void init(RestParamsPropertyHolder params) {
		paramsTableModel = createTableModel(params);
		paramsTable = new JTable(paramsTableModel);
		paramsTable.setRowHeight(19);
		paramsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paramsTable.setDefaultEditor(ParameterStyle.class,
				new DefaultCellEditor(new JComboBox(new Object[] {
						ParameterStyle.QUERY, ParameterStyle.TEMPLATE,
						ParameterStyle.HEADER, ParameterStyle.MATRIX,
						ParameterStyle.PLAIN })));

		paramsTable.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					public void valueChanged(ListSelectionEvent e) {
						int selectedRow = paramsTable.getSelectedRow();
						removeParamAction.setEnabled(selectedRow != -1);

						if (selectedRow != -1) {
							RestParamProperty selectedParameter = getSelectedParameter();
							if (paramDetailsModel != null) {
								paramDetailsModel.setBean(selectedParameter);
								detailsForm.setEnabled(true);
							}
						} else {
							if (paramDetailsModel != null) {
								detailsForm.setEnabled(false);
								paramDetailsModel.setBean(null);
							}
						}
					}
				});

		add(buildToolbar(), BorderLayout.NORTH);

		add(new JScrollPane(paramsTable), BorderLayout.CENTER);

	}

	protected RestParamProperty getSelectedParameter() {
		return paramsTable.getSelectedRow() == -1 ? null : paramsTableModel
				.getParameterAt(paramsTable.getSelectedRow());
	}

	public JTable getParamsTable() {
		return paramsTable;
	}

	public void release() {
		paramsTableModel.release();
		if (paramDetailsModel != null)
			paramDetailsModel.setBean(null);
	}

	protected Component buildToolbar() {
		JXToolBar toolbar = UISupport.createToolbar();

		toolbar.add(UISupport.createToolbarButton(addParamAction));
		toolbar.add(UISupport.createToolbarButton(removeParamAction, false));
		toolbar.add(UISupport.createToolbarButton(clearParamsAction,
				paramsTable.getRowCount() > 0));
		toolbar.addSeparator();

		insertAdditionalButtons(toolbar);

		toolbar.addGlue();

		toolbar.add(UISupport.createToolbarButton(new ShowOnlineHelpAction(
				HelpUrls.WADL_PARAMS_HELP_URL)));

		return toolbar;
	}

	protected void insertAdditionalButtons(JXToolBar toolbar) {
	}

	private class AddParamAction extends AbstractAction {
		public AddParamAction() {
			putValue(Action.SMALL_ICON, UISupport
					.createImageIcon("/add_property.gif"));
			putValue(Action.SHORT_DESCRIPTION,
					"Adds a parameter to the security check");
		}

		public void actionPerformed(ActionEvent e) {
			if (requestParams.size() < 1) {
				UISupport.showErrorMessage("The Current Request has no Parameters");
			}
			String[] requestParamsArray = requestParams.keySet().toArray(
					new String[0]);
			String name = UISupport.prompt("Choose parameter to check",
					"Add Parameter", requestParamsArray);
			if (StringUtils.hasContent(name)) {
				params.addParameter(requestParams.get(name));
				final int row = params.getPropertyNames().length - 1;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						requestFocusInWindow();
						scrollRectToVisible(paramsTable.getCellRect(row, 1,
								true));
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								paramsTable.editCellAt(row, 1);
								paramsTable.getEditorComponent()
										.requestFocusInWindow();
							}
						});
					}
				});

				clearParamsAction.setEnabled(true);
			}
		}
	}

	private class RemoveParamAction extends AbstractAction {
		public RemoveParamAction() {
			putValue(Action.SMALL_ICON, UISupport
					.createImageIcon("/remove_property.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Removes the selected parameter");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			int row = paramsTable.getSelectedRow();
			if (row == -1)
				return;

			UISupport.stopCellEditing(paramsTable);

			String propertyName = paramsTableModel.getValueAt(row, 0)
					.toString();
			if (UISupport.confirm("Remove parameter [" + propertyName + "]?",
					"Remove Parameter")) {
				paramsTable.clearSelection();
				params.removeProperty(propertyName);
				clearParamsAction.setEnabled(params.getPropertyCount() > 0);
			}
		}
	}

	private class ClearParamsAction extends AbstractAction {
		public ClearParamsAction() {
			putValue(Action.SMALL_ICON, UISupport
					.createImageIcon("/clear_properties.gif"));
			putValue(Action.SHORT_DESCRIPTION,
					"Clears all current parameter values");
		}

		public void actionPerformed(ActionEvent e) {
			if (UISupport.confirm("Clear all parameter values?",
					"Clear Parameters")) {
				params.clear();
			}
		}
	}

	public void setParams(RestParamsPropertyHolder params) {
		this.params = params;
		paramsTableModel.setParams(params);
	}

	public void refresh() {
		paramsTableModel.fireTableDataChanged();
	}

	public RestParamsPropertyHolder getParamsHolder() {
		return params;
	}

}
