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

package com.eviware.soapui.support.propertyexpansion;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.text.BadLocationException;
import java.awt.Point;

public class RSyntaxTextAreaPropertyExpansionTarget extends AbstractPropertyExpansionTarget {

    private final RSyntaxTextArea textField;

    public RSyntaxTextAreaPropertyExpansionTarget(RSyntaxTextArea textField, ModelItem modelItem) {
        super(modelItem);
        this.textField = textField;
    }

    @Override
    public void insertPropertyExpansion(PropertyExpansion expansion, Point pt) {
        int pos = pt == null ? -1 : textField.viewToModel(pt);
        if (pos == -1) {
            pos = textField.getCaretPosition();
        }

        try {
            textField.setText(textField.getText(0, pos) + expansion.toString()
                    + textField.getText(pos, textField.getText().length() - textField.getText(0, pos).length()));
        } catch (BadLocationException e) {
            SoapUI.logError(e, "Unable to insert property expansion");
        }

        if (pos >= 0) {
            textField.setCaretPosition(pos);
            textField.requestFocusInWindow();
        }
    }

    @Override
    public String getValueForCreation() {
        return textField.getSelectedText();
    }

    @Override
    public String getNameForCreation() {
        return textField.getName();
    }
}
