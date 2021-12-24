/*
 * SoapUI, Copyright (C) 2004-2022 SmartBear Software
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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.panels.request.views.json.actions.FormatJsonAction;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseDocument;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpResponseMessageEditor;
import com.eviware.soapui.impl.wsdl.submit.transports.http.HttpResponse;
import com.eviware.soapui.support.JsonUtil;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.actions.FindAndReplaceDialog;
import com.eviware.soapui.support.actions.FindAndReplaceable;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;
import com.eviware.soapui.support.xml.actions.EnableLineNumbersAction;
import com.eviware.soapui.support.xml.actions.GoToLineAction;
import net.sf.json.JSON;
import net.sf.json.JSONException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings("unchecked")
public class JsonResponseView extends AbstractXmlEditorView<HttpResponseDocument> implements PropertyChangeListener {
    private final HttpRequestInterface<?> httpRequest;
    private FindAndReplaceableTextArea contentEditor;
    private RTextScrollPane editorScrollPane;
    private boolean updatingRequest;
    private JPanel panel;
    private FormatJsonAction formatJsonAction;
    private EnableLineNumbersAction enableLineNumbersAction;
    private GoToLineAction goToLineAction;
    private FindAndReplaceDialog findAndReplaceDialog;
    private SaveJsonTextAreaAction saveJsonAction;

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

        contentEditor = new FindAndReplaceableTextArea();
        contentEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        SyntaxEditorUtil.decorateSyntaxArea(contentEditor);

        editorScrollPane = new RTextScrollPane(contentEditor);
        buildPopup(contentEditor.getPopupMenu(), contentEditor);

        HttpResponse response = httpRequest.getResponse();
        if (response != null) {
            setEditorContent(response);
        }

        editorScrollPane.setFoldIndicatorEnabled(true);
        editorScrollPane.setLineNumbersEnabled(true);
        contentPanel.add(editorScrollPane);
        contentEditor.setEditable(false);

        return contentPanel;
    }

    private void buildPopup(JPopupMenu inputPopup, FindAndReplaceableTextArea editArea) {
        formatJsonAction = new FormatJsonAction(editArea);
        findAndReplaceDialog = new FindAndReplaceDialog(editArea);
        enableLineNumbersAction = new EnableLineNumbersAction(editorScrollPane, "Toggle Line Numbers");
        goToLineAction = new GoToLineAction(editArea, "Go To Line");
        saveJsonAction = new SaveJsonTextAreaAction(editArea, "Save");

        inputPopup.add(saveJsonAction);
        inputPopup.addSeparator();
        inputPopup.add(findAndReplaceDialog);
        inputPopup.addSeparator();
        inputPopup.add(goToLineAction);
        inputPopup.add(enableLineNumbersAction);
        inputPopup.addSeparator();
        inputPopup.add(formatJsonAction);
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

    @Override
    public int getSupportScoreForContentType(String contentType ) {
        return JsonUtil.seemsToBeJsonContentType(contentType)? 2 : 0;
    }

    private static class FindAndReplaceableTextArea extends RSyntaxTextArea implements FindAndReplaceable {

        @Override
        public void setSelectedText(String txt) {
            replaceSelection(txt);
        }

        @Override
        public JComponent getEditComponent() {
            return this;
        }
    }

    private final class SaveJsonTextAreaAction extends AbstractAction {
        private final RSyntaxTextArea textArea;
        private String dialogTitle;
        private final Logger log = LogManager.getLogger(SaveJsonTextAreaAction.class);

        public SaveJsonTextAreaAction(RSyntaxTextArea editArea, String dialogTitle) {
            super("Save as...");
            this.textArea = editArea;
            this.dialogTitle = dialogTitle;
            if (UISupport.isMac()) {
                putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("menu S"));
            } else {
                putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("ctrl S"));
            }
        }

        public void actionPerformed(ActionEvent event) {
            File file = UISupport.getFileDialogs().saveAs(this, dialogTitle, ".json", "JSON Files (*.json)", null);
            if (file == null) {
                return;
            }

            FileWriter writer = null;

            try {
                writer = new FileWriter(file);
                writer.write(textArea.getText());
                writer.close();

                log.info("JSON written to [" + file.getAbsolutePath() + "]");
            } catch (IOException e1) {
                UISupport.showErrorMessage("Error saving json to file: " + e1.getMessage());
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        SoapUI.logError(e);
                    }
                }
            }
        }
    }
}
