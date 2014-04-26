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

package com.eviware.x.impl.swing;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.text.Document;

import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.components.JUndoableTextField;
import com.eviware.x.form.XFormTextField;

public class JTextFieldFormField extends AbstractSwingXFormField<JUndoableTextField> implements XFormTextField {
    private boolean updating;
    private String oldValue;

    public JTextFieldFormField() {
        super(new JUndoableTextField());

        getComponent().getDocument().addDocumentListener(new DocumentListenerAdapter() {

            @Override
            public void update(Document document) {
                String text = getComponent().getText();

                if (!updating) {
                    fireValueChanged(text, oldValue);
                }

                oldValue = text;
            }
        });
    }

    public void setRequired(boolean required, String message) {
        super.setRequired(required, message);

        if (required) {
            getComponent().setBorder(
                    BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED),
                            BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        } else {
            getComponent().setBorder(
                    BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY),
                            BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        }
    }

    public void setValue(String value) {
        updating = true;
        oldValue = null;
        getComponent().setText(value);
        updating = false;
    }

    public String getValue() {
        return getComponent().getText();
    }

    public void setWidth(int columns) {
        getComponent().setColumns(columns);
    }
}
