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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PluginInfo {

    private final PluginId id;
    private final Version version;
    private final String description;
    private final String infoUrl;
    private List<PluginInfo> dependencies = new ArrayList<PluginInfo>();

    public PluginInfo(PluginId id, Version version, String description, String infoUrl) {
        this.id = id;
        this.version = version;
        this.description = description;
        this.infoUrl = infoUrl;
    }

    public PluginId getId() {
        return id;
    }

    public Version getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getInfoUrl() {
        return infoUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != PluginInfo.class) {
            return false;
        }
        PluginInfo other = (PluginInfo)obj;
        return other.id.equals(this.id) && other.version.equals(this.version);
    }

    @Override
    public int hashCode() {
        return 17 * id.hashCode() + version.hashCode();
    }

    public void addDependency(PluginInfo pluginInfo) {
        dependencies.add(pluginInfo);
    }

    public List<PluginInfo> getDependencies() {
        return Collections.unmodifiableList(dependencies);
    }

    public boolean isCompatibleWith(PluginInfo pluginInfo) {
        return id.equals(pluginInfo.id) && version.compareTo(pluginInfo.version) >= 0;
    }
}
