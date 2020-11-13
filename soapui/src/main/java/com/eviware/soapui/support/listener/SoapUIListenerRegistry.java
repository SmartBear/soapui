/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.support.listener;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SoapUIListenerConfig;
import com.eviware.soapui.config.SoapUIListenersConfig;
import com.eviware.soapui.config.SoapuiListenersDocumentConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoapUIListenerRegistry implements ListenerRegistry {
    private Map<Class<?>, List<Class<?>>> listeners = new HashMap<Class<?>, List<Class<?>>>();
    private Map<Class<?>, List<Object>> singletonListeners = new HashMap<Class<?>, List<Object>>();
    private Map<Class<?>, SoapUIListenerConfig> listenerConfigs = new HashMap<Class<?>, SoapUIListenerConfig>();
    private final static Logger log = LogManager.getLogger(SoapUIListenerRegistry.class);

    public void addListener(Class<?> listenerInterface, Class<?> listenerClass, SoapUIListenerConfig config) {
        List<Class<?>> classes = null;
        if (listeners.containsKey(listenerInterface)) {
            classes = listeners.get(listenerInterface);
        }
        if (classes == null) {
            classes = new ArrayList<Class<?>>();
        }
        classes.add(listenerClass);
        listeners.put(listenerInterface, classes);

        if (config != null) {
            listenerConfigs.put(listenerClass, config);
        }
    }

    public void removeListener(Class<?> listenerInterface, Class<?> listenerClass) {
        List<Class<?>> classes = null;
        if (listeners.containsKey(listenerInterface)) {
            classes = listeners.get(listenerInterface);
        }
        if (classes != null) {
            classes.remove(listenerClass);
        }
        if (classes == null || classes.size() == 0) {
            listeners.remove(listenerInterface);
        }

        listenerConfigs.remove(listenerClass);
    }

    public SoapUIListenerRegistry(InputStream config) {
        if (config != null) {
            addConfig(config, getClass().getClassLoader());
        }
    }

    public void addConfig(InputStream config, ClassLoader classLoader) {
        try {
            SoapuiListenersDocumentConfig configDocument = SoapuiListenersDocumentConfig.Factory.parse(config);
            SoapUIListenersConfig soapuiListeners = configDocument.getSoapuiListeners();

            for (SoapUIListenerConfig listenerConfig : soapuiListeners.getListenerList()) {
                try {
                    String listenerInterfaceName = listenerConfig.getListenerInterface();
                    String listenerClassName = listenerConfig.getListenerClass();

                    Class<?> listenerInterface = Class.forName(listenerInterfaceName, true, classLoader);
                    Class<?> listenerClass = Class.forName(listenerClassName, true, classLoader);
                    if (!listenerInterface.isInterface()) {
                        throw new RuntimeException("Listener interface: " + listenerInterfaceName + " must be an interface");
                    }
                    if (!listenerInterface.isAssignableFrom(listenerClass)) {
                        throw new RuntimeException("Listener class: " + listenerClassName + " must implement interface: "
                                + listenerInterfaceName);
                    }
                    // make sure the class can be instantiated even if factory
                    // will instantiate interfaces only on demand
                    Object obj = listenerClass.newInstance();
                    if (listenerConfig.getSingleton()) {
                        if (obj instanceof InitializableListener) {
                            ((InitializableListener) obj).init(listenerConfig);
                        }

                        getLog().info("Adding singleton listener [" + listenerClass + "]");
                        addSingletonListener(listenerInterface, obj);
                    } else {
                        // class can be instantiated, register it
                        getLog().info("Adding listener [" + listenerClass + "]");
                        addListener(listenerInterface, listenerClass, listenerConfig);
                    }
                } catch (Exception e) {
                    System.err.println("Error initializing Listener: " + e);
                }
            }
        } catch (Exception e) {
            SoapUI.logError(e);
        } finally {
            try {
                config.close();
            } catch (IOException e) {
                SoapUI.logError(e);
            }
        }
    }

    private Logger getLog() {
        return DefaultSoapUICore.log == null ? log : DefaultSoapUICore.log;
    }

    public void addSingletonListener(Class<?> listenerInterface, Object listener) {
        if (!singletonListeners.containsKey(listenerInterface)) {
            singletonListeners.put(listenerInterface, new ArrayList<Object>());
        }

        singletonListeners.get(listenerInterface).add(listener);
    }

    public void removeSingletonListener(Class<?> listenerInterface, Object listener) {
        if (singletonListeners.containsKey(listenerInterface)) {
            singletonListeners.get(listenerInterface).remove(listener);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> List<T> getListeners(Class<T> listenerType) {
        List<T> result = new ArrayList<T>();
        if (listeners.containsKey(listenerType)) {
            List<Class<?>> list = listeners.get(listenerType);
            for (Class<?> listenerClass : list) {
                try {
                    T listener = (T) listenerClass.newInstance();
                    if (listenerConfigs.containsKey(listenerClass) && listener instanceof InitializableListener) {
                        ((InitializableListener) listener).init(listenerConfigs.get(listenerClass));
                    }

                    result.add(listener);
                } catch (Exception e) {
                    SoapUI.logError(e);
                }
            }
        }

        if (singletonListeners.containsKey(listenerType)) {
            result.addAll((Collection<? extends T>) singletonListeners.get(listenerType));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> List<T> joinListeners(Class<T> listenerType, Collection<T> existing) {
        List<T> result = new ArrayList<T>();
        if (listeners.containsKey(listenerType)) {
            List<Class<?>> list = listeners.get(listenerType);
            for (Class<?> listenerClass : list) {
                try {
                    T listener = (T) listenerClass.newInstance();
                    if (listenerConfigs.containsKey(listenerClass) && listener instanceof InitializableListener) {
                        ((InitializableListener) listener).init(listenerConfigs.get(listenerClass));
                    }

                    result.add(listener);
                } catch (Exception e) {
                    SoapUI.logError(e);
                }
            }
        }

        if (singletonListeners.containsKey(listenerType)) {
            result.addAll((Collection<? extends T>) singletonListeners.get(listenerType));
        }

        result.addAll(existing);

        return result;
    }
}
