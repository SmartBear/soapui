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

package com.eviware.soapui.impl.rest.panels.method;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.swing.JTableFactory;
import com.eviware.soapui.support.types.StringList;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RestRepresentationsTable extends JPanel implements PropertyChangeListener {
    private RestMethod restMethod;
    private List<RestRepresentation.Type> types;
    private JTable representationsTable;
    private RepresentationsTableModel tableModel;
    private AddRepresentationAction addRepresentationAction;
    private RemoveRepresentationAction removeRepresentationAction;
    private boolean readOnly;

    public RestRepresentationsTable(RestMethod restMethod, RestRepresentation.Type[] types, boolean readOnly) {
        super(new BorderLayout());
        this.restMethod = restMethod;
        this.types = Arrays.asList(types);
        this.readOnly = readOnly;

        tableModel = new RepresentationsTableModel();
        representationsTable = JTableFactory.getInstance().makeJTable(tableModel);
        representationsTable.setRowHeight(18);

        add(buildToolbar(), BorderLayout.NORTH);
        add(new JScrollPane(representationsTable), BorderLayout.CENTER);

        restMethod.addPropertyChangeListener("representations", this);
    }

    protected JXToolBar buildToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();
        if (!readOnly) {
            addRepresentationAction = new AddRepresentationAction();
            toolbar.addFixed(UISupport.createToolbarButton(addRepresentationAction));

            removeRepresentationAction = new RemoveRepresentationAction();
            removeRepresentationAction.setEnabled(false);
            representationsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    removeRepresentationAction.setEnabled(representationsTable.getSelectedRow() != -1);
                }
            });
            toolbar.addFixed(UISupport.createToolbarButton(removeRepresentationAction));
        }

        return toolbar;
    }

    public class RepresentationsTableModel extends AbstractTableModel implements PropertyChangeListener {
        private List<RestRepresentation> data = new ArrayList<RestRepresentation>();

        public RepresentationsTableModel() {
            initData();
        }

        private void initData() {
            if (!data.isEmpty()) {
                release();
                data.clear();
            }

            for (RestRepresentation representation : restMethod.getRepresentations()) {
                if (types.contains(representation.getType())) {
                    representation.addPropertyChangeListener(this);
                    data.add(representation);
                }
            }
        }

        public int getColumnCount() {
            return 4;
        }

        public int getRowCount() {
            return data.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            RestRepresentation representation = data.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    return representation.getType().toString();
                case 1:
                    return representation.getMediaType();
                case 2:
                    return representation.getType().equals(RestRepresentation.Type.REQUEST) ? "n/a" : representation
                            .getStatus().toString();
                case 3:
                    return representation.getElement() == null ? null : representation.getElement().toString();
            }

            return null;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return !readOnly && columnIndex > 0 && columnIndex < 3
                    && !(data.get(rowIndex).getType().equals(RestRepresentation.Type.REQUEST) && columnIndex == 2);
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (readOnly) {
                return;
            }
            RestRepresentation representation = data.get(rowIndex);

            switch (columnIndex) {
                case 1:
                    representation.setMediaType(value == null ? "" : value.toString());
                    break;
                case 2: {
                    if (value == null) {
                        value = "";
                    }

                    String[] items = value.toString().split(" ");
                    List<Integer> status = new ArrayList<Integer>();

                    for (String item : items) {
                        try {
                            if (StringUtils.hasContent(item)) {
                                status.add(Integer.parseInt(item.trim()));
                            }
                        } catch (NumberFormatException e) {
                        }
                    }

                    representation.setStatus(status);
                    break;
                }
            }
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Type";
                case 1:
                    return "Media-Type";
                case 2:
                    return "Status Codes";
                case 3:
                    return "QName";
            }

            return null;
        }

        public void refresh() {
            initData();
            fireTableDataChanged();
        }

        public void propertyChange(PropertyChangeEvent evt) {
            fireTableDataChanged();
        }

        public void release() {
            for (RestRepresentation representation : data) {
                representation.removePropertyChangeListener(this);
            }
        }

        public RestRepresentation getRepresentationAtRow(int rowIndex) {
            return data.get(rowIndex);
        }
    }

    public RestRepresentation getRepresentationAtRow(int rowIndex) {
        return tableModel.getRepresentationAtRow(rowIndex);
    }

    private class AddRepresentationAction extends AbstractAction {
        private AddRepresentationAction() {
            putValue(SMALL_ICON, UISupport.createImageIcon("/add.png"));
            putValue(SHORT_DESCRIPTION, "Adds a new Response Representation to this Method");
        }

        public void actionPerformed(ActionEvent e) {
            String type = types.size() == 1 ? types.get(0).toString() : UISupport.prompt(
                    "Specify type of Representation to add", "Add Representation", new StringList(types).toStringArray());

            if (type != null) {
                restMethod.addNewRepresentation(RestRepresentation.Type.valueOf(type));
            }
        }
    }

    private class RemoveRepresentationAction extends AbstractAction {
        private RemoveRepresentationAction() {
            putValue(SMALL_ICON, UISupport.createImageIcon("/delete.png"));
            putValue(SHORT_DESCRIPTION, "Removes selected Representation from this Method");
        }

        public void actionPerformed(ActionEvent e) {
            if (UISupport.confirm("Remove selected Representation?", "Remove Representation")) {
                restMethod
                        .removeRepresentation(tableModel.getRepresentationAtRow(representationsTable.getSelectedRow()));
            }
        }
    }

    public void propertyChange(PropertyChangeEvent arg0) {
        tableModel.refresh();
    }

    public void release() {
        tableModel.release();
        restMethod.removePropertyChangeListener("representations", this);
    }

    public void refresh() {
        tableModel.refresh();
    }

    public int getSelectedRow() {
        return representationsTable.getSelectedRow();
    }
}
