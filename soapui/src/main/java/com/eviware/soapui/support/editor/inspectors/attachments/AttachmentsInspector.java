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

package com.eviware.soapui.support.editor.inspectors.attachments;

import com.eviware.soapui.impl.wsdl.AttachmentContainer;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;

import javax.swing.JComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class AttachmentsInspector extends AbstractXmlInspector implements PropertyChangeListener {
    private AttachmentContainer container;
    private AttachmentsPanel attachmentsPanel;

    public AttachmentsInspector(AttachmentContainer container) {
        super("Attachments (" + container.getAttachmentCount() + ")", "Files attached to this message", true,
                AttachmentsInspectorFactory.INSPECTOR_ID);
        this.container = container;

        container.addAttachmentsChangeListener(this);
    }

    public JComponent getComponent() {
        if (attachmentsPanel == null) {
            attachmentsPanel = new AttachmentsPanel(container);
        }

        return attachmentsPanel;
    }

    @Override
    public void release() {
        super.release();
        attachmentsPanel.release();
        container.removeAttachmentsChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setTitle("Attachments (" + container.getAttachmentCount() + ")");
    }

    @Override
    public boolean isEnabledFor(EditorView<XmlDocument> view) {
        return !view.getViewId().equals(RawXmlEditorFactory.VIEW_ID);
    }
}
