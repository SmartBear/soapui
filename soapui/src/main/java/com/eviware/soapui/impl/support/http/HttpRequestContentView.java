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

package com.eviware.soapui.impl.support.http;

import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTableModel;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.handlers.JsonXmlSerializer;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpRequestDocument;
import com.eviware.soapui.impl.support.panels.AbstractHttpXmlRequestDesktopPanel.HttpRequestMessageEditor;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.MediaTypeComboBox;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.propertyexpansion.PropertyExpansionPopupListener;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;
import com.eviware.soapui.support.xml.XmlUtils;
import net.sf.json.JSON;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation;
import static com.eviware.soapui.support.JsonUtil.seemsToBeJsonContentType;

@SuppressWarnings("unchecked")
public class HttpRequestContentView extends AbstractXmlEditorView<HttpRequestDocument> implements
        PropertyChangeListener {
    private final HttpRequestInterface<?> httpRequest;
    private RSyntaxTextArea contentEditor;
    private boolean updatingRequest;
    private JComponent panel;
    private JComboBox mediaTypeCombo;
    private JSplitPane split;
    protected RestParamsTable paramsTable;
    private JCheckBox postQueryCheckBox;

    public HttpRequestContentView(HttpRequestMessageEditor httpRequestMessageEditor, HttpRequestInterface<?> httpRequest) {
        super("Request", httpRequestMessageEditor, HttpRequestContentViewFactory.VIEW_ID);
        this.httpRequest = httpRequest;

        httpRequest.addPropertyChangeListener(this);
    }

    public JComponent getComponent() {
        if (panel == null) {
            buildComponent();
        }

        return panel;
    }

    protected void buildComponent() {
        JPanel p = new JPanel(new BorderLayout());

        p.add(buildToolbar(), BorderLayout.NORTH);
        p.add(buildContent(), BorderLayout.CENTER);

        paramsTable = buildParamsTable();

        split = UISupport.createVerticalSplit(paramsTable, p);

        panel = new JPanel(new BorderLayout());
        panel.add(split);

        fixRequestPanel();
    }

    protected RestParamsTable buildParamsTable() {
        RestParamsTableModel restParamsTableModel = new RestParamsTableModel(httpRequest.getParams()) {
            @Override
            public String getColumnName(int column) {
                return column == 0 ? "Name" : "Value";
            }

            public int getColumnCount() {
                return 2;
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                RestParamProperty prop = params.getPropertyAt(rowIndex);
                return columnIndex == 0 ? prop.getName() : prop.getValue();
            }

            @Override
            public void setValueAt(Object value, int rowIndex, int columnIndex) {
                RestParamProperty prop = params.getPropertyAt(rowIndex);
                if (columnIndex == 0) {
                    prop.setName(value.toString());
                } else {
                    prop.setValue(value.toString());
                }
            }
        };
        return new RestParamsTable(httpRequest.getParams(), false, restParamsTableModel, ParamLocation.RESOURCE, true, false);
    }

    @Override
    public void release() {
        super.release();
        httpRequest.removePropertyChangeListener(this);
        paramsTable.release();
    }

    public HttpRequestInterface<?> getRestRequest() {
        return httpRequest;
    }

    protected Component buildContent() {
        JPanel contentPanel = new JPanel(new BorderLayout());

        // Add popup!
        contentEditor = SyntaxEditorUtil.createDefaultXmlSyntaxTextArea();
        SyntaxEditorUtil.setMediaType(contentEditor, httpRequest.getMediaType());
        contentEditor.setText(httpRequest.getRequestContent());

        contentEditor.getDocument().addDocumentListener(new DocumentListenerAdapter() {

            @Override
            public void update(Document document) {
                if (!updatingRequest) {
                    updatingRequest = true;
                    httpRequest.setRequestContent(getText(document));
                    updatingRequest = false;
                }
            }
        });

        contentPanel.add(new JScrollPane(contentEditor));

        PropertyExpansionPopupListener.enable(contentEditor, httpRequest);

        return contentPanel;
    }

    private void enableBodyComponents() {
        httpRequest.setPostQueryString(httpRequest.hasRequestBody() && httpRequest.isPostQueryString());
        postQueryCheckBox.setSelected(httpRequest.isPostQueryString());
        mediaTypeCombo.setEnabled(httpRequest.hasRequestBody() && !httpRequest.isPostQueryString());
        contentEditor.setEnabled(httpRequest.hasRequestBody() && !httpRequest.isPostQueryString());
        contentEditor.setEditable(httpRequest.hasRequestBody() && !httpRequest.isPostQueryString());
        postQueryCheckBox.setEnabled(httpRequest.hasRequestBody());
    }

    protected Component buildToolbar() {
        JXToolBar toolbar = UISupport.createToolbar();

        addMediaTypeCombo(toolbar);
        toolbar.addSeparator();

        addPostQueryCheckBox(toolbar);

        toolbar.setMinimumSize(new Dimension(50, 20));

        return toolbar;
    }

    protected void addPostQueryCheckBox(JXToolBar toolbar) {
        postQueryCheckBox = new JCheckBox("Post QueryString", httpRequest.isPostQueryString());
        postQueryCheckBox.setToolTipText("Controls if Query-parameters should be put in message body");
        postQueryCheckBox.setOpaque(false);
        postQueryCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                httpRequest.setPostQueryString(postQueryCheckBox.isSelected());
                enableBodyComponents();
            }
        });

        toolbar.add(postQueryCheckBox);
    }

    protected void addMediaTypeCombo(JXToolBar toolbar) {
        mediaTypeCombo = new MediaTypeComboBox(httpRequest);
        mediaTypeCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                SyntaxEditorUtil.setMediaType(contentEditor, e.getItem().toString());
            }
        });
        mediaTypeCombo.setEnabled(httpRequest.hasRequestBody());
        toolbar.addLabeledFixed("Media Type", mediaTypeCombo);
    }

    protected Object[] getRequestMediaTypes() {
        return MediaTypeComboBox.getMediaTypes();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(AbstractHttpRequest.REQUEST_PROPERTY) && !updatingRequest) {
            updatingRequest = true;
            String requestBodyAsXml = (String) evt.getNewValue();
            String mediaType = (String) mediaTypeCombo.getSelectedItem();
            if (XmlUtils.seemsToBeXml(requestBodyAsXml) &&
                    seemsToBeJsonContentType(mediaType)) {
                JSON jsonObject = new JsonXmlSerializer().read(requestBodyAsXml);
                contentEditor.setText(jsonObject.toString(3, 0));
            } else {
                contentEditor.setText(requestBodyAsXml);
            }
            updatingRequest = false;
        } else if (evt.getPropertyName().equals("method")) {
            fixRequestPanel();
        } else if (evt.getPropertyName().equals(Request.MEDIA_TYPE)) {
            mediaTypeCombo.setSelectedItem(evt.getNewValue());
        } else if (evt.getPropertyName().equals(AbstractHttpRequest.ATTACHMENTS_PROPERTY)) {
            mediaTypeCombo.setModel(new DefaultComboBoxModel(getRequestMediaTypes()));
            mediaTypeCombo.setSelectedItem(httpRequest.getMediaType());
        }

        super.propertyChange(evt);
        if (paramsTable != null) {
            paramsTable.refresh();
        }
    }

    private void fixRequestPanel() {
        if (httpRequest.hasRequestBody()) {
            panel.remove(paramsTable);
            split.setLeftComponent(paramsTable);
            panel.add(split);
            enableBodyComponents();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // wait for panel to get shown..
                    if (panel.getHeight() == 0) {
                        SwingUtilities.invokeLater(this);
                    } else {
                        split.setDividerLocation(0.5F);
                    }
                }
            });
        } else {
            panel.remove(split);
            panel.add(paramsTable);
        }
    }

    public boolean saveDocument(boolean validate) {
        return false;
    }

    public void setEditable(boolean enabled) {
        contentEditor.setEnabled(enabled && httpRequest.hasRequestBody());
        contentEditor.setEditable(enabled && httpRequest.hasRequestBody());
        mediaTypeCombo.setEnabled(enabled && !httpRequest.isPostQueryString());
        postQueryCheckBox.setEnabled(enabled);
    }

    public RestParamsTable getParamsTable() {
        return paramsTable;
    }

}
