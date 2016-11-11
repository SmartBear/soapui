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

package com.eviware.soapui.model.support;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;

/**
 * Base-class for ModelItem implementations
 *
 * @author Ole.Matzura
 */

public abstract class AbstractModelItem implements ModelItem {
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        try {
            propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
        } catch (Throwable t) {
            SoapUI.logError(t);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        try {
            propertyChangeSupport.addPropertyChangeListener(listener);
        } catch (Throwable t) {
            SoapUI.logError(t);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        try {
            propertyChangeSupport.removePropertyChangeListener(listener);
        } catch (Throwable t) {
            SoapUI.logError(t);
        }
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        try {
            propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
        } catch (Throwable t) {
            SoapUI.logError(t);
        }
    }

    public void notifyPropertyChanged(String name, Object oldValue, Object newValue) {
        try {
            propertyChangeSupport.firePropertyChange(name, oldValue, newValue);
        } catch (Throwable t) {
            SoapUI.logError(t);
        }
    }

    public void notifyPropertyChanged(String name, String oldValue, String newValue) {
        try {
            propertyChangeSupport.firePropertyChange(name, oldValue, newValue);
        } catch (Throwable t) {
            SoapUI.logError(t);
        }
    }

    public void notifyPropertyChanged(String name, int oldValue, int newValue) {
        try {
            propertyChangeSupport.firePropertyChange(name, oldValue, newValue);
        } catch (Throwable t) {
            SoapUI.logError(t);
        }
    }

    public void notifyPropertyChanged(String name, boolean oldValue, boolean newValue) {
        try {
            propertyChangeSupport.firePropertyChange(name, oldValue, newValue);
        } catch (Throwable t) {
            SoapUI.logError(t);
        }
    }

    public void fireIndexedPropertyChange(String propertyName, int index, boolean oldValue, boolean newValue) {
        try {
            propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
        } catch (Throwable t) {
            SoapUI.logError(t);
        }
    }

    public void fireIndexedPropertyChange(String propertyName, int index, int oldValue, int newValue) {
        try {
            propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
        } catch (Throwable t) {
            SoapUI.logError(t);
        }
    }

    public void fireIndexedPropertyChange(String propertyName, int index, Object oldValue, Object newValue) {
        try {
            propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue);
        } catch (Throwable t) {
            SoapUI.logError(t);
        }
    }

    @SuppressWarnings("unchecked")
    public List<? extends ModelItem> getChildren() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Project getProject() {
        Project project = ModelSupport.getModelItemProject(this);
        if (project == null) {
            throw new UnsupportedOperationException(this + " is not associated with a project");
        }
        return project;
    }
}
