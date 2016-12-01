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

package com.eviware.soapui.ui.support;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.ui.desktop.DesktopPanel;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation for simple DesktopPanels
 *
 * @author Ole.Matzura
 */

public class DefaultDesktopPanel implements DesktopPanel {
    private PropertyChangeSupport propertyChangeSupport;
    private String title;
    private JComponent component;
    private Set<ModelItem> depends = new HashSet<ModelItem>();
    private ImageIcon icon;
    private final String description;

    public DefaultDesktopPanel(String title, String description, JComponent component) {
        this.title = title;
        this.description = description;
        this.component = component;

        propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public void loadIcon(String path) {
        icon = UISupport.createImageIcon(path);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setTitle(String title) {
        String oldTitle = this.title;
        this.title = title;

        propertyChangeSupport.firePropertyChange(TITLE_PROPERTY, oldTitle, title);
    }

    public ModelItem getModelItem() {
        return null;
    }

    public boolean onClose(boolean canCancel) {
        return true;
    }

    public JComponent getComponent() {
        return component;
    }

    public boolean dependsOn(ModelItem modelItem) {
        return depends != null && depends.contains(modelItem);
    }

    public void addDependency(ModelItem modelItem) {
        depends.add(modelItem);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    public Icon getIcon() {
        return icon;
    }
}
