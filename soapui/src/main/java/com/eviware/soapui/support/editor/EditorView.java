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

package com.eviware.soapui.support.editor;

import com.eviware.soapui.support.PropertyChangeNotifier;

import javax.swing.JComponent;

/**
 * Views available for the XmlDocument of a XmlEditor
 *
 * @author ole.matzura
 */

public interface EditorView<T extends EditorDocument> extends PropertyChangeNotifier, EditorLocationListener<T> {
    public final static String TITLE_PROPERTY = EditorView.class.getName() + "@title";

    public Editor<T> getEditor();

    public String getTitle();

    public JComponent getComponent();

    public boolean deactivate();

    public boolean activate(EditorLocation<T> location);

    public EditorLocation<T> getEditorLocation();

    public void setLocation(EditorLocation<T> location);

    public void setDocument(T document);

    public T getDocument();

    public void addLocationListener(EditorLocationListener<T> listener);

    public void removeLocationListener(EditorLocationListener<T> listener);

    public void release();

    public void setEditable(boolean enabled);

    public String getViewId();

    public void requestFocus();
}
