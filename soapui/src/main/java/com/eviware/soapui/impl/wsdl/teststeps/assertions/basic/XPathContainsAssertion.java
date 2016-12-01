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

package com.eviware.soapui.impl.wsdl.teststeps.assertions.basic;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathImpl;
import com.eviware.soapui.impl.wsdl.support.assertions.AssertedXPathsContainer;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.xml.XmlUtils;
import junit.framework.ComparisonFailure;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlQName;
import org.apache.xmlbeans.impl.values.XmlValueDisconnectedException;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.JTextArea;

/**
 * Assertion that matches a specified XPath expression and its expected result
 * against the associated WsdlTestRequests response message
 *
 * @author Ole.Matzura
 */

public class XPathContainsAssertion extends AbstractXmlContainsAssertion {

    public static final String ID = "XPath Match";
    public static final String LABEL = "XPath Match";
    public static final String DESCRIPTION = "Uses an XPath expression to select content from the target property and compares the result to an expected value. Applicable to any property containing XML.";

    public XPathContainsAssertion(TestAssertionConfig assertionConfig, Assertable assertable) {
        super(assertionConfig, assertable, true, true, true, true);
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


    public String getHelpURL() {
        return HelpUrls.ASSERTION_XPATH_CONTENT;
    }


    public void selectFromCurrent() {
        XmlCursor cursor = null;

        try {
            String assertableContent = getAssertable().getAssertableContentAsXml();
            if (assertableContent == null || assertableContent.trim().length() == 0) {
                UISupport.showErrorMessage("Missing content to select from");
                return;
            }


            JTextArea pathArea = getPathArea();
            String txt = pathArea == null || !pathArea.isVisible() ? getPath() : pathArea.getSelectedText();
            if (txt == null) {
                txt = pathArea == null ? "" : pathArea.getText();
            }

            WsdlTestRunContext context = new WsdlTestRunContext(getAssertable().getTestStep());

            String expandedPath = PropertyExpander.expandProperties(context, txt.trim());

            JTextArea contentArea = getContentArea();
            if (contentArea != null && contentArea.isVisible()) {
                contentArea.setText("");
            }

            // XmlObject xml = XmlObject.Factory.parse( assertableContent );
            XmlObject xml = XmlUtils.createXmlObject(assertableContent);
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
    
    protected  String getQueryType() {
    	return "XPath";
    }
}
