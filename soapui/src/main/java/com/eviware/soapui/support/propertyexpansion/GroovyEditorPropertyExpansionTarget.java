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

import com.eviware.soapui.impl.wsdl.panels.teststeps.support.GroovyEditor;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.support.UISupport;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.text.BadLocationException;
import java.awt.Point;

public class GroovyEditorPropertyExpansionTarget extends AbstractPropertyExpansionTarget {
    private final RSyntaxTextArea textField;

    public GroovyEditorPropertyExpansionTarget(GroovyEditor textField, ModelItem modelItem) {
        super(modelItem);
        this.textField = textField.getEditArea();
    }

    public void insertPropertyExpansion(PropertyExpansion expansion, Point pt) {
        int pos = pt == null ? -1 : textField.viewToModel(pt);
        if (pos == -1) {
            pos = textField.getCaretPosition();
        }

        String name = expansion.getProperty().getName();
        String javaName = createJavaName(name);

        javaName = UISupport.prompt("Specify name of variable for property", "Get Property", javaName);
        if (javaName == null) {
            return;
        }

        String txt = createContextExpansion(javaName, expansion);

        try {
            int line = textField.getLineOfOffset(pos);
            pos = textField.getLineStartOffset(line);

            textField.setCaretPosition(pos);
            textField.insert(txt, pos);
            textField.setSelectionStart(pos);
            textField.setSelectionEnd(pos + txt.length());
            textField.requestFocusInWindow();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private String createJavaName(String name) {
        StringBuffer buf = new StringBuffer();
        for (int c = 0; c < name.length(); c++) {
            char ch = c == 0 ? name.toLowerCase().charAt(c) : name.charAt(c);
            if (buf.length() == 0 && Character.isJavaIdentifierStart(ch)) {
                buf.append(ch);
            } else if (buf.length() > 0 && Character.isJavaIdentifierPart(ch)) {
                buf.append(ch);
            }
        }

        return buf.toString();
    }

    public String getValueForCreation() {
        return textField.getSelectedText();
    }

    public String getNameForCreation() {
        return null;
    }
}
