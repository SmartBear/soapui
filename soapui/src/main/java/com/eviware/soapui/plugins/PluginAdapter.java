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
    public PluginId getId() {
        String groupId = getConfigurationAnnotation().groupId();
        String name = getConfigurationAnnotation().name();
        return new PluginId(groupId, name);
    }

    @Override
    public Version getVersion() {
        return Version.fromString(getConfigurationAnnotation().version());
    }

    @Override
    public void initialize() {
        if (getConfigurationAnnotation() == null) {
            throw new IllegalStateException("All plugin classes must be annotated with the @PluginConfigurationAnnotation");
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

    private PluginConfiguration getConfigurationAnnotation() {
        return getClass().getAnnotation(PluginConfiguration.class);
    }
}
