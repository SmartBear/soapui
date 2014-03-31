package com.eviware.soapui.plugins;

import com.eviware.soapui.support.action.SoapUIAction;

import java.util.Collection;
import java.util.List;

/**
 * Defines the interface of a class that controls the initialization and configuration of a SoapUI plugin.
 * The easiest way to create such a class is to extend PluginConfigurationAdapter. If it is annotated with
 * <code>@PluginEntry</code>, it will be automatically discovered when the plugin JAR is loaded.
 */
public interface PluginConfiguration
{
	/**
	 * Returns a boolean indicating whether this plugin should be activated when loaded. Implementations could
	 * e.g. check whether a certain system property is set to a specific value.
	 * @return <code>true</code> if and only if this plugin should be enabled
	 */
	boolean isActive();

	/**
	 * Returns a String identifying the group to which this plugin belongs. Ideally the group ID should be
	 * in the same format as that of a Maven group ID.
	 * @return a dot-separated String identifier for the group
	 */
	String getGroupId();

	/**
	 * Returns the name of the plugin.
	 * @return a non-null name, which should be unique when combined with the group ID.
	 */
	String getName();

	/**
	 * The version of this plugin.
	 * @return an object that
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
	 * @return an ordered list of listener classes
	 */
	List<Class<?>> getListeners();

	/**
	 * Gets all the SoapUI actions provided by this plugin. Classes can be annotated with ActionConfiguration to provide
	 * additional information. The order of the list controls the order in which actions are added to menus.
	 * @return an ordered list of action classes
	 */
	List<Class<? extends SoapUIAction>> getActions();

	/**
	 * Gets all the factories provided by this plugin. Classes can be annotated with FactoryConfiguration to provide
	 * additional information.
	 * @return as an unordered collection of factory classes
	 */
	Collection<Class<?>> getFactories();

}
