package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.plugins.SoapUIFactory;

/**
 * Created by ole on 14/06/14.
 */
public abstract class AbstractSoapUIFactory<T extends Object> implements SoapUIFactory {

    private Class<?> factoryType;

    public T createByReflection(Class<T> clazz) throws IllegalAccessException, InstantiationException {
        return clazz.newInstance();
    }

    protected AbstractSoapUIFactory(Class<?> factoryType) {
        this.factoryType = factoryType;
    }

    @Override
    public Class<?> getFactoryType() {
        return factoryType;
    }
}
