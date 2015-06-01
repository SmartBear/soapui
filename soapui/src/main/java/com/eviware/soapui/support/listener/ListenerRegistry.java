package com.eviware.soapui.support.listener;

import com.eviware.soapui.config.SoapUIListenerConfig;

import java.util.Collection;
import java.util.List;

public interface ListenerRegistry {
    void addListener(Class<?> listenerInterface, Class<?> listenerClass, SoapUIListenerConfig config);

    void removeListener(Class<?> listenerInterface, Class<?> listenerClass);

    void addSingletonListener(Class<?> listenerInterface, Object listener);

    void removeSingletonListener(Class<?> listenerInterface, Object listener);

    @SuppressWarnings("unchecked")
    <T extends Object> List<T> getListeners(Class<T> listenerType);

    @SuppressWarnings("unchecked")
    <T extends Object> List<T> joinListeners(Class<T> listenerType, Collection<T> existing);
}
