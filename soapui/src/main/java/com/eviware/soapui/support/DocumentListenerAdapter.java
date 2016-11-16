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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Adapter for DocumentListener implementations
 *
 * @author Ole.Matzura
 */

public abstract class DocumentListenerAdapter implements DocumentListener {
    public DocumentListenerAdapter() {
    }

    public void insertUpdate(DocumentEvent e) {
        update(e.getDocument());
    }

    public abstract void update(Document document);

    public void removeUpdate(DocumentEvent e) {
        update(e.getDocument());
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public String getText(Document document) {
        try {
            return document.getText(0, document.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
            return "";
        }
    }
}
