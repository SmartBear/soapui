package com.eviware.soapui.plugins;

import com.eviware.soapui.model.iface.SoapUIListener;
import com.eviware.soapui.support.action.SoapUIAction;

import java.util.Collection;
import java.util.List;

/**
 * Defines the interface of a SoapUI plugin.
 * The easiest way to create such a class is to extend PluginAdapter. A class implementing this interface
 * and annotated with {@link PluginConfiguration} will be automatically discovered when the plugin JAR is loaded.
 */
public interface Plugin {
    /**
     * Returns a boolean indicating whether this plugin should be activated when loaded. Implementations could
     * e.g. check whether a certain system property is set to a specific value.
     *
     * @return <code>true</code> if and only if this plugin should be enabled
     */
    boolean isActive();

    PluginId getId();

    /**
     * The version of this plugin.
     *
     * @return a Version object encapsulating the full version(major, minor and patch level)
     */
    Version getVersion();

    /**
     * This method is invoked by SoapUI when the plugin is loaded and should ensure that the plugin is fully initialized.
     * If the plugin cannot be initialized properly, the method should indicate this by throwing an appropriate
     * RuntimeException.
     */
    void initialize();

    /**
     * Gets all the listeners provided by this plugin. SoapUI will use reflection to ensure that Classes can be annotated
     * with ListenerConfiguration to provide additional information.  The order of the list controls the order in which
     * listeners are invoked.
     *
     * @return an ordered list of listener classes
     */
    List<Class<? extends SoapUIListener>> getListeners();

    /**
     * Gets all the SoapUI actions provided by this plugin. Classes can be annotated with ActionConfiguration to provide
     * additional information. The order of the list controls the order in which actions are added to menus.
     *
     * @return an ordered list of action classes
     */
    List<? extends SoapUIAction> getActions();

    /**
     * Gets all the factories provided by this plugin.
     *
     * @return an unordered collection of factory objects
     */
    Collection<? extends SoapUIFactory> getFactories();

}
