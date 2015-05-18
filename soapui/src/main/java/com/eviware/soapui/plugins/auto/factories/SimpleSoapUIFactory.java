package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.plugins.PluginProxies;

public class SimpleSoapUIFactory<T extends Object> extends AbstractSoapUIFactory<T> {
    private Class<T> objectClass;

    protected SimpleSoapUIFactory(Class<?> factoryType, Class<T> objectClass) {
        super(factoryType);
        this.objectClass = objectClass;
    }

    public T create() {
        try {
            T returnValue = createWithInjector(objectClass);
            return PluginProxies.proxyIfApplicable(returnValue);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        return null;
    }
}
