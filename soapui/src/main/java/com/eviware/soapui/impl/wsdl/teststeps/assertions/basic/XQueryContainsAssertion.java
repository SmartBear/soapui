/*
 * Copyright 2004-2014 SmartBear Software
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

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

import junit.framework.ComparisonFailure;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceEngine;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLAssert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.support.XPathReference;
import com.eviware.soapui.model.support.XPathReferenceContainer;
import com.eviware.soapui.model.support.XPathReferenceImpl;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.JUndoableTextArea;
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.soapui.support.xml.XmlUtils;
import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * Assertion that matches a specified XQuery expression and its expected result
 * against the associated WsdlTestRequests response message
 *
 * @author Ole.Matzura
 */

public class XQueryContainsAssertion extends WsdlMessageAssertion implements RequestAssertion, ResponseAssertion,
        XPathReferenceContainer {
    private final static Logger log = Logger.getLogger(XQueryContainsAssertion.class);
    private String expectedContent;
    private String path;
    private JDialog configurationDialog;
    private JTextArea pathArea;
    private JTextArea contentArea;
    private boolean configureResult;
    private boolean allowWildcards;

    public static final String ID = "XQuery Match";
    public static final String LABEL = "XQuery Match";
    public static final String DESCRIPTION = "Uses an XQuery expression to select content from the target property and compares the result to an expected value. Applicable to any property containing XML.";
    private JCheckBox allowWildcardsCheckBox;

    public XQueryContainsAssertion(TestAssertionConfig assertionConfig, Assertable assertable) {
        super(assertionConfig, assertable, true, true, true, true);

        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(getConfiguration());
        path = reader.readString("path", null);
        expectedContent = reader.readString("content", null);
        allowWildcards = reader.readBoolean("allowWildcards", false);
    }

    public String getExpectedContent() {
        return expectedContent;
    }

    public void setExpectedContent(String expectedContent) {
        this.expectedContent = expectedContent;
        setConfiguration(createConfiguration());
    }

    /**
     * @deprecated
     */

    public void setContent(String content) {
        setExpectedContent(content);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
        setConfiguration(createConfiguration());
    }

    public boolean isAllowWildcards() {
        return allowWildcards;
    }

    public void setAllowWildcards(boolean allowWildcards) {
        this.allowWildcards = allowWildcards;
    }

    protected String internalAssertResponse(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        if (!messageExchange.hasResponse()) {
            return "Missing Response";
        } else {
            return assertContent(messageExchange.getResponseContentAsXml(), context, "Response");
        }
    }

    protected String internalAssertProperty(TestPropertyHolder source, String propertyName,
                                            MessageExchange messageExchange, SubmitContext context) throws AssertionException {
        if (!XmlUtils.seemsToBeXml(source.getPropertyValue(propertyName))) {
            throw new AssertionException(new AssertionError("Property '" + propertyName
                    + "' has value which is not xml!"));
        }
        return assertContent(source.getPropertyValue(propertyName), context, propertyName);
    }

    public String assertContent(String response, SubmitContext context, String type) throws AssertionException {
        try {
            if (path == null) {
                return "Missing path for XQuery Assertion";
            }
            if (expectedContent == null) {
                return "Missing content for XQuery Assertion";
            }

            // XmlObject xml = XmlObject.Factory.parse( response );
            XmlObject xml = XmlUtils.createXmlObject(response);
            String expandedPath = PropertyExpander.expandProperties(context, path);
            XmlObject[] items = xml.execQuery(expandedPath);

            XmlObject contentObj = null;
            String expandedContent = PropertyExpander.expandProperties(context, expectedContent);

            try {
                // contentObj = XmlObject.Factory.parse( expandedContent );
                contentObj = XmlUtils.createXmlObject(expandedContent);
            } catch (Exception e) {
                // this is ok.. it just means that the content to match is not xml
                // but
                // (hopefully) just a string
            }

            if (items.length == 0) {
                throw new Exception("Missing content for xquery [" + path + "] in " + type);
            }

            XmlOptions options = new XmlOptions();
            options.setSavePrettyPrint();
            options.setSaveOuter();

            for (int c = 0; c < items.length; c++) {
                try {
                    if (contentObj == null) {
                        if (items[c] instanceof XmlAnySimpleType) {
                            String value = ((XmlAnySimpleType) items[c]).getStringValue();
                            String expandedValue = PropertyExpander.expandProperties(context, value);
                            XMLAssert.assertEquals(expandedContent, expandedValue);
                        } else {
                            Node domNode = items[c].getDomNode();
                            switch (domNode.getNodeType()) {
                                case Node.ELEMENT_NODE:
                                    String expandedValue = PropertyExpander.expandProperties(context,
                                            XmlUtils.getElementText((Element) domNode));
                                    if (allowWildcards) {
                                        Tools.assertSimilar(expandedContent, expandedValue, '*');
                                    } else {
                                        XMLAssert.assertEquals(expandedContent, expandedValue);
                                    }
                                    break;
                                case Node.DOCUMENT_NODE:
                                    expandedValue = PropertyExpander.expandProperties(context,
                                            XmlUtils.getElementText(((Document) domNode).getDocumentElement()));
                                    if (allowWildcards) {
                                        Tools.assertSimilar(expandedContent, expandedValue, '*');
                                    } else {
                                        XMLAssert.assertEquals(expandedContent, expandedValue);
                                    }
                                    break;
                                case Node.ATTRIBUTE_NODE:
                                    expandedValue = PropertyExpander.expandProperties(context, domNode.getNodeValue());
                                    if (allowWildcards) {
                                        Tools.assertSimilar(expandedContent, expandedValue, '*');
                                    } else {
                                        XMLAssert.assertEquals(expandedContent, expandedValue);
                                    }
                                    break;
                                default:
                                    expandedValue = PropertyExpander.expandProperties(context, domNode.getNodeValue());
                                    XMLAssert.assertEquals(expandedContent, expandedValue);
                                    break;
                            }
                        }
                    } else {
                        compareValues(contentObj.xmlText(options), items[c].xmlText(options));
                    }

                    break;
                } catch (Throwable e) {
                    if (c == items.length - 1) {
                        throw e;
                    }
                }
            }
        } catch (Throwable e) {
            String msg = "XQuery Match Assertion failed for path [" + path + "] : " + e.getClass().getSimpleName() + ":"
                    + e.getMessage();

            throw new AssertionException(new AssertionError(msg));
        }

        return type + " matches content for [" + path + "]";
    }

    private void compareValues(String expandedContent, String expandedValue) throws Exception {
        Diff diff = new Diff(expandedContent, expandedValue);
        diff.overrideDifferenceListener(new DifferenceListener() {

            public int differenceFound(Difference diff) {
                if (allowWildcards
                        && (diff.getId() == DifferenceEngine.TEXT_VALUE.getId() || diff.getId() == DifferenceEngine.ATTR_VALUE
                        .getId())) {
                    try {
                        Tools.assertSimilar(diff.getControlNodeDetail().getValue(), diff.getTestNodeDetail().getValue(), '*');
                        return Diff.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                    } catch (ComparisonFailure e) {
                        return Diff.RETURN_ACCEPT_DIFFERENCE;
                    }                    
                }

                return Diff.RETURN_ACCEPT_DIFFERENCE;
            }

            public void skippedComparison(Node arg0, Node arg1) {

            }
        });

        if (!diff.identical()) {
            throw new Exception(diff.toString());
        }
    }

    public boolean configure() {
        if (configurationDialog == null) {
            buildConfigurationDialog();
        }

        pathArea.setText(path);
        contentArea.setText(expectedContent);
        allowWildcardsCheckBox.setSelected(allowWildcards);

        UISupport.showDialog(configurationDialog);
        return configureResult;
    }


    public String getHelpURL() {
        return HelpUrls.ASSERTION_XQUERY;
    }

    protected void buildConfigurationDialog() {
        configurationDialog = new JDialog(UISupport.getMainFrame());
        configurationDialog.setTitle("XQuery Match configuration");
        configurationDialog.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent event) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        pathArea.requestFocusInWindow();
                    }
                });
            }
        });

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(UISupport.buildDescription("Specify XQuery expression and expected result",
                "declare namespaces with <code>declare namespace &lt;prefix&gt;='&lt;namespace&gt;';</code>", null),
                BorderLayout.NORTH);

        JSplitPane splitPane = UISupport.createVerticalSplit();

        JPanel pathPanel = new JPanel(new BorderLayout());
        JXToolBar pathToolbar = UISupport.createToolbar();
        addPathEditorActions(pathToolbar);

        pathArea = new JUndoableTextArea();
        pathArea.setToolTipText("Specifies the XQuery expression to select from the message for validation");

        pathPanel.add(pathToolbar, BorderLayout.NORTH);
        pathPanel.add(new JScrollPane(pathArea), BorderLayout.CENTER);

        splitPane.setTopComponent(UISupport.addTitledBorder(pathPanel, "XQuery Expression"));

        JPanel matchPanel = new JPanel(new BorderLayout());
        JXToolBar contentToolbar = UISupport.createToolbar();
        addMatchEditorActions(contentToolbar);

        contentArea = new JUndoableTextArea();
        contentArea.setToolTipText("Specifies the expected result of the XQuery expression");

        matchPanel.add(contentToolbar, BorderLayout.NORTH);
        matchPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        splitPane.setBottomComponent(UISupport.addTitledBorder(matchPanel, "Expected Result"));
        splitPane.setDividerLocation(150);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));

        contentPanel.add(splitPane, BorderLayout.CENTER);

        ButtonBarBuilder builder = new ButtonBarBuilder();

        ShowOnlineHelpAction showOnlineHelpAction = new ShowOnlineHelpAction(this.getHelpURL());
        builder.addFixed(UISupport.createToolbarButton(showOnlineHelpAction));
        builder.addGlue();

        JButton okButton = new JButton(new OkAction());
        builder.addFixed(okButton);
        builder.addRelatedGap();
        builder.addFixed(new JButton(new CancelAction()));

        builder.setBorder(BorderFactory.createEmptyBorder(1, 5, 5, 5));

        contentPanel.add(builder.getPanel(), BorderLayout.SOUTH);

        configurationDialog.setContentPane(contentPanel);
        configurationDialog.setSize(600, 500);
        configurationDialog.setModal(true);
        UISupport.initDialogActions(configurationDialog, showOnlineHelpAction, okButton);
    }

    protected void addPathEditorActions(JXToolBar toolbar) {
        toolbar.addFixed(new JButton(new DeclareNamespacesFromCurrentAction()));
    }

    protected void addMatchEditorActions(JXToolBar toolbar) {
        toolbar.addFixed(new JButton(new SelectFromCurrentAction()));
        toolbar.addRelatedGap();
        toolbar.addFixed(new JButton(new TestPathAction()));
        allowWildcardsCheckBox = new JCheckBox("Allow Wildcards");

        Dimension dim = new Dimension(100, 20);

        allowWildcardsCheckBox.setSize(dim);
        allowWildcardsCheckBox.setPreferredSize(dim);

        allowWildcardsCheckBox.setOpaque(false);
        toolbar.addRelatedGap();
        toolbar.addFixed(allowWildcardsCheckBox);
    }

    public XmlObject createConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        builder.add("path", path);
        builder.add("content", expectedContent);
        builder.add("allowWildcards", allowWildcards);
        return builder.finish();
    }

    public void selectFromCurrent() {
        // XmlCursor cursor = null;

        try {
            XmlOptions options = new XmlOptions();
            options.setSavePrettyPrint();
            options.setSaveAggressiveNamespaces();

            String assertableContent = getAssertable().getAssertableContentAsXml();
            if (assertableContent == null || assertableContent.trim().length() == 0) {
                UISupport.showErrorMessage("Missing content to select from");
                return;
            }

            // XmlObject xml = XmlObject.Factory.parse( assertableContent );
            XmlObject xml = XmlUtils.createXmlObject(assertableContent);

            String txt = pathArea == null || !pathArea.isVisible() ? getPath() : pathArea.getSelectedText();
            if (txt == null) {
                txt = pathArea == null ? "" : pathArea.getText();
            }

            WsdlTestRunContext context = new WsdlTestRunContext(getAssertable().getTestStep());

            String expandedPath = PropertyExpander.expandProperties(context, txt.trim());

            if (contentArea != null && contentArea.isVisible()) {
                contentArea.setText("");
            }

            XmlObject[] paths = xml.execQuery(expandedPath);
            if (paths.length == 0) {
                UISupport.showErrorMessage("No match in current response");
            } else if (paths.length > 1) {
                UISupport.showErrorMessage("More than one match in current response");
            } else {
                Node domNode = paths[0].getDomNode();
                String stringValue = null;

                if (domNode.getNodeType() == Node.ATTRIBUTE_NODE || domNode.getNodeType() == Node.TEXT_NODE) {
                    stringValue = domNode.getNodeValue();
                } else {
                    if (domNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element elm = (Element) domNode;
                        if (elm.getChildNodes().getLength() == 1 && elm.getAttributes().getLength() == 0) {
                            stringValue = XmlUtils.getElementText(elm);
                        } else {
                            stringValue = paths[0].xmlText(options);
                        }
                    } else {
                        stringValue = paths[0].xmlText(options);
                    }
                }

                if (contentArea != null && contentArea.isVisible()) {
                    contentArea.setText(stringValue);
                } else {
                    setContent(stringValue);
                }
            }
        } catch (Throwable e) {
            UISupport.showErrorMessage("Invalid XQuery expression.");
            SoapUI.logError(e);
        } finally {
            // if( cursor != null )
            // cursor.dispose();
        }
    }

    public class OkAction extends AbstractAction {
        public OkAction() {
            super("Save");
        }

        public void actionPerformed(ActionEvent arg0) {
            setPath(pathArea.getText().trim());
            setContent(contentArea.getText());
            setAllowWildcards(allowWildcardsCheckBox.isSelected());
            setConfiguration(createConfiguration());
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
            putValue(Action.SHORT_DESCRIPTION, "Add namespace declaration from current message to XQuery expression");
        }

        public void actionPerformed(ActionEvent arg0) {
            try {
                String content = getAssertable().getAssertableContentAsXml();
                if (content != null && content.trim().length() > 0) {
                    pathArea.setText(XmlUtils.declareXPathNamespaces(content) + pathArea.getText());
                } else if (UISupport.confirm("Declare namespaces from schema instead?", "Missing Response")) {
                    pathArea.setText(XmlUtils.declareXPathNamespaces((WsdlInterface) getAssertable().getInterface())
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
                    "Tests the XQuery expression for the current message against the Expected Content field");
        }

        public void actionPerformed(ActionEvent arg0) {
            String oldPath = getPath();
            String oldContent = getExpectedContent();
            boolean oldAllowWildcards = isAllowWildcards();

            setPath(pathArea.getText().trim());
            setContent(contentArea.getText());
            setAllowWildcards(allowWildcardsCheckBox.isSelected());

            try {
                String msg = assertContent(getAssertable().getAssertableContentAsXml(), new WsdlTestRunContext(getAssertable()
                        .getTestStep()), "Response");
                UISupport.showInfoMessage(msg, "Success");
            } catch (AssertionException e) {
                UISupport.showErrorMessage(e.getMessage());
            }

            setPath(oldPath);
            setContent(oldContent);
            setAllowWildcards(oldAllowWildcards);
        }
    }

    public class SelectFromCurrentAction extends AbstractAction {
        public SelectFromCurrentAction() {
            super("Select from current");
            putValue(Action.SHORT_DESCRIPTION,
                    "Selects the XQuery expression from the current message into the Expected Content field");
        }

        public void actionPerformed(ActionEvent arg0) {
            selectFromCurrent();
        }
    }

    @Override
    protected String internalAssertRequest(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        if (!messageExchange.hasRequest(true)) {
            return "Missing Request";
        } else {
            return assertContent(messageExchange.getRequestContent(), context, "Request");
        }
    }

    public JTextArea getContentArea() {
        return contentArea;
    }

    public JTextArea getPathArea() {
        return pathArea;
    }

    public PropertyExpansion[] getPropertyExpansions() {
        List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();

        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(getAssertable().getModelItem(), this,
                "expectedContent"));
        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(getAssertable().getModelItem(), this, "path"));

        return result.toArray(new PropertyExpansion[result.size()]);
    }

    public XPathReference[] getXPathReferences() {
        List<XPathReference> result = new ArrayList<XPathReference>();

        if (StringUtils.hasContent(getPath())) {
            TestModelItem testStep = getAssertable().getTestStep();
            TestProperty property = testStep instanceof WsdlTestRequestStep ? testStep.getProperty("Response")
                    : testStep.getProperty("Request");
            result.add(new XPathReferenceImpl("XQuery for " + getName() + " XQueryContainsAssertion in "
                    + testStep.getName(), property, this, "path"));
        }

        return result.toArray(new XPathReference[result.size()]);
    }

    public static class Factory extends AbstractTestAssertionFactory {
        public Factory() {
            super(XQueryContainsAssertion.ID, XQueryContainsAssertion.LABEL, XQueryContainsAssertion.class);
        }

        @Override
        public String getCategory() {
            return AssertionCategoryMapping.VALIDATE_RESPONSE_CONTENT_CATEGORY;
        }

        @Override
        public Class<? extends WsdlMessageAssertion> getAssertionClassType() {
            return XQueryContainsAssertion.class;
        }

        @Override
        public AssertionListEntry getAssertionListEntry() {
            return new AssertionListEntry(XQueryContainsAssertion.ID, XQueryContainsAssertion.LABEL,
                    XQueryContainsAssertion.DESCRIPTION);
        }

        @Override
        public boolean canAssert(TestPropertyHolder modelItem, String property) {
            if (!modelItem.getProperty(property).getSchemaType().isPrimitiveType()) {
                return true;
            }

            String content = modelItem.getPropertyValue(property);
            return XmlUtils.seemsToBeXml(content);
        }

    }
}
