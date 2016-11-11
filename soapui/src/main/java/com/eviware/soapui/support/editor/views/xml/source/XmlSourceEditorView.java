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

package com.eviware.soapui.support.editor.views.xml.source;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.submit.transports.http.DocumentContent;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.settings.UISettings;
import com.eviware.soapui.support.DocumentListenerAdapter;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JEditorStatusBar.JEditorStatusBarTarget;
import com.eviware.soapui.support.components.PreviewCorner;
import com.eviware.soapui.support.editor.EditorDocument;
import com.eviware.soapui.support.editor.EditorLocation;
import com.eviware.soapui.support.editor.views.AbstractXmlEditorView;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.soapui.support.editor.xml.XmlEditor;
import com.eviware.soapui.support.editor.xml.XmlLocation;
import com.eviware.soapui.support.editor.xml.support.ValidationError;
import com.eviware.soapui.support.propertyexpansion.PropertyExpansionPopupListener;
import com.eviware.soapui.support.swing.JTextComponentPopupMenu;
import com.eviware.soapui.support.swing.SoapUISplitPaneUI;
import com.eviware.soapui.support.xml.XmlUtils;
import com.eviware.soapui.support.xml.actions.EnableLineNumbersAction;
import com.eviware.soapui.support.xml.actions.FormatXmlAction;
import com.eviware.soapui.support.xml.actions.GoToLineAction;
import com.eviware.soapui.support.xml.actions.InsertBase64FileTextAreaAction;
import com.eviware.soapui.support.xml.actions.LoadXmlTextAreaAction;
import com.eviware.soapui.support.xml.actions.SaveXmlTextAreaAction;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.eviware.soapui.support.JsonUtil.seemsToBeJsonContentType;

/**
 * Default "XML" source editor view in SoapUI
 *
 * @author ole.matzura
 */

public class XmlSourceEditorView<T extends ModelItem> extends AbstractXmlEditorView<XmlDocument> {
    private static final String RSYNTAXAREA_THEME = "/rsyntaxarea-theme/soapui.xml";

    private RSyntaxTextArea editArea;
    private RTextScrollPane editorScrollPane;
    private ValidateMessageXmlAction validateXmlAction;
    private JSplitPane splitter;
    private JScrollPane errorScrollPane;
    private DefaultListModel errorListModel;
    private boolean updating;
    public boolean isLocating;
    private JPopupMenu inputPopup;
    private PreviewCorner previewCorner;
    private T modelItem;

    private EnableLineNumbersAction enableLineNumbersAction;
    private GoToLineAction goToLineAction;
    private SaveXmlTextAreaAction saveXmlTextAreaAction;

    // Read only views don't need these
    private FormatXmlAction formatXmlAction;
    private LoadXmlTextAreaAction loadXmlTextAreaAction;
    private InsertBase64FileTextAreaAction insertBase64FileTextAreaAction;
    private FindAndReplaceDialogView findAndReplaceDialog;
    private final boolean readOnly;

    public XmlSourceEditorView(XmlEditor<XmlDocument> xmlEditor, T modelItem, boolean readOnly) {
        this(xmlEditor, modelItem, readOnly, "XML");
    }

    public XmlSourceEditorView(XmlEditor<XmlDocument> xmlEditor, T modelItem, boolean readOnly, String tabTitle) {
        super(tabTitle, xmlEditor, XmlSourceEditorViewFactory.VIEW_ID);
        this.modelItem = modelItem;
        this.readOnly = readOnly;
    }

    protected void buildUI() {
        editArea = new RSyntaxTextArea(20, 60);

        try {
            Theme theme = Theme.load(XmlSourceEditorView.class.getResourceAsStream(RSYNTAXAREA_THEME));
            theme.apply(editArea);
        } catch (IOException e) {
            SoapUI.logError(e, "Could not load XML editor color theme file");
        }

        editArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        editArea.setFont(UISupport.getEditorFont());
        editArea.setCodeFoldingEnabled(true);
        editArea.setAntiAliasingEnabled(true);
        editArea.setMinimumSize(new Dimension(50, 50));
        editArea.setCaretPosition(0);
        editArea.setEnabled(!readOnly);
        editArea.setEditable(!readOnly);
        editArea.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Color.WHITE));

        errorListModel = new DefaultListModel();
        JList list = new JList(errorListModel);
        list.addMouseListener(new ValidationListMouseAdapter(list, editArea));
        errorScrollPane = new JScrollPane(list);
        errorScrollPane.setVisible(false);

        splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT) {
            public void requestFocus() {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        editArea.requestFocusInWindow();
                    }
                });
            }

            public boolean hasFocus() {
                return editArea.hasFocus();
            }
        };

        splitter.setUI(new SoapUISplitPaneUI());
        splitter.setDividerSize(0);
        splitter.setOneTouchExpandable(true);

        editArea.getDocument().addDocumentListener(new DocumentListenerAdapter() {

            public void update(Document document) {
                if (!updating && getDocument() != null) {
                    updating = true;
                    getDocument().setDocumentContent(getDocument().getDocumentContent(EditorDocument.Format.XML).withContent(editArea.getText()));
                    updating = false;
                }
            }
        });

        JPanel p = new JPanel(new BorderLayout());
        editorScrollPane = new RTextScrollPane(editArea);

        JTextComponentPopupMenu.add(editArea);

        buildPopup(editArea.getPopupMenu(), editArea);

        if (UISupport.isMac()) {
            editArea.getInputMap().put(KeyStroke.getKeyStroke("shift meta V"), validateXmlAction);
            editArea.getInputMap().put(KeyStroke.getKeyStroke("meta S"), saveXmlTextAreaAction);
            editArea.getInputMap().put(KeyStroke.getKeyStroke("control L"), enableLineNumbersAction);
            editArea.getInputMap().put(KeyStroke.getKeyStroke("control G"), goToLineAction);
            editArea.getInputMap().put(KeyStroke.getKeyStroke("meta F"), findAndReplaceDialog);
            editArea.getInputMap().put(KeyStroke.getKeyStroke("shift meta F"), formatXmlAction);
            if (!readOnly) {
                editArea.getInputMap().put(KeyStroke.getKeyStroke("meta L"), loadXmlTextAreaAction);
            }
        } else {
            editArea.getInputMap().put(KeyStroke.getKeyStroke("alt V"), validateXmlAction);
            editArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl S"), saveXmlTextAreaAction);
            editArea.getInputMap().put(KeyStroke.getKeyStroke("alt L"), enableLineNumbersAction);
            editArea.getInputMap().put(KeyStroke.getKeyStroke("control G"), goToLineAction);
            editArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl F"), findAndReplaceDialog);
            editArea.getInputMap().put(KeyStroke.getKeyStroke("alt F"), formatXmlAction);
            if (!readOnly) {
                editArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl L"), loadXmlTextAreaAction);
            }
        }


        editorScrollPane.setLineNumbersEnabled(SoapUI.getSettings().getBoolean(UISettings.SHOW_XML_LINE_NUMBERS));
        editorScrollPane.setFoldIndicatorEnabled(true);
        p.add(editorScrollPane, BorderLayout.CENTER);

        splitter.setTopComponent(editorScrollPane);
        splitter.setBottomComponent(errorScrollPane);
        splitter.setDividerLocation(1.0);
        splitter.setBorder(null);

        previewCorner = UISupport.addPreviewCorner(getEditorScrollPane(), true);
        if (!readOnly) {
            PropertyExpansionPopupListener.enable(editArea, modelItem);
        }
    }

    public JScrollPane getEditorScrollPane() {
        return editorScrollPane;
    }

    public T getModelItem() {
        return modelItem;
    }

    protected void buildPopup(JPopupMenu inputPopup, RSyntaxTextArea editArea) {
        this.inputPopup = inputPopup;
        validateXmlAction = new ValidateMessageXmlAction();
        saveXmlTextAreaAction = new SaveXmlTextAreaAction(editArea, "Save");
        enableLineNumbersAction = new EnableLineNumbersAction(editorScrollPane, "Toggle Line Numbers");
        goToLineAction = new GoToLineAction(editArea, "Go To Line");
        findAndReplaceDialog = new FindAndReplaceDialogView("Find / Replace");

        if (!readOnly) {
            loadXmlTextAreaAction = new LoadXmlTextAreaAction(editArea, "Load");
            insertBase64FileTextAreaAction = new InsertBase64FileTextAreaAction(editArea, "Insert File as Base64");
        }

        int cnt = inputPopup.getComponentCount();
        for (int i = cnt - 1; i >= 0; i--) {
            if (inputPopup.getComponent(i) instanceof JSeparator) {
                inputPopup.remove(inputPopup.getComponent(i));
            }
        }

        inputPopup.insert(validateXmlAction, 0);

        formatXmlAction = new FormatXmlAction(editArea);
        inputPopup.insert(formatXmlAction, 1);
        inputPopup.addSeparator();

        inputPopup.add(findAndReplaceDialog);
        inputPopup.addSeparator();
        inputPopup.add(goToLineAction);
        inputPopup.add(enableLineNumbersAction);
        inputPopup.addSeparator();
        inputPopup.add(saveXmlTextAreaAction);
        if (!readOnly) {
            inputPopup.add(loadXmlTextAreaAction);
            inputPopup.add(insertBase64FileTextAreaAction);
        }
    }

    @Override
    public void release() {
        super.release();
        inputPopup.removeAll();
        previewCorner.release();
        modelItem = null;
    }

    private final class FindAndReplaceDialogView extends AbstractAction {
        private JDialog dialog;
        private JCheckBox caseCheck;
        private JRadioButton allButton;
        private JRadioButton selectedLinesButton;
        private JRadioButton forwardButton;
        private JRadioButton backwardButton;
        private JCheckBox wholeWordCheck;
        private JButton findButton;
        private JButton replaceButton;
        private JButton replaceAllButton;
        private JComboBox findCombo;
        private JComboBox replaceCombo;
        private final String title;

        public FindAndReplaceDialogView(String title) {
            super(title);
            this.title = title;
            if (UISupport.isMac()) {
                putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("meta F"));
            } else {
                putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("ctrl F"));
            }
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            show();
        }

        public void show() {
            if (dialog == null) {
                buildDialog();
            }

            editArea.requestFocusInWindow();

            replaceCombo.setEnabled(!readOnly);
            replaceAllButton.setEnabled(!readOnly);
            replaceButton.setEnabled(!readOnly);

            UISupport.showDialog(dialog);
            findCombo.getEditor().selectAll();
            findCombo.requestFocus();
        }

        private void buildDialog() {
            Window window = SwingUtilities.windowForComponent(editArea);

            dialog = new JDialog(window, title);
            dialog.setModal(false);

            JPanel panel = new JPanel(new BorderLayout());
            findCombo = new JComboBox();
            findCombo.setEditable(true);
            replaceCombo = new JComboBox();
            replaceCombo.setEditable(!readOnly);

            // create inputs
            GridLayout gridLayout = new GridLayout(2, 2);
            gridLayout.setVgap(5);
            JPanel inputPanel = new JPanel(gridLayout);
            inputPanel.add(new JLabel("Find:"));
            inputPanel.add(findCombo);
            inputPanel.add(new JLabel("Replace with:"));
            inputPanel.add(replaceCombo);
            inputPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            // create direction panel
            ButtonGroup directionGroup = new ButtonGroup();
            forwardButton = new JRadioButton("Forward", true);
            forwardButton.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            directionGroup.add(forwardButton);
            backwardButton = new JRadioButton("Backward");
            backwardButton.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            directionGroup.add(backwardButton);

            JPanel directionPanel = new JPanel(new GridLayout(2, 1));
            directionPanel.add(forwardButton);
            directionPanel.add(backwardButton);
            directionPanel.setBorder(BorderFactory.createTitledBorder("Direction"));

            // create scope panel
            ButtonGroup scopeGroup = new ButtonGroup();
            allButton = new JRadioButton("All", true);
            allButton.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            selectedLinesButton = new JRadioButton("Selected Lines");
            selectedLinesButton.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            scopeGroup.add(allButton);
            scopeGroup.add(selectedLinesButton);

            JPanel scopePanel = new JPanel(new GridLayout(2, 1));
            scopePanel.add(allButton);
            scopePanel.add(selectedLinesButton);
            scopePanel.setBorder(BorderFactory.createTitledBorder("Scope"));

            // create options
            caseCheck = new JCheckBox("Case Sensitive");
            caseCheck.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            wholeWordCheck = new JCheckBox("Whole Word");
            wholeWordCheck.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            JPanel optionsPanel = new JPanel(new GridLayout(2, 1));
            optionsPanel.add(caseCheck);
            optionsPanel.add(wholeWordCheck);
            optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

            // create panel with options
            JPanel options = new JPanel(new GridLayout(1, 2));

            JPanel radios = new JPanel(new GridLayout(2, 1));
            radios.add(directionPanel);
            radios.add(scopePanel);

            options.add(optionsPanel);
            options.add(radios);
            options.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            // create buttons
            ButtonBarBuilder builder = new ButtonBarBuilder();
            findButton = new JButton(new FindAction(findCombo));
            builder.addFixed(findButton);
            builder.addRelatedGap();
            replaceButton = new JButton(new ReplaceAction());
            builder.addFixed(replaceButton);
            builder.addRelatedGap();
            replaceAllButton = new JButton(new ReplaceAllAction());
            builder.addFixed(replaceAllButton);
            builder.addUnrelatedGap();
            builder.addFixed(new JButton(new CloseAction(dialog)));
            builder.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            // tie it up!
            panel.add(inputPanel, BorderLayout.NORTH);
            panel.add(options, BorderLayout.CENTER);
            panel.add(builder.getPanel(), BorderLayout.SOUTH);

            dialog.getContentPane().add(panel);
            dialog.pack();
            UISupport.initDialogActions(dialog, null, findButton);
        }

        protected SearchContext createSearchAndReplaceContext() {
            if (findCombo.getSelectedItem() == null) {
                return null;
            }
            if (replaceCombo.getSelectedItem() == null) {
                return null;
            }

            String searchExpression = findCombo.getSelectedItem().toString();
            String replacement = replaceCombo.getSelectedItem().toString();

            SearchContext context = new SearchContext();
            context.setSearchFor(searchExpression);
            context.setReplaceWith(replacement);
            context.setRegularExpression(false);
            context.setMatchCase(caseCheck.isSelected());
            context.setSearchForward(forwardButton.isSelected());
            context.setWholeWord(wholeWordCheck.isSelected());
            return context;
        }

        protected SearchContext createSearchContext() {
            if (findCombo.getSelectedItem() == null) {
                return null;
            }

            String searchExpression = findCombo.getSelectedItem().toString();

            SearchContext context = new SearchContext();
            context.setSearchFor(searchExpression);
            context.setRegularExpression(false);
            context.setSearchForward(forwardButton.isSelected());
            context.setMatchCase(caseCheck.isSelected());
            context.setWholeWord(false);
            return context;
        }

        private class FindAction extends AbstractAction {
            public FindAction(JComboBox findCombo) {
                super("Find/Find Next");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                SearchContext context = createSearchContext();

                if (context == null) {
                    return;
                }

                boolean found = SearchEngine.find(editArea, context);
                if (!found) {
                    UISupport.showErrorMessage("String [" + context.getSearchFor() + "] not found");
                }
            }
        }

        private class ReplaceAction extends AbstractAction {
            public ReplaceAction() {
                super("Replace/Replace Next");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                SearchContext context = createSearchAndReplaceContext();

                if (context == null) {
                    return;
                }

                boolean found = SearchEngine.replace(editArea, context);
                if (!found) {
                    UISupport.showErrorMessage("String [" + context.getSearchFor() + "] not found");
                }
            }

        }

        private class ReplaceAllAction extends AbstractAction {
            public ReplaceAllAction() {
                super("Replace All");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                SearchContext context = createSearchAndReplaceContext();

                if (context == null) {
                    return;
                }

                int replaceCount = SearchEngine.replaceAll(editArea, context);
                if (replaceCount <= 0) {
                    UISupport.showErrorMessage("String [" + context.getSearchFor() + "] not found");
                }
            }

        }

        private class CloseAction extends AbstractAction {
            final JDialog dialog;

            public CloseAction(JDialog d) {
                super("Close");
                dialog = d;
            }

            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        }

    }

    private final static class ValidationListMouseAdapter extends MouseAdapter {
        private final JList list;

        private final RSyntaxTextArea textArea;

        public ValidationListMouseAdapter(JList list, RSyntaxTextArea editArea) {
            this.list = list;
            this.textArea = editArea;
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() < 2) {
                return;
            }

            int ix = list.getSelectedIndex();
            if (ix == -1) {
                return;
            }

            Object obj = list.getModel().getElementAt(ix);
            if (obj instanceof ValidationError) {
                ValidationError error = (ValidationError) obj;
                if (error.getLineNumber() >= 0) {
                    try {
                        textArea.setCaretPosition(textArea.getLineStartOffset(error.getLineNumber() - 1));
                    } catch (BadLocationException e1) {
                        SoapUI.logError(e1, "Unable to set the caret position. This is most likely a bug.");
                    }
                    textArea.requestFocus();
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    public RSyntaxTextArea getInputArea() {
        getComponent();
        return editArea;
    }

    public static class JEditorStatusBarTargetProxy implements JEditorStatusBarTarget {
        private final RSyntaxTextArea textArea;

        public JEditorStatusBarTargetProxy(RSyntaxTextArea area) {
            textArea = area;
        }

        @Override
        public void addCaretListener(CaretListener listener) {
            textArea.addCaretListener(listener);
        }

        @Override
        public int getCaretPosition() {
            return textArea.getCaretPosition();
        }

        @Override
        public void removeCaretListener(CaretListener listener) {
            textArea.removeCaretListener(listener);
        }

        @Override
        public int getLineStartOffset(int line) throws Exception {
            return textArea.getLineStartOffset(line);
        }

        @Override
        public int getLineOfOffset(int offset) throws Exception {
            return textArea.getLineOfOffset(offset);
        }
    }

    public void setEditable(boolean enabled) {
        getComponent();
        editArea.setEditable(enabled);
    }

    protected ValidationError[] validateXml(String xml) {
        try {
            XmlUtils.createXmlObject(xml, new XmlOptions().setLoadLineNumbers());
        } catch (XmlException e) {
            List<ValidationError> result = new ArrayList<ValidationError>();

            if (e.getErrors() != null) {
                for (Object error : e.getErrors()) {
                    if (error instanceof XmlError) {
                        result.add(new com.eviware.soapui.model.testsuite.AssertionError((XmlError) error));
                    } else {
                        result.add(new com.eviware.soapui.model.testsuite.AssertionError(error.toString()));
                    }
                }
            }

            if (result.isEmpty()) {
                result.add(new com.eviware.soapui.model.testsuite.AssertionError(e.toString()));
            }

            return result.toArray(new ValidationError[result.size()]);
        }

        return null;
    }

    public class ValidateMessageXmlAction extends AbstractAction {
        public ValidateMessageXmlAction() {
            super("Validate");
            if (UISupport.isMac()) {
                putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("shift meta V"));
            } else {
                putValue(Action.ACCELERATOR_KEY, UISupport.getKeyStroke("alt V"));
            }
        }

        public void actionPerformed(ActionEvent e) {
            if (validate()) {
                UISupport.showInfoMessage("Validation OK");
            }
        }
    }

    public boolean activate(XmlLocation location) {
        super.activate(location);

        if (location != null) {
            setLocation(location);
        }

        editArea.requestFocus();

        return true;
    }

    public JComponent getComponent() {
        if (splitter == null) {
            buildUI();
        }

        return splitter;
    }

    public XmlLocation getEditorLocation() {
        return new XmlLocation(getCurrentLine() + 1, getCurrentColumn());
    }

    @Override
    public void setLocation(EditorLocation<XmlDocument> location) {
        int line = location.getLine() - 1;
        if (line >= 0) {
            try {
                int caretLine = editArea.getCaretLineNumber();
                int offset = editArea.getLineStartOffset(line);
                editArea.setCaretPosition(offset + location.getColumn());
                int scrollLine = line + (line > caretLine ? 3 : -3);
                if (scrollLine >= editArea.getLineCount()) {
                    scrollLine = editArea.getLineCount() - 1;
                } else if (scrollLine < 0) {
                    scrollLine = 0;
                }

                editArea.scrollRectToVisible(new Rectangle(scrollLine, location.getColumn()));
            } catch (RuntimeException ignore) {
            } catch (BadLocationException e) {
                SoapUI.logError(e, "Unable to set the location in the XML document.");
            }
        }
    }

    public int getCurrentLine() {
        if (editArea == null) {
            return -1;
        }
        return editArea.getCaretLineNumber();
    }

    public int getCurrentColumn() {
        if (editArea == null) {
            return -1;
        }

        try {
            int pos = editArea.getCaretPosition();
            int line = editArea.getLineOfOffset(pos);

            return pos - editArea.getLineStartOffset(line);
        } catch (BadLocationException e) {
            SoapUI.logError(e, "Unable to get the current column. ");
            return -1;
        }
    }

    public String getText() {
        if (editArea == null) {
            return null;
        }
        return editArea.getText();
    }

    public boolean validate() {
        ValidationError[] errors = validateXml(PropertyExpander.expandProperties(getModelItem(), editArea.getText()));

        errorListModel.clear();
        if (errors == null || errors.length == 0) {
            splitter.setDividerLocation(1.0);
            splitter.setDividerSize(0);
            errorScrollPane.setVisible(false);
            return true;
        } else {
            Toolkit.getDefaultToolkit().beep();
            for (ValidationError error : errors) {
                errorListModel.addElement(error);
            }
            errorScrollPane.setVisible(true);
            splitter.setDividerLocation(0.8);
            splitter.setDividerSize(10);
            return false;
        }
    }

    @Override
    public void documentUpdated() {
        if (!updating) {
            updating = true;

            final DocumentContent rawDocumentContent = getDocument().getDocumentContent(EditorDocument.Format.RAW);
            final String contentType = rawDocumentContent.getContentType();
            if (rawDocumentContent.getContentAsString() == null) {
                editArea.setText("");
                editArea.setEnabled(false);
            } else if (seemsToBeJsonContentType(contentType) && readOnly) {
                editArea.setText("The content you are trying to view cannot be viewed as XML");
                editArea.setEnabled(false);
            } else {
                int caretPosition = editArea.getCaretPosition();
                editArea.setEnabled(true);
                final String contentAsString = getDocument().getDocumentContent(EditorDocument.Format.XML).getContentAsString();
                editArea.setText(contentAsString);
                editArea.setCaretPosition(caretPosition < contentAsString.length() ? caretPosition : 0);
            }

            updating = false;
        }
    }

    public boolean saveDocument(boolean validate) {
        return validate ? validate() : true;
    }

    public void locationChanged(EditorLocation<XmlDocument> location) {
        isLocating = true;
        setLocation(location);
        isLocating = false;
    }

    public JPopupMenu getEditorPopup() {
        return editArea.getPopupMenu();
    }

    public boolean hasFocus() {
        return editArea.hasFocus();
    }

    public boolean isInspectable() {
        return true;
    }

    public ValidateMessageXmlAction getValidateXmlAction() {
        return validateXmlAction;
    }
}
