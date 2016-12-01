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

import com.eviware.soapui.model.settings.Settings;

/**
 * Basic EditorModel for SoapUI editors
 */

public interface EditorModel {
    /**
     * Return the settings to use for storing customizations (line-numbers, etc)
     *
     * @return the settings to use
     */

    public Settings getSettings();

    /**
     * Returns the text to display in the editor
     *
     * @return the text to display in the editor
     */

    public String getEditorText();

    /**
     * Save the text in the editor, usually called when the contents of the
     * editor have changed
     *
     * @param text the editor text to save
     */

    public void setEditorText(String text);

    /**
     * Adds a listener for text changes
     *
     * @param editorModelListener
     */

    public void addEditorModelListener(EditorModelListener editorModelListener);

    /**
     * Removes a listener for text changes
     *
     * @param editorModelListener
     */

    public void removeEditorModelListener(EditorModelListener editorModelListener);

    /**
     * Interface for listeners to editor text changes
     */

    public interface EditorModelListener {
        /**
         * Notification that should be sent by EditorModel to all registered
         * listeners if the text changes by some external method (ie not via
         * EditorModel.setEditorText() )
         *
         * @param oldText the old text value
         * @param newText the new text value
         */

        public void editorTextChanged(String oldText, String newText);
    }
}
