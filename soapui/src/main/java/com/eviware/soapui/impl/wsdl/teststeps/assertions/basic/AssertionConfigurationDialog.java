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

package com.eviware.soapui.impl.wsdl.teststeps.assertions.basic;

import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.support.JsonUtil;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JUndoableTextArea;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.xml.XmlUtils;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 */
public class AssertionConfigurationDialog {
    private final static Logger log = LogManager.getLogger(AssertionConfigurationDialog.class);

    protected JDialog configurationDialog;
    private JCheckBox allowWildcardsCheckBox;
    private JCheckBox ignoreNamespaceDifferencesCheckBox;
    private JCheckBox ignoreCommentsCheckBox;
    protected JTextArea pathArea;
    protected JTextArea contentArea;
    protected AbstractXmlContainsAssertion assertion;
    protected boolean configureResult;

    public AssertionConfigurationDialog(AbstractXmlContainsAssertion assertion) {
        this.assertion = assertion;
    }

    public boolean configure() {
        if (configurationDialog == null) {
            buildConfigurationDialog();
        }

        initializeFieldsWithValuesFromAssertion();

        UISupport.showDialog(configurationDialog);
        return configureResult;
    }

    protected void initializeFieldsWithValuesFromAssertion() {
        pathArea.setText(this.assertion.getPath());
        contentArea.setText(this.assertion.getExpectedContent());
        allowWildcardsCheckBox.setSelected(this.assertion.isAllowWildcards());
        ignoreNamespaceDifferencesCheckBox.setSelected(this.assertion.isIgnoreNamespaceDifferences());
        ignoreCommentsCheckBox.setSelected(this.assertion.isIgnoreComments());
    }


    public String getHelpURL() {
        return HelpUrls.ASSERTION_JSON_CONTENT;
    }

    protected void buildConfigurationDialog() {
        configurationDialog = new JDialog(UISupport.getMainFrame());
        configurationDialog.setTitle(assertion.getConfigurationDialogTitle());
        configurationDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        // pathArea.requestFocusInWindow();
                    }
                });
            }
        });

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(UISupport.buildDescription(assertion.getPathAreaTitle(), assertion.getPathAreaDescription(),
                null), BorderLayout.NORTH);

        JSplitPane splitPane = UISupport.createVerticalSplit();

        JPanel pathPanel = getPathAreaPanel();

        splitPane.setTopComponent(UISupport.addTitledBorder(pathPanel, assertion.getPathAreaBorderTitle()));

        contentArea = new JUndoableTextArea();
        contentArea.setToolTipText(assertion.getContentAreaToolTipText());

        JPanel matchPanel = new JPanel(new BorderLayout());
        JXToolBar contentToolbar = UISupport.createToolbar();
        assertion.addMatchEditorActions(contentToolbar);

        matchPanel.add(contentToolbar, BorderLayout.NORTH);
        matchPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        splitPane.setBottomComponent(UISupport.addTitledBorder(matchPanel, assertion.getContentAreaBorderTitle()));
        splitPane.setDividerLocation(200);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));

        contentPanel.add(splitPane, BorderLayout.CENTER);

        ButtonBarBuilder builder = new ButtonBarBuilder();

        ShowOnlineHelpAction showOnlineHelpAction = new ShowOnlineHelpAction(this.getHelpURL());
        builder.addFixed(UISupport.createToolbarButton(showOnlineHelpAction));
        builder.addGlue();

        JButton okButton = new JButton(createOkAction());
        builder.addFixed(okButton);
        builder.addRelatedGap();
        builder.addFixed(new JButton(new CancelAction()));

        builder.setBorder(BorderFactory.createEmptyBorder(1, 5, 5, 5));

        contentPanel.add(builder.getPanel(), BorderLayout.SOUTH);

        configurationDialog.setContentPane(contentPanel);
        configurationDialog.setSize(800, 600);
        configurationDialog.setModal(true);
        UISupport.initDialogActions(configurationDialog, showOnlineHelpAction, okButton);
    }

    protected AbstractAction createOkAction() {
        return new OkAction();
    }

    protected JPanel getPathAreaPanel() {
        pathArea = new JUndoableTextArea();
        pathArea.setToolTipText(assertion.getPathAreaToolTipText());

        JPanel pathPanel = new JPanel(new BorderLayout());
        JXToolBar pathToolbar = UISupport.createToolbar();
        assertion.addPathEditorActions(pathToolbar);

        pathPanel.add(pathToolbar, BorderLayout.NORTH);
        pathPanel.add(new JScrollPane(pathArea), BorderLayout.CENTER);
        return pathPanel;
    }

    void addDeclareNamespaceButton(JXToolBar toolbar) {
        if (assertion.canAssertXmlContent()) {
            toolbar.addFixed(new JButton(new DeclareNamespacesFromCurrentAction()));
        }
    }

    public JTextArea getPathArea() {
        return pathArea;
    }

    protected void addMatchEditorActions(JXToolBar toolbar) {
        toolbar.addFixed(new JButton(createSelectFromCurrentAction()));
        toolbar.addRelatedGap();
        toolbar.addFixed(new JButton(createTestPathAction()));

        allowWildcardsCheckBox = new JCheckBox("Allow Wildcards");
        Dimension dim = new Dimension(120, 20);
        allowWildcardsCheckBox.setSize(dim);
        allowWildcardsCheckBox.setPreferredSize(dim);
        allowWildcardsCheckBox.setOpaque(false);

        Dimension largerDim = new Dimension(170, 20);
        ignoreNamespaceDifferencesCheckBox = new JCheckBox("Ignore namespace prefixes");
        ignoreNamespaceDifferencesCheckBox.setSize(largerDim);
        ignoreNamespaceDifferencesCheckBox.setPreferredSize(largerDim);
        ignoreNamespaceDifferencesCheckBox.setOpaque(false);

        ignoreCommentsCheckBox = new JCheckBox("Ignore XML Comments");
        ignoreCommentsCheckBox.setSize(largerDim);
        ignoreCommentsCheckBox.setPreferredSize(largerDim);
        ignoreCommentsCheckBox.setOpaque(false);

        if (assertion.canAssertXmlContent()) {

            toolbar.addRelatedGap();
            toolbar.addFixed(allowWildcardsCheckBox);

            toolbar.addRelatedGap();
            toolbar.addFixed(ignoreNamespaceDifferencesCheckBox);

            toolbar.addRelatedGap();
            toolbar.addFixed(ignoreCommentsCheckBox);
        }
    }

    protected SelectFromCurrentAction createSelectFromCurrentAction() {
        return new SelectFromCurrentAction();
    }

    protected TestPathAction createTestPathAction() {
        return new TestPathAction();
    }

    public JTextArea getContentArea() {
        return contentArea;
    }

    public class OkAction extends AbstractAction {
        public OkAction() {
            super("Save");
        }

        public void actionPerformed(ActionEvent arg0) {
            assertion.setPath(pathArea.getText().trim());
            assertion.setExpectedContent(contentArea.getText());
            assertion.setAllowWildcards(allowWildcardsCheckBox.isSelected());
            assertion.setIgnoreNamespaceDifferences(ignoreNamespaceDifferencesCheckBox.isSelected());
            assertion.setIgnoreComments(ignoreCommentsCheckBox.isSelected());
            assertion.setConfiguration(assertion.createConfiguration());
            configureResult = true;
            configurationDialog.setVisible(false);
        }
    }

    public class CancelAction extends AbstractAction {
        public CancelAction() {
            super("Cancel");
        }

        public void actionPerformed(ActionEvent arg0) {
            configureResult = false;
            configurationDialog.setVisible(false);
        }
    }

    public class DeclareNamespacesFromCurrentAction extends AbstractAction {
        public DeclareNamespacesFromCurrentAction() {
            super("Declare");
            putValue(Action.SHORT_DESCRIPTION, "Add namespace declaration from current message to " + assertion.getQueryType() + " expression");
        }

        public void actionPerformed(ActionEvent arg0) {
            try {
                String content = assertion.getAssertable().getAssertableContentAsXml();
                if (content != null && content.trim().length() > 0) {
                    pathArea.setText(XmlUtils.declareXPathNamespaces(content) + pathArea.getText());
                } else if (UISupport.confirm("Declare namespaces from schema instead?", "Missing Response")) {
                    pathArea.setText(XmlUtils.declareXPathNamespaces((WsdlInterface) assertion.getAssertable().getInterface())
                            + pathArea.getText());
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    public class TestPathAction extends AbstractAction {
        public TestPathAction() {
            super("Test");
            putValue(Action.SHORT_DESCRIPTION,
                    "Tests the " + assertion.getQueryType() + " expression for the current message against the Expected Content field");
        }

        public void actionPerformed(ActionEvent arg0) {
            String oldPath = assertion.getPath();
            String oldContent = assertion.getExpectedContent();
            boolean oldAllowWildcards = assertion.isAllowWildcards();

            setAssertionParameters(pathArea.getText().trim(), contentArea.getText(), allowWildcardsCheckBox.isSelected());
            assertion.setIgnoreNamespaceDifferences(ignoreNamespaceDifferencesCheckBox.isSelected());
            assertion.setIgnoreComments(ignoreCommentsCheckBox.isSelected());

            try {
                String assertableContent = assertion.getAssertable().getAssertableContent();
                if (XPathContainsAssertion.ID.equals(assertion.getConfig().getType()) //Backward compatibility
                        || (!JsonUtil.seemsToBeJson(assertableContent))) {
                    assertableContent = assertion.getAssertable().getAssertableContentAsXml();
                }
                if (assertableContent == null) {
                    UISupport.showErrorMessage("Missing content!!");
                    setAssertionParameters(oldPath, oldContent, oldAllowWildcards);
                    return;
                }
                String msg = assertion.assertContent(assertableContent,
                        new WsdlTestRunContext(assertion.getAssertable().getTestStep()), "Response");
                UISupport.showInfoMessage(msg, "Success");
            } catch (AssertionException e) {
                UISupport.showErrorMessage(e.getMessage());
            }
            setAssertionParameters(oldPath, oldContent, oldAllowWildcards);
        }

        private void setAssertionParameters(String path, String content, boolean allowWildCards) {
            assertion.setPath(path);
            assertion.setExpectedContent(content);
            assertion.setAllowWildcards(allowWildCards);
        }
    }

    public class SelectFromCurrentAction extends AbstractAction {
        public SelectFromCurrentAction() {
            super("Select from current");
            putValue(Action.SHORT_DESCRIPTION,
                    "Selects the " + assertion.getQueryType() + " expression from the current message into the Expected Content field");
        }

        public void actionPerformed(ActionEvent arg0) {
            assertion.selectFromCurrent();
        }
    }
}
