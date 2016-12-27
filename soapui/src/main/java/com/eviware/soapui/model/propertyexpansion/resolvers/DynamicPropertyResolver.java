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

package com.eviware.soapui.model.propertyexpansion.resolvers;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.resolvers.providers.CurrentStepIndexProvider;
import com.eviware.soapui.model.propertyexpansion.resolvers.providers.CurrentStepRunIndexProvider;
import com.eviware.soapui.model.propertyexpansion.resolvers.providers.ProjectDirProvider;
import com.eviware.soapui.model.propertyexpansion.resolvers.providers.WorkspaceDirProvider;
import com.eviware.soapui.plugins.SoapUIFactory;
import com.eviware.soapui.support.factory.SoapUIFactoryRegistryListener;

import java.util.HashMap;
import java.util.Map;

public class DynamicPropertyResolver implements PropertyResolver, SoapUIFactoryRegistryListener {
    private static Map<String, ValueProvider> providers = new HashMap<String, ValueProvider>();

    static {
        addProvider("projectDir", new ProjectDirProvider());
        addProvider("workspaceDir", new WorkspaceDirProvider());
        addProvider( "currentStepIndex", new CurrentStepIndexProvider() );
        addProvider( "currentStepRunIndex", new CurrentStepRunIndexProvider() );
    }

    public DynamicPropertyResolver() {
        for (ValueProviderFactory obj : SoapUI.getFactoryRegistry().getFactories(ValueProviderFactory.class)) {
            addProvider( obj );
        }

        SoapUI.getFactoryRegistry().addFactoryRegistryListener(this);
    }

    public String resolveProperty(PropertyExpansionContext context, String name, boolean globalOverride) {
        ValueProvider provider = providers.get(name);
        if (provider != null) {
            return provider.getValue(context);
        }

        return null;
    }

    public static void addProvider(String propertyName, ValueProvider provider) {
        providers.put(propertyName, provider);
    }

    public static void addProvider( ValueProviderFactory factory )
    {
        addProvider(factory.getValueId(), factory.createValueProvider());
    }

    public static void removeProvider( ValueProviderFactory factory )
    {
        providers.remove( factory.getValueId() );
    }

    @Override
    public void factoryAdded(Class<?> factoryType, Object factory) {
       if( factoryType.equals(ValueProviderFactory.class))
           addProvider((ValueProviderFactory)factory);
    }

    @Override
    public void factoryRemoved(Class<?> factoryType, Object factory) {
        if( factoryType.equals(ValueProviderFactory.class))
            removeProvider((ValueProviderFactory)factory);
    }

    public static interface ValueProvider {
        String getValue(PropertyExpansionContext context);
    }

    public static interface ValueProviderFactory extends SoapUIFactory {
        public ValueProvider createValueProvider();

        public String getValueId();
    }
}
