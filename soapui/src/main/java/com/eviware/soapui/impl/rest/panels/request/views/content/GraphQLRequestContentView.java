package com.eviware.soapui.impl.rest.panels.request.views.content;

import com.eviware.soapui.impl.wsdl.panels.teststeps.GraphQLRequestTestStepDesktopPanel;
import com.eviware.soapui.impl.wsdl.teststeps.GraphQLTestRequestInterface;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.JsonUtil;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.propertyexpansion.PropertyExpansionPopupListener;
import com.eviware.soapui.support.xml.SyntaxEditorUtil;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeListener;

public class GraphQLRequestContentView extends AbstractXmlEditorView<XmlDocument> implements PropertyChangeListener {
    private static final String VIEW_ID = "GraphQLRequestView";
    private static final String TITLE = "Request";
    protected final GraphQLTestRequestInterface graphQLRequest;
    protected RSyntaxTextArea contentEditor;
    private boolean updatingRequest;
    private JSplitPane splitter;
    protected JComponent mainPanel;

    public GraphQLRequestContentView(GraphQLRequestTestStepDesktopPanel.GraphQLRequestMessageEditor editor,
                                     GraphQLTestRequestInterface graphQLRequest) {
        super(TITLE, editor, VIEW_ID);
        this.graphQLRequest = graphQLRequest;
        this.graphQLRequest.addPropertyChangeListener(this);
    }

    public JComponent getComponent() {
        if (mainPanel == null) {
            buildComponent();
        }
        return mainPanel;
    }

    protected void buildComponent() {
        Component queryPanel = buildQueryPanel();
        Component variablesPanel = buildVariablesPanel();
        splitter = UISupport.createVerticalSplit(queryPanel, variablesPanel);
        splitter.setResizeWeight(0.5);
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(splitter);
    }

    protected Component buildQueryPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentEditor = SyntaxEditorUtil.createDefaultJsonSyntaxTextArea();
        contentEditor.setCurrentLineHighlightColor(Color.WHITE);
        SyntaxEditorUtil.setMediaType(contentEditor, graphQLRequest.getMediaType());
        contentEditor.setText(graphQLRequest.getQuery());
        addPropertyExpansionPopup(contentEditor);
        contentEditor.getDocument().addDocumentListener(new DocumentListenerAdapter() {
            @Override
            public void update(Document document) {
                if (!updatingRequest) {
                    updatingRequest = true;
                    graphQLRequest.setQuery(getText(document));
                    updatingRequest = false;
                }
            }
        });
        applyCodeCompletion();
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(contentEditor);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        return contentPanel;
    }

    protected void applyCodeCompletion() {
    }

    protected void removeCodeCompletion() {
    }

    protected Component buildVariablesPanel() {
        JPanel variablesPanel = new JPanel(new BorderLayout());
        JLabel variablesLabel = new JLabel("Query Variables");
        JPanel innerPanel = new JPanel(new BorderLayout());
        innerPanel.add(variablesLabel);
        variablesPanel.add(innerPanel, BorderLayout.NORTH);
        RSyntaxTextArea variablesEditor = SyntaxEditorUtil.createDefaultJsonSyntaxTextArea();
        variablesEditor.setCurrentLineHighlightColor(Color.WHITE);
        SyntaxEditorUtil.setMediaType(variablesEditor, graphQLRequest.getMediaType());
        variablesEditor.setText(graphQLRequest.getVariables());
        addPropertyExpansionPopup(variablesEditor);
        variablesEditor.getDocument().addDocumentListener(new DocumentListenerAdapter() {
            @Override
            public void update(Document document) {
                if (!updatingRequest) {
                    updatingRequest = true;
                    graphQLRequest.setVariables(getText(document));
                    updatingRequest = false;
                }
            }
        });
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(variablesEditor);
        variablesPanel.add(scrollPane, BorderLayout.CENTER);
        return variablesPanel;
    }

    protected void addPropertyExpansionPopup(RSyntaxTextArea textField) {
        PropertyExpansionPopupListener.enable(textField, graphQLRequest);
    }

    @Override
    public boolean saveDocument(boolean validate) {
        return false;
    }

    @Override
    public void setEditable(boolean enabled) {
    }

    @Override
    public void release() {
        removeCodeCompletion();
        super.release();
    }

    @Override
    public int getSupportScoreForContentType(String contentType) {
        return JsonUtil.seemsToBeJsonContentType(contentType)? 2 : 0;
    }
}
