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
package com.eviware.soapui.support.xml.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import com.eviware.soapui.support.xml.XmlUtils;

public class FormatAction extends AbstractAction {
    private final static Logger log = Logger.getLogger(FormatAction.class);
    private final RSyntaxTextArea textArea;
    private final String language;

    public FormatAction(RSyntaxTextArea textArea, String language) {
        this(textArea, language, "Format " + language);
    }

    public FormatAction(RSyntaxTextArea textArea, String language, String title) {
        super(title);
        this.textArea = textArea;
        this.language = language;
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        try {
            textArea.setText(XmlUtils.prettyPrintXml(textArea.getText()));
            textArea.setCaretPosition(0);
        } catch (Exception e1) {
            log.error(e1.getMessage());
        }
    }
}
