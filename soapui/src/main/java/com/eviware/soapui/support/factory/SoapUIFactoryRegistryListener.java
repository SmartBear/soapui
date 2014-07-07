package com.eviware.soapui.support.factory;

/**
 * Created by ole on 15/06/14.
 */
public interface SoapUIFactoryRegistryListener {

    public void factoryAdded( Class<?> factoryType, Object factory );

    public void factoryRemoved( Class<?> factoryType, Object factory );
}
