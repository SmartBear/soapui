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
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

public class EnableLineNumbersAction extends AbstractAction {
    private final RTextScrollPane editorScrollPane;

    public EnableLineNumbersAction(RTextScrollPane editorScrollPane, String title) {
        super(title);
        this.editorScrollPane = editorScrollPane;
        if (UISupport.isMac()) {
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("ctrl L"));
        } else {
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("alt L"));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editorScrollPane.setLineNumbersEnabled(!editorScrollPane.getLineNumbersEnabled());
    }

}
