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

package com.eviware.x.impl.swing;

import com.eviware.soapui.support.components.JUndoableTextArea;
import com.eviware.x.form.XFormTextField;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Color;

public class JTextAreaFormField extends AbstractSwingXFormField<JComponent> implements XFormTextField {
    private JScrollPane scrollPane;

    public JTextAreaFormField() {
        super(new JUndoableTextArea());

        scrollPane = new JScrollPane(super.getComponent());
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

    public JTextArea getTextArea() {
        return (JTextArea) super.getComponent();
    }

    public JComponent getComponent() {
        return scrollPane;
    }

    public void setValue(String value) {
        getTextArea().setText(value);
    }

    public String getValue() {
        return getTextArea().getText();
    }

    public void setWidth(int columns) {
        getTextArea().setColumns(columns);
    }

    @Override
    public boolean isMultiRow() {
        return true;
    }
}
