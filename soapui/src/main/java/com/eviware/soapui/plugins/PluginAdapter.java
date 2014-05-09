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

    public static final String DEFAULT_VERSION_STRING = "0.1";

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public String getGroupId() {
        return getClass().getPackage().getName();
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public Version getVersion() {
        return Version.fromString(DEFAULT_VERSION_STRING);
    }

    @Override
    public void initialize() {

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
}
