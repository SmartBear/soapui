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
        PluginConfiguration annotation = getConfigurationAnnotation();
        PluginId id = new PluginId(annotation.groupId(), annotation.name());
        Version version = Version.fromString(annotation.version());
        String infoUrl = annotation.infoUrl();
        return new PluginInfo(id, version, annotation.description(), infoUrl);
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
