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

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractEditorModel implements EditorModel {
    private Set<EditorModelListener> listeners = new HashSet<EditorModelListener>();

    public void addEditorModelListener(EditorModelListener editorModelListener) {
        listeners.add(editorModelListener);
    }

    public void removeEditorModelListener(EditorModelListener editorModelListener) {
        listeners.remove(editorModelListener);
    }

    public void fireEditorTextChanged(String oldText, String newText) {
        for (EditorModelListener listener : listeners) {
            listener.editorTextChanged(oldText, newText);
        }
    }

    public void release() {
        listeners.clear();
    }
}
