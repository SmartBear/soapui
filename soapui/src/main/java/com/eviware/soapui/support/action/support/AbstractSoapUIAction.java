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

package com.eviware.soapui.support.action.support;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.SoapUIAction;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract SoapUIAction for extension
 *
 * @author ole.matzura
 */

public abstract class AbstractSoapUIAction<T extends ModelItem> implements SoapUIAction<T> {
    private PropertyChangeSupport propertySupport;
    private String name;
    private String description;
    private boolean enabled = true;
    private String id;

    public AbstractSoapUIAction(String id) {
        this.id = id;
        propertySupport = new PropertyChangeSupport(this);
    }

    public AbstractSoapUIAction(String name, String description) {
        this(null, name, description);
        id = getClass().getSimpleName();
    }

    public AbstractSoapUIAction(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;

        propertySupport = new PropertyChangeSupport(this);
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setEnabled(boolean enabled) {
        if (enabled == this.enabled) {
            return;
        }

        boolean oldEnabled = this.enabled;
        this.enabled = enabled;

        propertySupport.firePropertyChange(ENABLED_PROPERTY, oldEnabled, enabled);
    }

    public boolean applies(T target) {
        return true;
    }

    public boolean isDefault() {
        return false;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(propertyName, listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(propertyName, listener);
    }
}
