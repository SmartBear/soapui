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

import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseDocument;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseMessageEditor;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.support.JsonUtil;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;
import net.sf.json.JSON;
import net.sf.json.JSONException;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

@SuppressWarnings("unchecked")
public class JsonResponseView extends AbstractXmlEditorView<HttpResponseDocument> implements PropertyChangeListener {
    private final HttpRequestInterface<?> httpRequest;
    private RSyntaxTextArea contentEditor;
    private boolean updatingRequest;
    private JPanel panel;

    public JsonResponseView(HttpResponseMessageEditor httpRequestMessageEditor, HttpRequestInterface<?> httpRequest) {
        super("JSON", httpRequestMessageEditor, JsonResponseViewFactory.VIEW_ID);
        this.httpRequest = httpRequest;

        httpRequest.addPropertyChangeListener(this);
    }

    public JComponent getComponent() {
        if (panel == null) {
            panel = new JPanel(new BorderLayout());

            panel.add(UISupport.createToolbar(), BorderLayout.NORTH);
            panel.add(buildContent(), BorderLayout.CENTER);
            panel.add(buildStatus(), BorderLayout.SOUTH);
        }

        return panel;
    }

    @Override
    public void release() {
        super.release();
        httpRequest.removePropertyChangeListener(this);
    }

    private Component buildStatus() {
        return new JPanel();
    }

    private Component buildContent() {
        JPanel contentPanel = new JPanel(new BorderLayout());

        contentEditor = SyntaxEditorUtil.createDefaultJavaScriptSyntaxTextArea();
        HttpResponse response = httpRequest.getResponse();
        if (response != null) {
            setEditorContent(response);
        }

        RTextScrollPane scrollPane = new RTextScrollPane(contentEditor);
        scrollPane.setFoldIndicatorEnabled(true);
        scrollPane.setLineNumbersEnabled(true);
        contentPanel.add(scrollPane);
        contentEditor.setEditable(false);

        return contentPanel;
    }

    protected void setEditorContent(HttpResponse httpResponse) {
        if (httpResponse == null || httpResponse.getContentAsString() == null) {
            contentEditor.setText("");
        } else {
            String content;

            if (JsonUtil.seemsToBeJsonContentType(httpResponse.getContentType())) {
                try {
                    JSON json = new JsonUtil().parseTrimmedText(httpResponse.getContentAsString());
                    if (json.isEmpty()) {
                        content = "<Empty JSON content>";
                    } else {
                        content = json.toString(3);
                    }
                } catch (JSONException e) {
                    content = httpResponse.getContentAsString();
                }
                contentEditor.setText(content);
            } else {
                contentEditor.setText("The content you are trying to view cannot be viewed as JSON");
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(AbstractHttpRequestInterface.RESPONSE_PROPERTY) && !updatingRequest) {
            updatingRequest = true;
            setEditorContent(((HttpResponse) evt.getNewValue()));
            updatingRequest = false;
        }
    }

    public boolean saveDocument(boolean validate) {
        return false;
    }

    public void setEditable(boolean enabled) {
    }
}
