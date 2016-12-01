/*
 * SoapUI, Copyright (C) 2004-2016 SmartBear Software 
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent 
 * versions of the EUPL (the "Licence"); 
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at: 
 * 
 * http://ec.europa.eu/idabc/eupl 
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is 
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the Licence for the specific language governing permissions and limitations 
 * under the Licence. 
 */

package com.eviware.soapui.support.editor.inspectors.jms.property;

import com.eviware.soapui.impl.wsdl.panels.request.StringToStringMapTableModel;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.swing.JTableFactory;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class JMSPropertyInspector extends AbstractXmlInspector implements PropertyChangeListener {
    private StringToStringMapTableModel headersTableModel;
    private final JMSPropertyInspectorModel model;
    private JTable headersTable;
    private JPanel panel;
    private JButton removeButton;
    public boolean changing;

    protected JMSPropertyInspector(JMSPropertyInspectorModel model) {
        super("JMS Properties (" + (model.getJMSProperties() == null ? "0" : model.getJMSProperties().size()) + ")",
                "Additional JMS Property for this message", true, JMSPropertyInspectorFactory.INSPECTOR_ID);

        this.model = model;

        model.addPropertyChangeListener(this);
        model.setInspector(this);
    }

    public JComponent getComponent() {
        if (panel != null) {
            return panel;
        }

        headersTableModel = new StringToStringMapTableModel(model.getJMSProperties(), "Key", "Value",
                !model.isReadOnly());
        headersTableModel.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent arg0) {
                model.setJMSProperties(headersTableModel.getData());
                setTitle("JMS Property (" + (model.getJMSProperties() == null ? "0" : model.getJMSProperties().size())
                        + ")");
            }
        });

        headersTable = JTableFactory.getInstance().makeJTable(headersTableModel);

        panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(headersTable), BorderLayout.CENTER);

        if (!model.isReadOnly()) {
            headersTable.setSurrendersFocusOnKeystroke(true);
            headersTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

            JXToolBar builder = UISupport.createSmallToolbar();
            builder.addFixed(UISupport.createToolbarButton(new AddAction()));
            removeButton = UISupport.createToolbarButton(new RemoveAction());
            builder.addFixed(removeButton);
            builder.addGlue();
            // builder.addFixed( UISupport.createToolbarButton( new
            // ShowOnlineHelpAction( HelpUrls.HEADERS_HELP_URL ) ) );

            panel.add(builder, BorderLayout.NORTH);

            headersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent e) {
                    removeButton.setEnabled(headersTable.getSelectedRow() != -1);
                }
            });

            if (headersTable.getRowCount() > 0) {
                headersTable.setRowSelectionInterval(0, 0);
            } else {
                removeButton.setEnabled(false);
            }
        }

        return panel;
    }

    public JTable getHeadersTable() {
        return headersTable;
    }

    @Override
    public void release() {
        super.release();
        model.release();
        model.removePropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (!changing) {
            headersTableModel.setData(model.getJMSProperties());
        }
    }

    private final class RemoveAction extends AbstractAction {
        private RemoveAction() {
            super();
            putValue(AbstractAction.SMALL_ICON, UISupport.createImageIcon("/delete.png"));
            putValue(AbstractAction.SHORT_DESCRIPTION, "Removes the selected custom JMS Property from this message");
        }

        public void actionPerformed(ActionEvent arg0) {
            int row = headersTable.getSelectedRow();
            if (row != -1 && UISupport.confirm("Delete selected JMS Property?", "Remove JMS Property")) {
                changing = true;
                headersTableModel.remove(row);
                changing = false;
            }
        }
    }

    private final class AddAction extends AbstractAction {
        private AddAction() {
            super();
            putValue(AbstractAction.SMALL_ICON, UISupport.createImageIcon("/add.png"));
            putValue(AbstractAction.SHORT_DESCRIPTION, "Adds a custom JMS Property to this message");
        }

        public void actionPerformed(ActionEvent arg0) {
            Object header = UISupport.prompt("Specify name of JMS Property to add", "Add JMS Property", "");
            if (header != null) {
                changing = true;
                headersTableModel.add(header.toString(), "");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        int row = headersTable.getRowCount() - 1;
                        headersTable.scrollRectToVisible(headersTable.getCellRect(row, 1, true));
                        headersTable.setRowSelectionInterval(row, row);

                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                headersTable.editCellAt(headersTable.getRowCount() - 1, 1);
                                headersTable.getEditorComponent().requestFocusInWindow();
                            }
                        });
                    }
                });

                changing = false;
            }
        }
    }

    @Override
    public boolean isEnabledFor(EditorView<XmlDocument> view) {
        return !view.getViewId().equals(RawXmlEditorFactory.VIEW_ID);
    }
}
