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

import com.eviware.soapui.model.iface.SoapUIListener;
import com.eviware.soapui.support.action.SoapUIAction;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A helper class providing reasonable defaults for all methods in the <code>Plugin</code> interface.
 */
public class PluginAdapter implements Plugin {

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public PluginInfo getInfo() {
        return PluginLoader.readPluginInfoFrom(this.getClass());
    }

    @Override
    public void initialize() {
        if (getConfigurationAnnotation() == null) {
            throw new IllegalStateException("Subclasses of PluginAdapter must be annotated with the @PluginConfigurationAnnotation");
        }
    }

    @Override
    public List<Class<? extends SoapUIListener>> getListeners() {
        return Collections.emptyList();
    }

    @Override
    public List<? extends SoapUIAction> getActions() {
        return Collections.emptyList();
    }

    @Override
    public Collection<? extends ApiImporter> getApiImporters() {
        return Collections.emptySet();
    }

    @Override
    public Collection<? extends SoapUIFactory> getFactories() {
        return Collections.emptySet();
    }

    @Override
    public boolean hasSameIdAs(Plugin otherPlugin) {
        return otherPlugin.getInfo().getId().equals(this.getInfo().getId());
    }

    private PluginConfiguration getConfigurationAnnotation() {
        return getClass().getAnnotation(PluginConfiguration.class);
    }
}
