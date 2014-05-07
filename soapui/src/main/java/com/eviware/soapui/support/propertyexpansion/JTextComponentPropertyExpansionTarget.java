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

package com.eviware.soapui.support.propertyexpansion;

import java.awt.Point;

import javax.swing.text.JTextComponent;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;

public class JTextComponentPropertyExpansionTarget extends AbstractPropertyExpansionTarget {
    private final JTextComponent textField;

    public JTextComponentPropertyExpansionTarget(JTextComponent textField, ModelItem modelItem) {
        super(modelItem);
        this.textField = textField;
    }

    public void insertPropertyExpansion(PropertyExpansion expansion, Point pt) {
        int pos = pt == null ? -1 : textField.viewToModel(pt);
        if (pos == -1) {
            pos = textField.getCaretPosition();
        }

        if (pos == -1 || textField.getSelectionStart() == textField.getSelectionEnd()) {
            textField.setText(expansion.toString());
            textField.requestFocusInWindow();
        } else {
            String text = textField.getText();
            if (textField.getSelectionStart() < textField.getSelectionEnd()) {
                textField.setText(text.substring(0, textField.getSelectionStart()) + expansion
                        + text.substring(textField.getSelectionEnd()));
                textField.setCaretPosition(textField.getSelectionStart());
            } else {
                textField.setText(text.substring(0, pos) + expansion + text.substring(pos));
                textField.setCaretPosition(pos);
            }

            textField.requestFocusInWindow();
        }
    }

    public String getValueForCreation() {
        return textField.getSelectedText() == null ? textField.getText() : textField.getSelectedText();
    }

    public String getNameForCreation() {
        return textField.getName();
    }
}
