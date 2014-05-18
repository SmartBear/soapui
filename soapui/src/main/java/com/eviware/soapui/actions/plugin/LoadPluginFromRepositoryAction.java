package com.eviware.soapui.actions.plugin;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.actions.project.SimpleDialog;
import com.eviware.soapui.impl.wsdl.actions.support.OpenUrlAction;
import com.eviware.soapui.plugins.AvailablePlugin;
import com.eviware.soapui.plugins.Plugin;
import com.eviware.soapui.plugins.PluginInfo;
import com.eviware.soapui.plugins.PluginManager;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.ProgressDialog;
import com.eviware.x.dialogs.Worker;
import com.eviware.x.dialogs.XProgressMonitor;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
* Created by ole on 19/05/14.
*/
class LoadPluginFromRepositoryAction extends AbstractAction
{
    private PluginManagerAction.PluginsTableModel installedPluginsTableModel;
    private SimpleDialog dialog;

    public LoadPluginFromRepositoryAction(PluginManagerAction.PluginsTableModel installedPluginsTableModel)
    {
        super( "Browse Plugin Repository...");
        this.installedPluginsTableModel = installedPluginsTableModel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if( dialog == null )
        {
            dialog = new PluginBrowserDialog();
            dialog.setSize( 500, 300 );
        }

        dialog.setVisible(true);
    }

    private class PluginBrowserDialog extends SimpleDialog
    {
        private PluginsTableModel pluginsTableModel;
        private JTable table;

        public PluginBrowserDialog()
        {
            super( "Plugin Browser", "Browser Available SoapUI Plugins", null, false );
        }

        @Override
        protected Component buildContent() {

            JPanel panel = UISupport.createEmptyPanel(5, 15, 15, 15);
            panel.add( BorderLayout.NORTH,
                    UISupport.wrapInEmptyPanel(
                            new JLabel( "Installed Plugins"), BorderFactory.createEmptyBorder( 0, 0, 5, 0 )));
            pluginsTableModel = new PluginsTableModel();
            table = new JTable(pluginsTableModel);
            table.setDefaultRenderer(
                 URL.class,
                  new UrlTableCellRenderer()
            );
            table.setCellSelectionEnabled( false );
            table.setRowSelectionAllowed( true );
            table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

            table.addMouseListener( new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int row = table.rowAtPoint( e.getPoint() );
                    int column = table.columnAtPoint( e.getPoint() );

                    if( row >= 0 && column == 4 )
                    {
                        URL url = (URL) pluginsTableModel.getValueAt( row, column );
                        if( url != null )
                            Tools.openURL( url.toString() );
                    }
                }
            });


            panel.add( BorderLayout.CENTER, new JScrollPane(table));
            return panel;
        }

        @Override
        protected boolean handleOk() {
            installedPluginsTableModel.refresh();
            return true;
        }

        private class UrlTableCellRenderer implements TableCellRenderer {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if( value == null )
                    return new JPanel();

                JButton button = new JButton( "...");
                button.setSelected(false);
                UISupport.setFixedSize(button, 30, 20 );
                button.setFocusPainted( hasFocus );

                return button;
            }
        }
    }

    private class PluginsTableModel extends AbstractTableModel
    {
        private List<AvailablePlugin> plugins;
        private PluginManager pluginManager;

        public PluginsTableModel()
        {
            pluginManager = SoapUI.getSoapUICore().getPluginManager();
            plugins = pluginManager.getAvailablePlugins();
        }

        @Override
        public int getRowCount() {

            return plugins.size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int column) {
            switch ( column )
            {
                case 0 : return "Installed";
                case 1 : return "Name";
                case 2 : return "Version";
                case 3 : return "Description";
                case 4 : return "More info...";
            }

            return "";
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {

            if( columnIndex == 0 )
                return Boolean.class;
            else if( columnIndex == 4 )
                return URL.class;
            else
                return String.class;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if( columnIndex == 0 )
            {
                boolean value = (Boolean)aValue;
                final AvailablePlugin plugin = plugins.get(rowIndex);

                if( value && UISupport.confirm( "Install [" + plugin.getPluginInfo().getId().getName() + "]?", "Install Plugin"))
                {
                    ProgressDialog progressDialog = new ProgressDialog( "Install Plugin", "Downloading plugin", 0, null, false );
                    progressDialog.setIndeterminate();
                    progressDialog.run( new Worker() {
                        @Override
                        public Object construct(XProgressMonitor monitor) {

                            try {
                                InputStream in = plugin.getUrl().openStream();
                                File tempFile = File.createTempFile( "soapui-plugin", "jar");
                                monitor.setProgress(0, "Downloading...");
                                Tools.readAndWrite(in, 0, new FileOutputStream(tempFile));
                                monitor.setProgress(0, "Installing...");
                                pluginManager.installPlugin( tempFile );
                                installedPluginsTableModel.refresh();
                                UISupport.showInfoMessage("Plugin installed successfully");
                            } catch (IOException e) {
                                UISupport.showErrorMessage( e );
                            }

                            return null;
                        }

                        @Override
                        public void finished() {

                        }

                        @Override
                        public boolean onCancel() {
                            return false;
                        }
                    });

                }
                else if( !value && UISupport.confirm( "Uninstall [" + plugin.getPluginInfo().getId().getName() + "]?", "Uninstall Plugin"))
                {
                    for(Plugin p : pluginManager.getInstalledPlugins())
                    {
                        if( p.getInfo().getId().equals( plugin.getPluginInfo().getId()))
                        {
                            try {
                                pluginManager.uninstallPlugin( p );
                                installedPluginsTableModel.refresh();
                            } catch (IOException e) {
                                UISupport.showErrorMessage( e );
                            }

                            break;
                        }
                    }
                }
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {

            PluginInfo plugin = plugins.get(rowIndex).getPluginInfo();
            boolean installed = false;
            for(Plugin p : pluginManager.getInstalledPlugins())
            {
                installed = p.getInfo().getId().equals( plugin.getId()) | installed;
            }

            switch( columnIndex )
            {
                case 0: return installed;
                case 1: return plugin.getId().getName();
                case 2: return plugin.getVersion();
                case 3: return plugin.getDescription();
                case 4:
                    try {
                        return StringUtils.hasContent(plugin.getInfoUrl()) ? new URL(plugin.getInfoUrl()) : null;
                    } catch (MalformedURLException e) {
                        SoapUI.logError( e );
                    }
            }

            return null;
        }
    }
}
