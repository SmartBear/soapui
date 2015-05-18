package com.eviware.soapui.plugins.auto.factories;

import com.eviware.soapui.plugins.SoapUIFactory;
/*
import com.google.inject.Inject;
import com.google.inject.Injector;
*/

/**
 * Created by ole on 14/06/14.
 */
public abstract class AbstractSoapUIFactory<T extends Object> implements SoapUIFactory {

    private Class<?> factoryType;
    //private Injector injector;

    /*@Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }


    public boolean hasInjector() {
        return injector != null;
    }

    public Injector getInjector() {
        return injector;
    }

    public T createWithInjector(Class<T> clazz) throws IllegalAccessException, InstantiationException {
        return (injector != null) ? injector.getInstance(clazz) : clazz.newInstance();
    }

    protected T injectMembers(T obj) {
        if (hasInjector())
            getInjector().injectMembers(obj);

        return obj;
    }
    */

    public T createWithInjector(Class<T> clazz) throws IllegalAccessException, InstantiationException {
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
