/*
 * SoapUI, Copyright (C) 2004-2019 SmartBear Software
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
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

/**
 * Formats the XML of a JXmlTextArea
 *
 * @author Ole.Matzura
 */

public class FormatXmlAction extends AbstractAction {
    private final static Logger log = LogManager.getLogger(FormatXmlAction.class);
    private final RSyntaxTextArea textArea;

    public FormatXmlAction(RSyntaxTextArea editArea) {
        this(editArea, "Format XML");
    }

    public FormatXmlAction(RSyntaxTextArea editArea, String title) {
        super(title);
        putValue(Action.SMALL_ICON, UISupport.createImageIcon("/format_request.gif"));
        putValue(Action.SHORT_DESCRIPTION, "Pretty-prints the xml");
        if (UISupport.isMac()) {
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("shift meta F"));
        } else {
            putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("alt F"));
        }
        this.textArea = editArea;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            textArea.setText(XmlUtils.prettyPrintXml(textArea.getText()));
            textArea.setCaretPosition(0);
        } catch (Exception e1) {
            log.error(e1.getMessage());
        }
    }
}
