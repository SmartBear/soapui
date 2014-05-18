package com.eviware.soapui.actions.plugin;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.UISupport;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
* Created by ole on 19/05/14.
*/
class LoadPluginFromFileAction extends AbstractAction
{
    private PluginManagerAction.PluginsTableModel pluginsTableModel;

    public LoadPluginFromFileAction(PluginManagerAction.PluginsTableModel pluginsTableModel)
    {
        super( "Load Plugin from file...");
        this.pluginsTableModel = pluginsTableModel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        boolean retry = true;
        while( retry ) {
            File file = UISupport.getFileDialogs().open("PluginManager", "Select plugin", "jar", "Plugin File", null);
            if (file != null) {
                try {
                    SoapUI.getSoapUICore().getPluginManager().installPlugin(file);
                    pluginsTableModel.refresh();
                    retry = false;
                } catch (IOException e1) {
                    UISupport.showErrorMessage(e1);
                }
            }
            else retry = false;
        }
    }
}
