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
