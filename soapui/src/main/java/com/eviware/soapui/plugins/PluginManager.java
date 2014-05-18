/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.plugins;

import com.eviware.soapui.SoapUIExtensionClassLoader;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.action.SoapUIActionRegistry;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistry;
import com.eviware.soapui.support.listener.SoapUIListenerRegistry;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginManager {

    public static Logger log = Logger.getLogger(PluginLoader.class);

    private Map<File, Plugin> installedPlugins = new HashMap<File, Plugin>();
    private List<AvailablePlugin> availablePlugins = new ArrayList<AvailablePlugin>();
    private File pluginDirectory;
    private PluginLoader pluginLoader;

    public PluginManager(SoapUIExtensionClassLoader extensionClassLoader, SoapUIFactoryRegistry factoryRegistry,
                         SoapUIActionRegistry actionRegistry, SoapUIListenerRegistry listenerRegistry) {
        pluginLoader = new PluginLoader(extensionClassLoader, factoryRegistry, actionRegistry, listenerRegistry);
        pluginDirectory = new File(System.getProperty("soapui.home"), "plugins");
    }

    public void loadPlugins() {
        File[] pluginFiles = pluginDirectory.listFiles();
        if (pluginFiles != null) {
            for (File pluginFile : pluginFiles) {
                log.info("Adding plugin from [" + pluginFile.getAbsolutePath() + "]");
                try {
                    Plugin plugin = pluginLoader.loadPluginFrom(pluginFile);
                    if (plugin == null) {
                        pluginLoader.loadOldStylePluginFrom(pluginFile);
                    } else {
                        //TODO: probably check if there is a duplicate in the list, here or elsewhere
                        installedPlugins.put(pluginFile, plugin);
                    }
                } catch (IOException e) {
                    log.warn("Could not load plugin from file [" + pluginFile + "]");
                }
            }
        }
        String availablePluginsUrl = System.getProperty( "soapui.plugins.url", "" );
        try {
            if(StringUtils.hasContent( availablePluginsUrl))
                availablePlugins = pluginLoader.loadAvailablePluginsFrom(new URL(availablePluginsUrl));
        } catch (IOException e) {
            log.warn( "Could not load plugins from [" + availablePluginsUrl + "]" );
        }
    }

    public void installPlugin(File pluginFile) throws IOException {
        Plugin plugin = pluginLoader.loadPluginFrom(pluginFile);
        if (plugin != null) {
            uninstallPlugin(plugin);
            File destinationFile = new File(pluginDirectory, pluginFile.getName());
            FileUtils.copyFile(pluginFile, destinationFile);
            installedPlugins.put(destinationFile, plugin);
        } else {
            throw new InvalidPluginException("Could not load plugin from file [" + pluginFile + "]");
        }
    }

    public void uninstallPlugin(Plugin plugin) throws IOException {
        for (File installedPluginFile : installedPlugins.keySet()) {
            if (installedPlugins.get(installedPluginFile).hasSameIdAs(plugin)) {
                if (!installedPluginFile.delete()) {
                    throw new IOException("Couldn't delete old plugin file " + installedPluginFile);
                }
                installedPlugins.remove(installedPluginFile);
                break;
            }
        }
    }

    public Collection<Plugin> getInstalledPlugins() {
        return Collections.unmodifiableCollection(installedPlugins.values());
    }

    public List<AvailablePlugin> getAvailablePlugins() {
        return Collections.unmodifiableList(availablePlugins);
    }
}
