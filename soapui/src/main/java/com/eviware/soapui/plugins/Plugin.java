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
import java.util.List;

/**
 * Defines the interface of a SoapUI plugin.
 * The easiest way to create such a class is to extend PluginAdapter. A class implementing this interface
 * and annotated with {@link PluginConfiguration} will be automatically discovered when the plugin JAR is loaded.
 */
public interface Plugin {

    PluginInfo getInfo();

    /**
     * Returns a boolean indicating whether this plugin should be activated when loaded. Implementations could
     * e.g. check whether a certain system property is set to a specific value.
     *
     * @return <code>true</code> if and only if this plugin should be enabled
     */
    boolean isActive();

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
     * Gets all the API importers provided by this plugin. Classes can be annotated with PluginApiImporter to provide
     * additional information.
     *
     * NOTE: this is currently not used by SoapUI Open Source and is only present to ensure binary compatibility
     * between SoapUI OS and Ready! API.
     *
     * @return a collection of API importer classes
     */
    Collection<? extends ApiImporter> getApiImporters();

    /**
     * Gets all the factories provided by this plugin.
     *
     * @return an unordered collection of factory objects
     */
    Collection<? extends SoapUIFactory> getFactories();

    /**
     * Returns {@code true} if and only if the plugin passed as a parameter is identified as the same plugin,
     * i.e. if the ID of this plugin matches this.
     *
     * @param otherPlugin
     * @return
     */
    boolean hasSameIdAs(Plugin otherPlugin);
}
