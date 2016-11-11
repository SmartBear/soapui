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

package com.eviware.soapui.support.editor.registry;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistryListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry of registered XmlInspectorFactories
 *
 * @author ole.matzura
 */

public class InspectorRegistry implements SoapUIFactoryRegistryListener {
    private static InspectorRegistry instance;
    private List<InspectorFactory> factories = new ArrayList<>();

    private InspectorRegistry() {

        for (InspectorFactory factory : SoapUI.getFactoryRegistry().getFactories(InspectorFactory.class)) {
            addFactory(factory);
        }

        SoapUI.getFactoryRegistry().addFactoryRegistryListener(this);
    }

    public void addFactory(InspectorFactory factory) {
        for (int c = 0; c < factories.size(); c++) {
            InspectorFactory f = factories.get(c);
            if (f.getInspectorId().equals(factory.getInspectorId())) {
                factories.set(c, factory);
                return;
            }
        }

        factories.add(factory);
    }

    public static final InspectorRegistry getInstance() {
        if (instance == null) {
            instance = new InspectorRegistry();
        }

        return instance;
    }

    public void removeFactory(InspectorFactory factory) {
        factories.remove(factory);
    }

    public InspectorFactory[] getFactories() {
        return factories.toArray(new InspectorFactory[factories.size()]);
    }

    public InspectorFactory[] getFactoriesOfType(Class<?> type) {
        List<InspectorFactory> result = new ArrayList<>();
        for (InspectorFactory factory : factories) {
            if (type.isAssignableFrom(factory.getClass())) {
                result.add(factory);
            }
        }

        return result.toArray(new InspectorFactory[result.size()]);
    }

    @Override
    public void factoryAdded(Class<?> factoryType, Object factory) {
        if (factoryType.isAssignableFrom(InspectorFactory.class)) {
            addFactory((InspectorFactory) factory);
        }
    }

    @Override
    public void factoryRemoved(Class<?> factoryType, Object factory) {
        if (factoryType.isAssignableFrom(InspectorFactory.class)) {
            removeFactory((InspectorFactory) factory);
        }
    }
}
