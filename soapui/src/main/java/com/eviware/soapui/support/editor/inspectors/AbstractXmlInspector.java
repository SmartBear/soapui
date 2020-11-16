/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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

package com.eviware.soapui.support.editor.inspectors;

import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.Inspector;
import com.eviware.soapui.support.components.JInspectorPanel;
import com.eviware.soapui.support.editor.Editor;
import com.eviware.soapui.support.editor.EditorLocation;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditor;
import com.eviware.soapui.support.editor.xml.XmlInspector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.ImageIcon;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract base-class to be extended by XmlInspectors
 *
 * @author ole.matzura
 */

public abstract class AbstractXmlInspector implements XmlInspector {
    public static final Logger log = LogManager.getLogger(AbstractXmlInspector.class);

    private final PropertyChangeSupport propertySupport;
    private String title;
    private String description;
    private boolean enabled;
    private XmlEditor editor;
    private final String inspectorId;
    private boolean active;
    private ImageIcon imageIcon;

    protected AbstractXmlInspector(String title, String description, boolean enabled, String inspectorId) {
        this.title = title;
        this.description = description;
        this.enabled = enabled;
        this.inspectorId = inspectorId;

        propertySupport = new PropertyChangeSupport(this);
    }

    public final String getInspectorId() {
        return inspectorId;
    }

    public void deactivate() {
        active = false;
    }

    @Override
    public ImageIcon getIcon() {
        return this.imageIcon;
    }

    public void setIcon(ImageIcon imageIcon) {
        ImageIcon old = this.imageIcon;
        this.imageIcon = imageIcon;
        propertySupport.firePropertyChange(Inspector.ICON_PROPERTY, old, imageIcon);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        String oldDescription = this.description;
        this.description = description;
        propertySupport.firePropertyChange(DESCRIPTION_PROPERTY, oldDescription, description);
    }

    public void setTitle(String title) {
        String oldTitle = this.title;
        this.title = title;
        propertySupport.firePropertyChange(TITLE_PROPERTY, oldTitle, title);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        boolean oldEnabled = this.enabled;
        this.enabled = enabled;
        propertySupport.firePropertyChange(ENABLED_PROPERTY, oldEnabled, enabled);
    }

    public void init(Editor<XmlDocument> editor) {
        this.editor = (XmlEditor) editor;
    }

    public Editor<XmlDocument> getEditor() {
        return (Editor<XmlDocument>) editor;
    }

    public void release() {
    }

    public void activate() {
        active = true;
        getComponent().requestFocusInWindow();
    }

    public boolean isActive() {
        return active;
    }

    public boolean isContentHandler() {
        return false;
    }

    public void locationChanged(EditorLocation<XmlDocument> location) {
    }

    public boolean isEnabledFor(EditorView<XmlDocument> view) {
        return false;
    }

    /**
     * Make this inspector visible in the enclosing inspector panel. Obviously, this will only work if the inspector
     * is in an inspector panel.
     */
    public void showInPanel() {
        JInspectorPanel parentPanel = UISupport.findParentWithClass(getComponent(), JInspectorPanel.class);
        if (parentPanel != null) {
            parentPanel.activate(this);
        } else {
            log.debug("showInPanel() called, but the inspector " + getClass().getSimpleName() + "isn't in a panel");
        }
    }

}
