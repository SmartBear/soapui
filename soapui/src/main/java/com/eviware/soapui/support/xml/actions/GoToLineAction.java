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

package com.eviware.soapui.support.xml.actions;

import com.eviware.soapui.support.UISupport;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

public class GoToLineAction extends AbstractAction {
    private final RSyntaxTextArea editArea;

    public GoToLineAction(RSyntaxTextArea editArea, String title) {
        super(title);
        this.editArea = editArea;
        putValue(Action.SHORT_DESCRIPTION, "Moves the caret to the specified line");
        putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("control G"));
    }

    public void actionPerformed(ActionEvent e) {
        String line = UISupport.prompt("Enter line-number to (1.." + (editArea.getLineCount()) + ")", "Go To Line",
                String.valueOf(editArea.getCaretLineNumber() + 1));

        if (line != null) {
            try {
                int ln = Integer.parseInt(line) - 1;

                if (ln < 0) {
                    ln = 0;
                }

                if (ln >= editArea.getLineCount()) {
                    ln = editArea.getLineCount() - 1;
                }

                editArea.scrollRectToVisible(editArea.modelToView(editArea.getLineStartOffset(ln)));
                editArea.setCaretPosition(editArea.getLineStartOffset(ln));
            } catch (Exception e1) {
            }
        }
    }
}
