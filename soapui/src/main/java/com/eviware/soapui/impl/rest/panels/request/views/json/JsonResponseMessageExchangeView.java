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

package com.eviware.soapui.impl.rest.panels.request.views.json;

import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseDocument;
import com.eviware.soapui.impl.wsdl.support.MessageExchangeModelItem;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.support.JsonUtil;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.editor.xml.XmlEditor;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;
import net.sf.json.JSON;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@SuppressWarnings("unchecked")
public class JsonResponseMessageExchangeView extends AbstractXmlEditorView<HttpResponseDocument> implements
        PropertyChangeListener {
    private final MessageExchangeModelItem messageExchangeModelItem;
    private JPanel contentPanel;
    private RSyntaxTextArea contentEditor;
    private boolean updatingRequest;
    private JPanel panel;

    public JsonResponseMessageExchangeView(XmlEditor editor, MessageExchangeModelItem messageExchangeModelItem) {
        super("JSON", editor, JsonResponseViewFactory.VIEW_ID);
        this.messageExchangeModelItem = messageExchangeModelItem;

        messageExchangeModelItem.addPropertyChangeListener(this);
    }

    public JComponent getComponent() {
        if (panel == null) {
            panel = new JPanel(new BorderLayout());

            panel.add(buildToolbar(), BorderLayout.NORTH);
            panel.add(buildContent(), BorderLayout.CENTER);
            panel.add(buildStatus(), BorderLayout.SOUTH);
        }

        return panel;
    }

    @Override
    public void release() {
        super.release();

        messageExchangeModelItem.removePropertyChangeListener(this);
    }

    private Component buildStatus() {
        return new JPanel();
    }

    private Component buildContent() {
        contentPanel = new JPanel(new BorderLayout());

        contentEditor = SyntaxEditorUtil.createDefaultJsonSyntaxTextArea();
        MessageExchange me = messageExchangeModelItem.getMessageExchange();
        if (me != null) {
            setEditorContent(me);
        }

        RTextScrollPane scrollPane = new RTextScrollPane(contentEditor);
        scrollPane.setLineNumbersEnabled(true);
        scrollPane.setFoldIndicatorEnabled(true);
        contentPanel.add(scrollPane);
        contentEditor.setEditable(false);

        return contentPanel;
    }

    protected void setEditorContent(MessageExchange me) {
        if (me == null) {
            contentEditor.setText("");
        } else {
            String content = "<Not JSON content>";

            if (JsonUtil.seemsToBeJsonContentType(me.getResponseHeaders().get("Content-Type", ""))) {
                try {
                    JSON json = new JsonUtil().parseTrimmedText(me.getResponseContent());
                    if (json.isEmpty()) {
                        content = "<Empty JSON content>";
                    } else {
                        content = json.toString(3);
                    }
                } catch (Throwable e) {
                    if (!"Invalid JSON String".equals(e.getMessage())) {
                        e.printStackTrace();
                    } else {
                        content = me.getResponseContent();
                    }
                }

                contentEditor.setText(content);
            } else {
                contentEditor.setText("<Not JSON content>");
            }
        }
    }

    private Component buildToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();

        return toolbar;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("messageExchange") && !updatingRequest) {
            setEditorContent(((MessageExchange) evt.getNewValue()));
        }
    }

    public boolean saveDocument(boolean validate) {
        return false;
    }

    public void setEditable(boolean enabled) {
    }
}
