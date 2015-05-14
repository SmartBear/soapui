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

import java.net.URL;

public class AvailablePlugin {

    private final PluginInfo pluginInfo;
    private final URL url;
    private Version installedVersion;
    private String category;

    public AvailablePlugin(PluginInfo pluginInfo, URL url, PluginManager pluginManager, String category) {
        this.pluginInfo = pluginInfo;
        this.url = url;
        this.installedVersion = findInstalledVersion(pluginManager);
        this.category = category == null ? "" : category;
    }

    private Version findInstalledVersion(PluginManager pluginManager) {
        for (Plugin p : pluginManager.getInstalledPlugins()) {
            if (p.getInfo().getId().equals(getPluginInfo().getId())) {
                return p.getInfo().getVersion();
            }
        }
        return null;
    }

    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }

    public URL getUrl() {
        return url;
    }

    public Version getInstalledVersion() {
        return installedVersion;
    }

    public boolean updateAvailable() {
        Version installedVersion = getInstalledVersion();
        return installedVersion == null || installedVersion.compareTo(pluginInfo.getVersion()) < 0;
    }

    public String getCategory() {
        return category;
    }

}
