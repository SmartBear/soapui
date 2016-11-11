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

package com.eviware.soapui.support.editor.support;

import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorDocument;
import com.eviware.soapui.support.editor.EditorLocation;
import com.eviware.soapui.support.editor.EditorLocationListener;
import com.eviware.soapui.support.editor.EditorView;

import javax.swing.JComponent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base-class to be extended by XmlViews
 *
 * @author ole.matzura
 */

public abstract class AbstractEditorView<T extends EditorDocument> implements EditorView<T> {
    private String title;
    private boolean isActive;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private T xmlDocument;
    private Set<EditorLocationListener<T>> listeners = new HashSet<EditorLocationListener<T>>();
    private Editor<T> editor;
    private JComponent component;
    private final String viewId;

    public AbstractEditorView(String title, Editor<T> editor, String viewId) {
        super();
        this.title = title;
        this.editor = editor;
        this.viewId = viewId;
    }

    protected PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    public JComponent getComponent() {
        if (component == null) {
            component = buildUI();
        }

        return component;
    }

    public String getViewId() {
        return viewId;
    }

    public void requestFocus() {
        if (component != null) {
            component.requestFocusInWindow();
        }
    }

    public abstract JComponent buildUI();

    public boolean activate(EditorLocation<T> location) {
        isActive = true;
        return true;
    }

    public boolean deactivate() {
        isActive = false;
        return true;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        String oldTitle = this.title;
        this.title = title;

        propertyChangeSupport.firePropertyChange(TITLE_PROPERTY, oldTitle, title);
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

    public T getDocument() {
        return xmlDocument;
    }

    public void setDocument(T xmlDocument) {
        this.xmlDocument = xmlDocument;
    }

    public void release() {
        if (this.xmlDocument != null) {
            this.xmlDocument = null;
        }
    }

    public void addLocationListener(EditorLocationListener<T> listener) {
        listeners.add(listener);
    }

    public void removeLocationListener(EditorLocationListener<T> listener) {
        listeners.remove(listener);
    }

    public void fireLocationChanged(EditorLocation<T> location) {
        for (EditorLocationListener<T> listener : listeners) {
            listener.locationChanged(location);
        }
    }

    public EditorLocation<T> getEditorLocation() {
        return null;
    }

    public void setLocation(EditorLocation<T> location) {
    }

    public void locationChanged(EditorLocation<T> location) {
    }

    public Editor<T> getEditor() {
        return editor;
    }

    public void setEditable(boolean enabled) {
    }
}
