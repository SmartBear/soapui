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
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.JTextArea;

/**
 * Assertion that matches a specified XQuery expression and its expected result
 * against the associated WsdlTestRequests response message
 *
 * @author Ole.Matzura
 */

public class XQueryContainsAssertion extends AbstractXmlContainsAssertion {

    public static final String ID = "XQuery Match";
    public static final String LABEL = "XQuery Match";
    public static final String DESCRIPTION = "Uses an XQuery expression to select content from the target property and compares the result to an expected value. Applicable to any property containing XML.";

    public XQueryContainsAssertion(TestAssertionConfig assertionConfig, Assertable assertable) {
        super(assertionConfig, assertable, true, true, true, true);
    }

    public String assertContent(String response, SubmitContext context, String type) throws AssertionException {
        try {
            if (path == null) {
                return "Missing path for XQuery Assertion";
            }
            if (expectedContent == null) {
                return "Missing content for XQuery Assertion";
            }
            
            XmlOptions options = new XmlOptions();
            if (ignoreComments) {
                options.setLoadStripComments();
            }

            // XmlObject xml = XmlObject.Factory.parse( response, options );
            XmlObject xml = XmlUtils.createXmlObject(response, options);
            String expandedPath = PropertyExpander.expandProperties(context, path);
            XmlObject[] items = xml.execQuery(expandedPath);

            XmlObject contentObj = null;
            String expandedContent = PropertyExpander.expandProperties(context, expectedContent);

            try {
                // contentObj = XmlObject.Factory.parse( expandedContent );
                contentObj = XmlUtils.createXmlObject(expandedContent, options);
            } catch (Exception e) {
                // this is ok.. it just means that the content to match is not xml
                // but
                // (hopefully) just a string
            	e.printStackTrace();
            }

            if (items.length == 0) {
                throw new Exception("Missing content for xquery [" + path + "] in " + type);
            }

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

        InternalDifferenceListener internalDifferenceListener = new InternalDifferenceListener();
        diff.overrideDifferenceListener(internalDifferenceListener);

        if (!diff.identical()) {
            throw new Exception(diff.toString());
        }
    }

    public String getHelpURL() {
        return HelpUrls.ASSERTION_XQUERY;
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
    
    protected  String getQueryType() {
    	return "XQuery";
    }    
}
