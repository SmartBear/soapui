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

package com.eviware.soapui.support.factory;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SoapUIFactoriesConfig;
import com.eviware.soapui.config.SoapUIFactoryConfig;
import com.eviware.soapui.config.SoapuiFactoriesDocumentConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SoapUIFactoryRegistry {
    private Map<Class<?>, List<Object>> factories = new HashMap<Class<?>, List<Object>>();
    private Map<Class<?>, SoapUIFactoryConfig> factoryConfigs = new HashMap<Class<?>, SoapUIFactoryConfig>();
    private final static Logger log = LogManager.getLogger(SoapUIFactoryRegistry.class);
    private Set<SoapUIFactoryRegistryListener> listeners = new HashSet<SoapUIFactoryRegistryListener>();

    public SoapUIFactoryRegistry(InputStream config) {
        if (config != null) {
            addConfig(config, getClass().getClassLoader());
        }
    }

    public void addConfig(InputStream config, ClassLoader classLoader) {
        try {
            SoapuiFactoriesDocumentConfig configDocument = SoapuiFactoriesDocumentConfig.Factory.parse(config);
            SoapUIFactoriesConfig soapuiListeners = configDocument.getSoapuiFactories();

            for (SoapUIFactoryConfig factoryConfig : soapuiListeners.getFactoryList()) {
                try {
                    String factoryTypeName = factoryConfig.getFactoryType();
                    String factoryClassName = factoryConfig.getFactoryClass();

                    Class<?> factoryType = Class.forName(factoryTypeName, true, classLoader);
                    Class<?> factoryClass = Class.forName(factoryClassName, true, classLoader);

                    if (!factoryType.isAssignableFrom(factoryClass)) {
                        throw new RuntimeException("Factory class: " + factoryClassName + " must be of type: "
                                + factoryTypeName);
                    }
                    // make sure the class can be instantiated even if factory
                    // will instantiate interfaces only on demand
                    Object obj = factoryClass.newInstance();
                    if (obj instanceof InitializableFactory) {
                        ((InitializableFactory) obj).init(factoryConfig);
                    }

                    getLog().info("Adding factory [" + factoryClass + "]");
                    addFactory(factoryType, obj);
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

    public void addFactory(Class<?> factoryType, Object factory) {
        if (!factories.containsKey(factoryType)) {
            factories.put(factoryType, new ArrayList<Object>());
        }

        factories.get(factoryType).add(factory);

        for( SoapUIFactoryRegistryListener listener : listeners )
            listener.factoryAdded( factoryType, factory );
    }

    public void removeFactory(Class<?> factoryType, Object factory) {
        if (factories.containsKey(factoryType)) {
            factories.get(factoryType).remove(factory);

            for( SoapUIFactoryRegistryListener listener : listeners )
               listener.factoryRemoved( factoryType, factory );
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> List<T> getFactories(Class<T> factoryType) {
        List<T> result = new ArrayList<T>();

        if (factories.containsKey(factoryType)) {
            for( Object obj : factories.get(factoryType))
                result.add((T) obj);
        }

        return result;
    }

    public void addFactoryRegistryListener( SoapUIFactoryRegistryListener listener )
    {
        listeners.add( listener );
    }

    public void removeFactoryRegistryListener( SoapUIFactoryRegistryListener listener )
    {
        listeners.remove( listener );
    }
}
