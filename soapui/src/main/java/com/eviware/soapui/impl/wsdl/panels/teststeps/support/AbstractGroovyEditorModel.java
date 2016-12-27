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

package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.settings.Settings;

import javax.swing.Action;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public abstract class AbstractGroovyEditorModel implements GroovyEditorModel {
    private final String[] keywords;
    private Action runAction;
    private final String name;

    private PropertyChangeSupport propertyChangeSupport;
    private final ModelItem modelItem;

    public AbstractGroovyEditorModel(String[] keywords, ModelItem modelItem, String name) {
        this.keywords = keywords;
        this.modelItem = modelItem;
        this.name = name;

        runAction = createRunAction();

        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public String[] getKeywords() {
        return keywords;
    }

    public Action getRunAction() {
        return runAction;
    }

    public Action createRunAction() {
        return null;
    }

    public abstract String getScript();

    public ModelItem getModelItem() {
        return modelItem;
    }

    public Settings getSettings() {
        return modelItem.getSettings();
    }

    public abstract void setScript(String text);

    public String getScriptName() {
        return name;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void notifyPropertyChanged(String name, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(name, oldValue, newValue);
    }
}
