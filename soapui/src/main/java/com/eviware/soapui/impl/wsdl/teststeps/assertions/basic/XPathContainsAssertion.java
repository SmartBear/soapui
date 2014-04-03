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
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlQName;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceEngine;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLAssert;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathImpl;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathsContainer;
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
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.soapui.support.xml.XmlUtils;
import com.jgoodies.forms.builder.ButtonBarBuilder;

/**
 * Assertion that matches a specified XPath expression and its expected result
 * against the associated WsdlTestRequests response message
 *
 * @author Ole.Matzura
 */

public class XPathContainsAssertion extends WsdlMessageAssertion implements RequestAssertion, ResponseAssertion,
        XPathReferenceContainer {
    private final static Logger log = Logger.getLogger(XPathContainsAssertion.class);
    private String expectedContent;
    private String path;
    private JDialog configurationDialog;
    private JTextArea pathArea;
    private JTextArea contentArea;
    private boolean configureResult;
    private boolean allowWildcards;
    private boolean ignoreNamespaceDifferences;
    private boolean ignoreComments;

    public static final String ID = "XPath Match";
    public static final String LABEL = "XPath Match";
    public static final String DESCRIPTION = "Uses an XPath expression to select content from the target property and compares the result to an expected value. Applicable to any property containing XML.";
    private JCheckBox allowWildcardsCheckBox;
    private JCheckBox ignoreNamespaceDifferencesCheckBox;
    private JCheckBox ignoreCommentsCheckBox;

    public XPathContainsAssertion(TestAssertionConfig assertionConfig, Assertable assertable) {
        super(assertionConfig, assertable, true, true, true, true);

        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(getConfiguration());
        path = reader.readString("path", null);
        expectedContent = reader.readString("content", null);
        allowWildcards = reader.readBoolean("allowWildcards", false);
        ignoreNamespaceDifferences = reader.readBoolean("ignoreNamspaceDifferences", false);
        ignoreComments = reader.readBoolean("ignoreComments", false);
    }

    public String getExpectedContent() {
        return expectedContent;
    }

    public void setExpectedContent(String expectedContent) {
        setExpectedContent(expectedContent, true);
    }

    /**
     * @deprecated
     */

    @Deprecated
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
        setConfiguration(createConfiguration());
    }

    public boolean isIgnoreNamespaceDifferences() {
        return ignoreNamespaceDifferences;
    }

    public void setIgnoreNamespaceDifferences(boolean ignoreNamespaceDifferences) {
        this.ignoreNamespaceDifferences = ignoreNamespaceDifferences;
        setConfiguration(createConfiguration());
    }

    public boolean isIgnoreComments() {
        return ignoreComments;
    }

    public void setIgnoreComments(boolean ignoreComments) {
        this.ignoreComments = ignoreComments;
        setConfiguration(createConfiguration());
    }

    @Override
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
                return "Missing path for XPath assertion";
            }
            if (expectedContent == null) {
                return "Missing content for XPath assertion";
            }

            XmlOptions options = new XmlOptions();
            if (ignoreComments) {
                options.setLoadStripComments();
            }

            // XmlObject xml = XmlObject.Factory.parse( response, options );
            XmlObject xml = XmlUtils.createXmlObject(response, options);
            String expandedPath = PropertyExpander.expandProperties(context, path);
            XmlObject[] items = xml.selectPath(expandedPath);
            AssertedXPathsContainer assertedXPathsContainer = (AssertedXPathsContainer) context
                    .getProperty(AssertedXPathsContainer.ASSERTEDXPATHSCONTAINER_PROPERTY);

            XmlObject contentObj = null;
            String expandedContent = PropertyExpander.expandProperties(context, expectedContent);

            // stupid check for text selection for those situation that the
            // selected
            // text actually contains xml which should be compared as a string.
            if (!expandedPath.endsWith("text()")) {
                try {
                    // contentObj = XmlObject.Factory.parse( expandedContent, options
                    // );
                    contentObj = XmlUtils.createXmlObject(expandedContent, options);
                } catch (Exception e) {
                    // this is ok.. it just means that the content to match is not
                    // xml
                    // but
                    // (hopefully) just a string
                }
            }

            if (items.length == 0) {
                throw new Exception("Missing content for xpath [" + path + "] in " + type);
            }

            options.setSavePrettyPrint();
            options.setSaveOuter();

            for (int c = 0; c < items.length; c++) {
                try {
                    AssertedXPathImpl assertedXPathImpl = null;
                    if (assertedXPathsContainer != null) {
                        String xpath = XmlUtils.createAbsoluteXPath(items[c].getDomNode());
                        if (xpath != null) {
                            XmlObject xmlObj = items[c];
                            assertedXPathImpl = new AssertedXPathImpl(this, xpath, xmlObj);
                            assertedXPathsContainer.addAssertedXPath(assertedXPathImpl);
                        }
                    }

                    if (contentObj == null) {
                        if (items[c] instanceof XmlAnySimpleType && !(items[c] instanceof XmlQName)) {
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
                        compareValues(contentObj.xmlText(options), items[c].xmlText(options), items[c]);
                    }

                    break;
                } catch (Throwable e) {
                    if (c == items.length - 1) {
                        throw e;
                    }
                }
            }
        } catch (Throwable e) {
            String msg = "";

            if (e instanceof ComparisonFailure) {
                ComparisonFailure cf = (ComparisonFailure) e;
                String expected = cf.getExpected();
                String actual = cf.getActual();

                // if( expected.length() > ERROR_LENGTH_LIMIT )
                // expected = expected.substring(0, ERROR_LENGTH_LIMIT) + "..";
                //
                // if( actual.length() > ERROR_LENGTH_LIMIT )
                // actual = actual.substring(0, ERROR_LENGTH_LIMIT) + "..";

                msg = "XPathContains comparison failed for path [" + path + "], expecting [" + expected + "], actual was [" + actual + "]";
            } else {
                msg = "XPathContains assertion failed for path [" + path + "] : " + e.getClass().getSimpleName() + ":"
                        + e.getMessage();
            }

            throw new AssertionException(new AssertionError(msg));
        }

        return type + " matches content for [" + path + "]";
    }

    private void compareValues(String expandedContent, String expandedValue, XmlObject object) throws Exception {
        Diff diff = new Diff(expandedContent, expandedValue);
        InternalDifferenceListener internalDifferenceListener = new InternalDifferenceListener();
        diff.overrideDifferenceListener(internalDifferenceListener);

        if (!diff.identical()) {
            throw new Exception(diff.toString());
        }

        StringList nodesToRemove = internalDifferenceListener.getNodesToRemove();

        if (!nodesToRemove.isEmpty()) {
            for (String node : nodesToRemove) {
                if (node == null) {
                    continue;
                }

                int ix = node.indexOf("\n/");
                if (ix != -1) {
                    node = node.substring(0, ix + 1) + "./" + node.substring(ix + 1);
                } else if (node.startsWith("/")) {
                    node = "/" + node;
                }

                XmlObject[] paths = object.selectPath(node);
                if (paths.length > 0) {
                    Node domNode = paths[0].getDomNode();
                    if (domNode.getNodeType() == Node.ATTRIBUTE_NODE) {
                        ((Attr) domNode).getOwnerElement().removeAttributeNode((Attr) domNode);
                    } else {
                        domNode.getParentNode().removeChild(domNode);
                    }

                    try {
                        object.set(object.copy());
                    } catch (XmlValueDisconnectedException e) {
                        // this means that we've excluded the root note.. it's ok..
                        return;
                    }
                }
            }
        }
    }

    @Override
    public boolean configure() {
        if (configurationDialog == null) {
            buildConfigurationDialog();
        }

        pathArea.setText(path);
        contentArea.setText(expectedContent);
        allowWildcardsCheckBox.setSelected(allowWildcards);
        ignoreNamespaceDifferencesCheckBox.setSelected(ignoreNamespaceDifferences);

        UISupport.showDialog(configurationDialog);
        return configureResult;
    }

    protected void buildConfigurationDialog() {
        configurationDialog = new JDialog(UISupport.getMainFrame());
        configurationDialog.setTitle("XPath Match Configuration");
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
        contentPanel.add(UISupport.buildDescription("Specify xpath expression and expected result",
                "declare namespaces with <code>declare namespace &lt;prefix&gt;='&lt;namespace&gt;';</code>", null),
                BorderLayout.NORTH);

        JSplitPane splitPane = UISupport.createVerticalSplit();

        pathArea = new JUndoableTextArea();
        pathArea.setToolTipText("Specifies the XPath expression to select from the message for validation");

        JPanel pathPanel = new JPanel(new BorderLayout());
        JXToolBar pathToolbar = UISupport.createToolbar();
        addPathEditorActions(pathToolbar);

        pathPanel.add(pathToolbar, BorderLayout.NORTH);
        pathPanel.add(new JScrollPane(pathArea), BorderLayout.CENTER);

        splitPane.setTopComponent(UISupport.addTitledBorder(pathPanel, "XPath Expression"));

        contentArea = new JUndoableTextArea();
        contentArea.setToolTipText("Specifies the expected result of the XPath expression");

        JPanel matchPanel = new JPanel(new BorderLayout());
        JXToolBar contentToolbar = UISupport.createToolbar();
        addMatchEditorActions(contentToolbar);

        matchPanel.add(contentToolbar, BorderLayout.NORTH);
        matchPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        splitPane.setBottomComponent(UISupport.addTitledBorder(matchPanel, "Expected Result"));
        splitPane.setDividerLocation(150);
        splitPane.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 1));

        contentPanel.add(splitPane, BorderLayout.CENTER);

        ButtonBarBuilder builder = new ButtonBarBuilder();

        ShowOnlineHelpAction showOnlineHelpAction = new ShowOnlineHelpAction(HelpUrls.XPATHASSERTIONEDITOR_HELP_URL);
        builder.addFixed(UISupport.createToolbarButton(showOnlineHelpAction));
        builder.addGlue();

        JButton okButton = new JButton(new OkAction());
        builder.addFixed(okButton);
        builder.addRelatedGap();
        builder.addFixed(new JButton(new CancelAction()));

        builder.setBorder(BorderFactory.createEmptyBorder(1, 5, 5, 5));

        contentPanel.add(builder.getPanel(), BorderLayout.SOUTH);

        configurationDialog.setContentPane(contentPanel);
        configurationDialog.setSize(800, 500);
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

        Dimension dim = new Dimension(120, 20);

        allowWildcardsCheckBox.setSize(dim);
        allowWildcardsCheckBox.setPreferredSize(dim);

        allowWildcardsCheckBox.setOpaque(false);
        toolbar.addRelatedGap();
        toolbar.addFixed(allowWildcardsCheckBox);

        Dimension largerDim = new Dimension(170, 20);
        ignoreNamespaceDifferencesCheckBox = new JCheckBox("Ignore namespace prefixes");
        ignoreNamespaceDifferencesCheckBox.setSize(largerDim);
        ignoreNamespaceDifferencesCheckBox.setPreferredSize(largerDim);
        ignoreNamespaceDifferencesCheckBox.setOpaque(false);
        toolbar.addRelatedGap();
        toolbar.addFixed(ignoreNamespaceDifferencesCheckBox);

        ignoreCommentsCheckBox = new JCheckBox("Ignore XML Comments");
        ignoreCommentsCheckBox.setSize(largerDim);
        ignoreCommentsCheckBox.setPreferredSize(largerDim);
        ignoreCommentsCheckBox.setOpaque(false);
        toolbar.addRelatedGap();
        toolbar.addFixed(ignoreCommentsCheckBox);
    }

    public XmlObject createConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        builder.add("path", path);
        builder.add("content", expectedContent);
        builder.add("allowWildcards", allowWildcards);
        builder.add("ignoreNamspaceDifferences", ignoreNamespaceDifferences);
        builder.add("ignoreComments", ignoreComments);
        return builder.finish();
    }

    public void selectFromCurrent() {
        XmlCursor cursor = null;

        try {
            String assertableContent = getAssertable().getAssertableContent();
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

            cursor = xml.newCursor();
            cursor.selectPath(expandedPath);
            if (!cursor.toNextSelection()) {
                UISupport.showErrorMessage("No match in current response");
            } else if (cursor.hasNextSelection()) {
                UISupport.showErrorMessage("More than one match in current response");
            } else {
                String stringValue = XmlUtils.getValueForMatch(cursor);

                if (contentArea != null && contentArea.isVisible()) {
                    contentArea.setText(stringValue);
                } else {
                    setExpectedContent(stringValue, false);
                }
            }
        } catch (Throwable e) {
            UISupport.showErrorMessage("Invalid XPath expression.");
            SoapUI.logError(e);
        } finally {
            if (cursor != null) {
                cursor.dispose();
            }
        }
    }

    private void setExpectedContent(String expectedContent, boolean save) {
        this.expectedContent = expectedContent;
        if (save) {
            setConfiguration(createConfiguration());
        }
    }

    private final class InternalDifferenceListener implements DifferenceListener {
        private StringList nodesToRemove = new StringList();

        public int differenceFound(Difference diff) {
            if (allowWildcards
                    && (diff.getId() == DifferenceEngine.TEXT_VALUE.getId() || diff.getId() == DifferenceEngine.ATTR_VALUE
                    .getId())) {
                if (diff.getControlNodeDetail().getValue().equals("*")) {
                    Node node = diff.getTestNodeDetail().getNode();
                    String xp = XmlUtils.createAbsoluteXPath(node.getNodeType() == Node.ATTRIBUTE_NODE ? node : node
                            .getParentNode());
                    nodesToRemove.add(xp);
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
            } else if (allowWildcards && diff.getId() == DifferenceEngine.NODE_TYPE.getId()) {
                if (diff.getControlNodeDetail().getNode().getNodeValue().equals("*")) {
                    Node node = diff.getTestNodeDetail().getNode();
                    String xp = XmlUtils.createAbsoluteXPath(node.getNodeType() == Node.ATTRIBUTE_NODE ? node : node
                            .getParentNode());
                    nodesToRemove.add(xp);
                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
                }
            } else if (ignoreNamespaceDifferences && diff.getId() == DifferenceEngine.NAMESPACE_PREFIX_ID) {
                return Diff.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            }

            return Diff.RETURN_ACCEPT_DIFFERENCE;
        }

        public void skippedComparison(Node arg0, Node arg1) {

        }

        public StringList getNodesToRemove() {
            return nodesToRemove;
        }
    }

    public class OkAction extends AbstractAction {
        public OkAction() {
            super("Save");
        }

        public void actionPerformed(ActionEvent arg0) {
            setPath(pathArea.getText().trim());
            setExpectedContent(contentArea.getText());
            setAllowWildcards(allowWildcardsCheckBox.isSelected());
            setIgnoreNamespaceDifferences(ignoreNamespaceDifferencesCheckBox.isSelected());
            setIgnoreComments(ignoreCommentsCheckBox.isSelected());
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
            putValue(Action.SHORT_DESCRIPTION, "Add namespace declaration from current message to XPath expression");
        }

        public void actionPerformed(ActionEvent arg0) {
            try {
                String content = getAssertable().getAssertableContent();
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
                    "Tests the XPath expression for the current message against the Expected Content field");
        }

        public void actionPerformed(ActionEvent arg0) {
            String oldPath = getPath();
            String oldContent = getExpectedContent();
            boolean oldAllowWildcards = isAllowWildcards();

            setPath(pathArea.getText().trim());
            setExpectedContent(contentArea.getText());
            setAllowWildcards(allowWildcardsCheckBox.isSelected());
            setIgnoreNamespaceDifferences(ignoreNamespaceDifferencesCheckBox.isSelected());
            setIgnoreComments(ignoreCommentsCheckBox.isSelected());

            try {
                String msg = assertContent(getAssertable().getAssertableContent(), new WsdlTestRunContext(getAssertable()
                        .getTestStep()), "Response");
                UISupport.showInfoMessage(msg, "Success");
            } catch (AssertionException e) {
                UISupport.showErrorMessage(e.getMessage());
            }

            setPath(oldPath);
            setExpectedContent(oldContent);
            setAllowWildcards(oldAllowWildcards);
        }
    }

    public class SelectFromCurrentAction extends AbstractAction {
        public SelectFromCurrentAction() {
            super("Select from current");
            putValue(Action.SHORT_DESCRIPTION,
                    "Selects the XPath expression from the current message into the Expected Content field");
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

    @Override
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
            result.add(new XPathReferenceImpl("XPath for " + getName() + " XPathContainsAssertion in "
                    + testStep.getName(), property, this, "path"));
        }

        return result.toArray(new XPathReference[result.size()]);
    }

    public static class Factory extends AbstractTestAssertionFactory {
        public Factory() {
            super(XPathContainsAssertion.ID, XPathContainsAssertion.LABEL, XPathContainsAssertion.class);
        }

        @Override
        public String getCategory() {
            return AssertionCategoryMapping.VALIDATE_RESPONSE_CONTENT_CATEGORY;
        }

        @Override
        public Class<? extends WsdlMessageAssertion> getAssertionClassType() {
            return XPathContainsAssertion.class;
        }

        @Override
        public AssertionListEntry getAssertionListEntry() {
            return new AssertionListEntry(XPathContainsAssertion.ID, XPathContainsAssertion.LABEL,
                    XPathContainsAssertion.DESCRIPTION);
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
