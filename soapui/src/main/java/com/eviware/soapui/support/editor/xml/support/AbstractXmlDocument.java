/*
 * Copyright 2004-2014 SmartBear Software
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

package com.eviware.soapui.support.editor.xml.support;

import com.eviware.soapui.impl.wsdl.submit.transports.http.DocumentContent;
import com.eviware.soapui.support.editor.ContentChangeListener;
import com.eviware.soapui.support.editor.ContentChangeSupport;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;

import javax.annotation.Nonnull;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract base-class for XmlDocument implementations
 *
 * @author ole.matzura
 */

public abstract class AbstractXmlDocument implements XmlDocument {
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private ContentChangeSupport contentChangeSupport = new ContentChangeSupport();

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

    public void addContentChangeListener(ContentChangeListener listener) {
        contentChangeSupport.addContentChangeListener(listener);
    }

    @Override
    public void removeContentChangeListener(ContentChangeListener listener) {
        contentChangeSupport.removeContentChangeListener(listener);
    }

    protected void fireXmlChanged(String oldValue, String newValue) {
        propertyChangeSupport.firePropertyChange(XML_PROPERTY, oldValue, newValue);
    }

    protected void fireContentChanged(DocumentContent oldValue, DocumentContent newValue) {
        contentChangeSupport.fireContentChange(oldValue, newValue);
    }

    public void release() {
    }

    public SchemaTypeSystem getTypeSystem() {
        return XmlBeans.getBuiltinTypeSystem();
    }

    @Override
    public void setXml(DocumentContent documentContent) {
        setXml(documentContent.getContentAsString());
    }

    protected abstract void setXml(String contentAsString);

    @Override
    @Nonnull
    public DocumentContent getDocumentContent() {
        return new DocumentContent(null, getXml());
    }
}
