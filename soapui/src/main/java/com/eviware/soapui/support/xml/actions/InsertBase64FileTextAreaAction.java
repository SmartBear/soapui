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
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * Inserts a file as base64 into a JXmlTextArea at the current cursor position.
 *
 * @author Cory Lewis
 * @author Ole.Matzura
 */

public class InsertBase64FileTextAreaAction extends AbstractAction {
    private final RSyntaxTextArea textArea;
    private String dialogTitle;

    public InsertBase64FileTextAreaAction(RSyntaxTextArea editArea, String dialogTitle) {
        super("Insert file as Base64");

        this.textArea = editArea;
        this.dialogTitle = dialogTitle;
        if (UISupport.isMac()) {
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("meta G"));
        } else {
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("ctrl G"));
        }
    }

    public void actionPerformed(ActionEvent e) {
        File file = UISupport.getFileDialogs().open(this, dialogTitle, null, null, null);
        if (file == null) {
            return;
        }

        try {
            // read file
            byte[] ba = FileUtils.readFileToByteArray(file);

            // convert to base 64
            Base64 b64 = new Base64();
            String hex = new String(b64.encode(ba));
            // insert into text at cursor position
            int pos = textArea.getCaretPosition();
            StringBuffer text = new StringBuffer(textArea.getText());
            text.insert(pos, hex);
            textArea.setText(text.toString());

        } catch (IOException e1) {
            UISupport.showErrorMessage("Error reading from file: " + e1.getMessage());
        }
    }

}
