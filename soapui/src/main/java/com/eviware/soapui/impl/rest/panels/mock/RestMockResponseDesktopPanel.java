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

package com.eviware.soapui.impl.rest.panels.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.mock.RestMockResponse;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.panels.mockoperation.MockResponseXmlDocument;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.support.MediaTypeComboBox;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.editor.inspectors.httpheaders.HttpHeadersInspector;
import com.eviware.soapui.support.editor.inspectors.httpheaders.MockResponseHeadersModel;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;
import com.eviware.soapui.ui.support.AbstractMockResponseDesktopPanel;
import org.apache.commons.httpclient.HttpStatus;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Field;
import java.util.Vector;

public class RestMockResponseDesktopPanel extends
        AbstractMockResponseDesktopPanel<RestMockResponse, MockResponse> {

    public RestMockResponseDesktopPanel(MockResponse mockResponse) {
        super((RestMockResponse) mockResponse);

        init(mockResponse);
    }

    public JComponent addTopEditorPanel() {
        JPanel topEditorPanel = new JPanel(new BorderLayout());

        topEditorPanel.add(createHttpStatusPanel(), BorderLayout.NORTH);
        topEditorPanel.add(createHeaderInspector(), BorderLayout.CENTER);

        return topEditorPanel;
    }

    protected Component addBottomEditorPanel(MockResponseMessageEditor responseEditor) {
        JPanel bottomEditorPanel = new JPanel(new BorderLayout());

        bottomEditorPanel.add(createMediaTypeCombo(), BorderLayout.NORTH);
        bottomEditorPanel.add(responseEditor, BorderLayout.CENTER);

        return bottomEditorPanel;
    }

    public boolean hasTopEditorPanel() {
        return true;
    }

    private JComponent createHttpStatusPanel() {
        return createPanelWithLabel("Http Status Code: ", createStatusCodeCombo());
    }

    protected MockResponseMessageEditor buildResponseEditor() {
        MockResponseXmlDocument documentContent = new MockResponseXmlDocument(getMockResponse());
        MockResponseMessageEditor mockResponseMessageEditor = new MockResponseMessageEditor(documentContent);
        SyntaxEditorUtil.setMediaType(mockResponseMessageEditor.getInputArea(), getModelItem().getMediaType());
        return mockResponseMessageEditor;
    }

    private JComponent createMediaTypeCombo() {
        MediaTypeComboBox mediaTypeComboBox = new MediaTypeComboBox(this.getModelItem());
        mediaTypeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                SyntaxEditorUtil.setMediaType(getResponseEditor().getInputArea(), e.getItem().toString());
            }
        });
        JComponent innerPanel = createPanelWithLabel("Content | Media type: ", mediaTypeComboBox);

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.X_AXIS));
        outerPanel.add(innerPanel);
        outerPanel.add(Box.createHorizontalGlue());
        outerPanel.add(UISupport.createFormButton(new ShowOnlineHelpAction(HelpUrls.REST_MOCK_RESPONSE_EDITOR_BODY)));

        return outerPanel;
    }

    private JComponent createPanelWithLabel(String labelText, Component rightSideComponent) {
        JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        innerPanel.add(new JLabel(labelText));
        innerPanel.add(rightSideComponent);

        return innerPanel;
    }

    private JComboBox createStatusCodeCombo() {
        ComboBoxModel httpStatusCodeComboBoxModel = new HttpStatusCodeComboBoxModel();

        final JComboBox statusCodeCombo = new JComboBox(httpStatusCodeComboBoxModel);

        statusCodeCombo.setSelectedItem(CompleteHttpStatus.from(getModelItem().getResponseHttpStatus()));
        statusCodeCombo.setToolTipText("Set desired HTTP status code");
        statusCodeCombo.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                getModelItem().setResponseHttpStatus(((CompleteHttpStatus) statusCodeCombo.getSelectedItem()).getStatusCode());
            }
        });
        return statusCodeCombo;
    }

    private JComponent createHeaderInspector() {
        MockResponseHeadersModel model = new MockResponseHeadersModel(getModelItem());
        HttpHeadersInspector inspector = new HttpHeadersInspector(model);

        JComponent component = inspector.getComponent();
        return component;
    }

    public boolean hasRequestEditor() {
        return false;
    }

    @Override
    public String getHelpUrl() {
        return HelpUrls.REST_MOCK_RESPONSE_EDITOR;
    }

}

class CompleteHttpStatus {
    private int statusCode;
    private String description;

    private CompleteHttpStatus(int statusCode) {
        this.statusCode = statusCode;
        this.description = HttpStatus.getStatusText(statusCode);
    }

    public static CompleteHttpStatus from(int statusCode) {
        return new CompleteHttpStatus(statusCode);
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String toString() {
        return "" + statusCode + " - " + description;
    }

    @Override
    public boolean equals(Object object) {
        return ((CompleteHttpStatus) object).statusCode == statusCode;

    }
}

class HttpStatusCodeComboBoxModel extends DefaultComboBoxModel {
    private static Vector<CompleteHttpStatus> LIST_OF_CODES = new Vector<CompleteHttpStatus>();

    static {
        final String statusCodePrefix = "SC_";

        for (Field statusCodeField : HttpStatus.class.getDeclaredFields()) {
            try {
                if (statusCodeField.getName().startsWith(statusCodePrefix)) {
                    LIST_OF_CODES.add(CompleteHttpStatus.from(statusCodeField.getInt(null)));
                }
            } catch (IllegalAccessException e) {
                SoapUI.logError(e);
            }
        }
    }

    public HttpStatusCodeComboBoxModel() {
        super(LIST_OF_CODES);
    }
}
