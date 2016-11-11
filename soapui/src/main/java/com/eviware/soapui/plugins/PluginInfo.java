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
