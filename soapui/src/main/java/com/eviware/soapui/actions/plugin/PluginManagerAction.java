package com.eviware.soapui.actions.plugin;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.project.SimpleDialog;
import com.eviware.soapui.plugins.Plugin;
import com.eviware.soapui.plugins.PluginInfo;
import com.eviware.soapui.plugins.PluginManager;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ole on 15/05/14.
 */
public class PluginManagerAction extends AbstractAction {

    private SimpleDialog dialog;

    public PluginManagerAction()
    {
        super("Extension Manager");

        putValue(Action.SHORT_DESCRIPTION, "Configures SoapUI Extensions");
        putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("menu alt X"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if( dialog == null )
        {
            dialog = new PluginManagerDialog();
            dialog.setSize( 500, 300 );
        }

        dialog.setVisible(true);
    }

    private static class PluginManagerDialog extends SimpleDialog
    {
        private PluginsTableModel pluginsTableModel = new PluginsTableModel();

        public PluginManagerDialog()
        {
            super( "Extension Manager", "Manage SoapUI Extensions", null, false );
        }

        @Override
        protected Component buildContent() {

            JPanel panel = UISupport.createEmptyPanel( 5, 15, 15, 15 );
            panel.add( BorderLayout.NORTH,
                    UISupport.wrapInEmptyPanel(
                    new JLabel( "Installed Extensions"), BorderFactory.createEmptyBorder( 0, 0, 5, 0 )));
            panel.add( BorderLayout.CENTER, new JScrollPane( new JTable(pluginsTableModel)));
            return panel;
        }

        @Override
        public ActionList buildActions(String url, boolean okAndCancel) {
            ActionList actions = super.buildActions(url, okAndCancel);

            actions.insertAction(new LoadPluginFromFileAction( pluginsTableModel ), 0);
            actions.insertAction(new LoadPluginFromRepositoryAction( pluginsTableModel ), 1);
            actions.insertSeparator( 2 );

            return actions;
        }

        @Override
        protected boolean handleOk() {
            return true;
        }
    }

    public static class PluginsTableModel extends AbstractTableModel
    {
        private List<Plugin> plugins;
        private PluginManager pluginManager;

        public PluginsTableModel()
        {
            pluginManager = SoapUI.getSoapUICore().getPluginManager();
            plugins = new ArrayList<Plugin>(pluginManager.getInstalledPlugins());
        }

        @Override
        public int getRowCount() {

            return plugins.size();
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int column) {
            switch ( column )
            {
                case 0 : return "Name";
                case 1 : return "Version";
                case 2 : return "Description";
            }

            return "";
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
             return String.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {

            PluginInfo plugin = plugins.get(rowIndex).getInfo();
            switch( columnIndex )
            {
                case 0: return plugin.getId().getName();
                case 1: return plugin.getVersion();
                case 2: return plugin.getDescription();
            }

            return null;
        }

        public void refresh() {
            plugins.clear();
            plugins.addAll( pluginManager.getInstalledPlugins());
            fireTableDataChanged();
        }
    }
}
