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

package com.eviware.soapui.support.editor.views.xml.raw;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditor;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public abstract class RawXmlEditor<T extends XmlDocument> extends AbstractXmlEditorView<T> {
    private JTextArea textArea;
    private JScrollPane scrollPane;

    public RawXmlEditor(String title, XmlEditor<T> xmlEditor, String tooltip) {
        super(title, xmlEditor, RawXmlEditorFactory.VIEW_ID);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(SoapUI.getSettings().getBoolean(UISettings.WRAP_RAW_MESSAGES));
        textArea.setToolTipText(tooltip);
        scrollPane = new JScrollPane(textArea);
        UISupport.addPreviewCorner(scrollPane, true);
    }

    public void documentUpdated(){
        textArea.setText(getContent());
        textArea.setLineWrap(SoapUI.getSettings().getBoolean(UISettings.WRAP_RAW_MESSAGES));
        textArea.setCaretPosition(0);
    }

    public abstract String getContent();

    public JComponent getComponent() {
        return scrollPane;
    }

    public boolean isInspectable() {
        return false;
    }

    public boolean saveDocument(boolean validate) {
        return true;
    }

    public void setEditable(boolean enabled) {

    }

}
