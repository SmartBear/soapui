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

package com.eviware.soapui.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

public class WeakPropertyChangeListener implements PropertyChangeListener {
    WeakReference<?> listenerRef;
    Object src;

    @SuppressWarnings("unchecked")
    public WeakPropertyChangeListener(PropertyChangeListener listener, Object src) {
        listenerRef = new WeakReference(listener);
        this.src = src;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        PropertyChangeListener listener = (PropertyChangeListener) listenerRef.get();
        if (listener == null) {
            removeListener();
        } else {
            listener.propertyChange(evt);
        }
    }

    private void removeListener() {
        try {
            Method method = src.getClass().getMethod("removePropertyChangeListener",
                    new Class[]{PropertyChangeListener.class});
            method.invoke(src, new Object[]{this});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
