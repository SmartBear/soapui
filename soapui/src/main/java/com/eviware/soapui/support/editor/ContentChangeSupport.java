/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.support.editor;

import com.eviware.soapui.impl.wsdl.submit.transports.http.DocumentContent;

import java.util.ArrayList;
import java.util.List;

public class ContentChangeSupport {

    List<ContentChangeListener> listeners = new ArrayList<ContentChangeListener>();

    public void addContentChangeListener(ContentChangeListener listener) {
        listeners.add(listener);
    }

    public void removeContentChangeListener(ContentChangeListener listener) {
        listeners.remove(listener);
    }

    public void fireContentChange(DocumentContent oldContent, DocumentContent newContent) {
        for (ContentChangeListener listener : listeners) {
            listener.contentChange(new ContentChangeEvent(oldContent, newContent));
        }
    }

    public void clear() {
        listeners.clear();
    }
}
