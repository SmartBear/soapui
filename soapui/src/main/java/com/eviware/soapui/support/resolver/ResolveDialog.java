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

package com.eviware.soapui.support.resolver;

import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.actions.project.SimpleDialog;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext.PathToResolve;
import com.eviware.soapui.support.resolver.ResolveContext.Resolver;
import com.eviware.soapui.support.swing.JTableFactory;
import org.jdesktop.swingx.JXTable;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility for resolving items
 *
 * @author Ole.Matzura
 */

public class ResolveDialog {

    private JDialog dialog;
    private ResolveContextTableModel resolveContextTableModel;
    private boolean showOkMessage;
    private String title;
    private String description;
    private String helpUrl;
    private JXTable table;

    public ResolveDialog(String title, String description, String helpUrl) {
        this.title = title;

        this.description = description;
        this.helpUrl = helpUrl;

    }

    @SuppressWarnings("serial")
    private void buildDialog() {
        dialog = new SimpleDialog(title, description, helpUrl, true) {
            @Override
            protected Component buildContent() {
                JPanel panel = new JPanel(new BorderLayout());
                table = JTableFactory.getInstance().makeJXTable(resolveContextTableModel);
                table.setHorizontalScrollEnabled(true);
                table.setDefaultRenderer(JComboBox.class, new ResolverRenderer());
                table.setDefaultEditor(JComboBox.class, new ResolverEditor());
                table.getColumn(2).setCellRenderer(new PathCellRenderer());
                table.getColumn(3).setWidth(100);
                table.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() > 1) {
                            int ix = table.getSelectedRow();
                            if (ix != -1) {
                                ResolveContext.PathToResolve pathToResolve = resolveContextTableModel.getContext()
                                        .getPathsToResolve().get(ix);

                                if (pathToResolve != null) {
                                    UISupport.selectAndShow(pathToResolve.getOwner());
                                }
                            }
                        }
                    }
                });

                panel.add(new JScrollPane(table), BorderLayout.CENTER);
                panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                return panel;
            }

            /*
             * Change Cancel into Update
             */
            @Override
            protected void modifyButtons() {
                super.modifyButtons();
                Component[] components = buttons.getComponents();
                for (Component component : components) {
                    if (component instanceof JButton) {
                        JButton button = (JButton) component;
                        if (button.getText().equals("Cancel")) {
                            button.setText("Update");
                        }
                    }
                }
            }

            @Override
            protected boolean handleCancel() {
                return handleUpdate();
            }

            @SuppressWarnings("unchecked")
            private boolean handleUpdate() {
                for (PathToResolve otherPath : resolveContextTableModel.getContext().getPathsToResolve()) {
                    if (!otherPath.isResolved()) {
                        otherPath.getOwner().afterLoad();
                        otherPath.getOwner().resolve(resolveContextTableModel.getContext());
                    }
                }

                dialog = null;
                setVisible(false);
                resolve(resolveContextTableModel.getContext().getModelItem());
                return true;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected boolean handleOk() {
                for (PathToResolve path : resolveContextTableModel.getContext().getPathsToResolve()) {
                    if (!path.isResolved()) {
                        if (UISupport.confirm("There are unresolved paths, continue?", "Unresolved paths - Warning")) {
                            return true;
                        }
                        return false;
                    }
                }
                return true;
            }

        };

        dialog.setSize(550, 300);
        dialog.setModal(false);
        dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {

            @SuppressWarnings("unchecked")
            @Override
            public void windowClosing(WindowEvent arg0) {
                for (PathToResolve path : resolveContextTableModel.getContext().getPathsToResolve()) {
                    if (!path.isResolved()) {
                        if (UISupport.confirm("There are unresolved paths, continue?", "Unresolved paths - Warning")) {
                            dialog.setVisible(false);
                        }
                        break;
                    }
                }
            }
        });

    }

    public boolean isShowOkMessage() {
        return showOkMessage;
    }

    public void setShowOkMessage(boolean showOkMessage) {
        this.showOkMessage = showOkMessage;
    }

    public ResolveContext<?> resolve(AbstractWsdlModelItem<?> modelItem) {
        ResolveContext<?> context = new ResolveContext<AbstractWsdlModelItem<?>>(modelItem);
        modelItem.resolve(context);
        if (context.isEmpty()) {
            if (isShowOkMessage()) {
                UISupport.showInfoMessage("No resolve problems found", title);
            }
        } else {
            resolveContextTableModel = new ResolveContextTableModel(context);
            if (dialog == null) {
                buildDialog();
            } else {
                table.setModel(resolveContextTableModel);
            }

            UISupport.centerDialog(dialog);
            dialog.setVisible(true);
        }

        return context;
    }

    @SuppressWarnings("serial")
    private class ResolveContextTableModel extends AbstractTableModel {
        private ResolveContext<?> context;
        private ArrayList<JComboBox> jbcList = new ArrayList<JComboBox>();

        @SuppressWarnings("unchecked")
        public ResolveContextTableModel(ResolveContext<?> context2) {
            context = context2;
            for (PathToResolve path : context.getPathsToResolve()) {
                ArrayList<Object> resolversAndDefaultAction = new ArrayList<Object>();
                resolversAndDefaultAction.add("Choose one...");
                for (Object resolver : path.getResolvers()) {
                    resolversAndDefaultAction.add(resolver);
                }
                JComboBox jbc = new JComboBox(resolversAndDefaultAction.toArray());
                jbcList.add(jbc);
            }

        }

        public JComboBox getResolversAndActions(int row) {
            return jbcList.get(row);
        }

        public int getColumnCount() {
            return 4;
        }

        public void setContext(ResolveContext<?> context) {
            this.context = context;
            fireTableDataChanged();
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Item";
                case 1:
                    return "Description";
                case 2:
                    return "Value";
                case 3:
                    return "Action";
            }

            return super.getColumnName(column);
        }

        @Override
        public Class<?> getColumnClass(int arg0) {
            if (arg0 == 3) {
                return JComboBox.class;
            } else {
                return String.class;
            }
        }

        public int getRowCount() {
            return context.getPathsToResolve().size();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 3;
        }

        @SuppressWarnings("unchecked")
        public Object getValueAt(int arg0, int arg1) {
            PathToResolve ptr = context.getPathsToResolve().get(arg0);
            switch (arg1) {
                case 0:
                    return createItemName(ptr);
                case 1:
                    return ptr.getDescription();
                case 2:
                    return ptr.getPath();

            }

            return null;
        }

        @SuppressWarnings("unchecked")
        private String createItemName(PathToResolve ptr) {
            String name = "";
            ModelItem modelItem = ptr.getOwner();
            try {
                name = modelItem.getName();
            } catch (Exception e) {
                e.getStackTrace();
            }

            while (modelItem.getParent() != null && !(modelItem.getParent() instanceof Project)) {
                modelItem = modelItem.getParent();
                name = modelItem.getName() + " - " + name;
            }

            return name;
        }

        public ResolveContext<?> getContext() {
            return context;
        }

        @SuppressWarnings("unchecked")
        public void setResolver(int pathIndex, Object resolveOrDefaultAction) {
            PathToResolve path = context.getPathsToResolve().get(pathIndex);
            if (resolveOrDefaultAction instanceof Resolver) {
                path.setResolver(resolveOrDefaultAction);
            }

        }
    }

    private class ResolverRenderer implements TableCellRenderer {

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            return ((ResolveContextTableModel) table.getModel()).getResolversAndActions(row);
        }
    }

    @SuppressWarnings("serial")
    private class ResolverEditor extends AbstractCellEditor implements TableCellEditor {
        private JComboBox jbc = new JComboBox();

        @SuppressWarnings("unchecked")
        public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, int row,
                                                     int column) {
            jbc = ((ResolveContextTableModel) table.getModel()).getResolversAndActions(row);
            final PathToResolve path = resolveContextTableModel.getContext().getPathsToResolve().get(row);

            jbc.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    Object key = jbc.getSelectedItem();
                    if (key instanceof Resolver) {
                        path.setResolver(key);
                    }
                    if (path.resolve()) {
                        path.setSolved(true);
                        jbc.addItem("Resolved");
                        jbc.setSelectedIndex(jbc.getItemCount() - 1);

                        // for (int cnt = 0; cnt <
                        // resolveContextTableModel.getContext().getPathsToResolve().size();
                        // cnt++)
                        // {
                        // PathToResolve otherPath =
                        // resolveContextTableModel.getContext().getPathsToResolve().get(cnt);
                        // if (path != otherPath & !otherPath.isResolved())
                        // {
                        // otherPath.getOwner().afterLoad();
                        // otherPath.getOwner().resolve(resolveContextTableModel.getContext());
                        // if (otherPath.isResolved())
                        // {
                        // JComboBox jbcOther = ((ResolveContextTableModel)
                        // table.getModel())
                        // .getResolversAndActions(cnt);
                        // jbcOther.addItem("Resolved");
                        // jbcOther.setSelectedIndex(jbcOther.getItemCount() - 1);
                        // }
                        // }
                        // }
                    }
                }

            });
            return jbc;
        }

        public Object getCellEditorValue() {
            return null;
        }

    }

    @SuppressWarnings("serial")
    private class PathCellRenderer extends DefaultTableCellRenderer {
        private Color greenColor = Color.GREEN.darker().darker();
        private Color redColor = Color.RED.darker().darker();

        @SuppressWarnings("unchecked")
        @Override
        public Component getTableCellRendererComponent(JTable arg0, Object arg1, boolean arg2, boolean arg3, int arg4,
                                                       int arg5) {
            Component comp = super.getTableCellRendererComponent(arg0, arg1, arg2, arg3, arg4, arg5);

            List<? extends PathToResolve> paths = resolveContextTableModel.getContext().getPathsToResolve();
            PathToResolve ptr = arg4 >= paths.size() ? null : paths.get(arg4);
            // boolean resolved = ptr.getResolver() != null &&
            // ptr.getResolver().isResolved();

            if (ptr != null && ptr.isResolved()) {
                comp.setForeground(greenColor);
                setText(ptr.getPath());
            } else {
                comp.setForeground(redColor);
            }

            return comp;
        }
    }

}
