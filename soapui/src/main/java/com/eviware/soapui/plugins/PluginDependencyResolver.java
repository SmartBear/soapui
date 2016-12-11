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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PluginDependencyResolver {
    private final Map<PluginInfo,File> infoToFileMap;

    public PluginDependencyResolver(PluginLoader pluginLoader, Iterable<File> pluginFiles) throws IOException {
        infoToFileMap = new HashMap<PluginInfo,File>();
        for (File pluginFile : pluginFiles) {
            infoToFileMap.put(pluginLoader.loadPluginInfoFrom(pluginFile, Collections.<JarClassLoader>emptySet()), pluginFile);
        }
    }

    public List<File> determineLoadOrder() throws IOException {
        List<PluginInfo> infoList = new ArrayList<PluginInfo>(infoToFileMap.keySet());
        Collections.sort(infoList, new PluginDependencyComparator());
        List<File> resultList = new ArrayList<File>();
        for (PluginInfo pluginInfo : infoList) {
            resultList.add(infoToFileMap.get(pluginInfo));
        }
        return resultList;
    }

    public Collection<PluginInfo> findAllDependencies(PluginInfo plugin) {
        Set<PluginInfo> allDependencies = new HashSet<PluginInfo>();
        for (PluginInfo dependency : plugin.getDependencies()) {
            PluginInfo realDependency = findDependency(dependency.getId());
            if (realDependency != null) {
                allDependencies.add(realDependency);
                allDependencies.addAll(findAllDependencies(realDependency));
            }
        }
        return allDependencies;
    }

    private PluginInfo findDependency(PluginId id) {
        for (PluginInfo plugin : infoToFileMap.keySet()) {
            if (plugin.getId().equals(id)) {
                return plugin;
            }
        }
        return null;
    }

    public Collection<PluginInfo> findAllDependencies(File pluginFile) {
        for (Map.Entry<PluginInfo, File> entry : infoToFileMap.entrySet()) {
            if (entry.getValue().equals(pluginFile)) {
                return findAllDependencies(entry.getKey());
            }
        }
        return Collections.emptySet();
    }

    public void addPlugin(PluginInfo rootPlugin, File readytest) {
       infoToFileMap.put(rootPlugin, readytest);
    }

    public void removePlugin(PluginInfo pluginId) {
        infoToFileMap.remove(pluginId);
    }

    public List<PluginInfo> getPluginInfoListFromFiles(List<File> files) {
        List<PluginInfo> result = new ArrayList<PluginInfo>();
        for (File file : files) {
            for (Map.Entry<PluginInfo, File> entry : infoToFileMap.entrySet()) {
                if (entry.getValue().equals(file)) {
                    result.add(entry.getKey());
                    break;
                }
            }
        }
        return result;
    }

    private  class PluginDependencyComparator implements Comparator<PluginInfo> {

        @Override
        public int compare(PluginInfo first, PluginInfo second) {
            if (first.getDependencies().isEmpty()) {
                return -1;
            }
            else if (second.getDependencies().isEmpty()) {
                return 1;
            }
            else {
                if (dependsOn(first, second)) {
                    return 1;
                }
                else {
                    if (dependsOn(second, first)) {
                        return -1;
                    }
                    else {
                        return 0;
                    }
                }
            }
        }

        private boolean dependsOn(PluginInfo first, PluginInfo second) {
            return findAllDependencies(first).contains(second);
        }
    }
}
