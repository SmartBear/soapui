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

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.model.TestModelItem;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
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
import com.eviware.soapui.support.components.JXToolBar;
import com.eviware.soapui.support.types.StringList;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.soapui.support.xml.XmlUtils;
import org.apache.xmlbeans.XmlObject;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceEngine;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

import javax.swing.JTextArea;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractXmlContainsAssertion extends WsdlMessageAssertion implements RequestAssertion, ResponseAssertion,
XPathReferenceContainer{

    protected String expectedContent;
    protected String path;

    protected boolean allowWildcards;
    protected boolean ignoreNamespaceDifferences;
    protected boolean ignoreComments;
    
    protected AssertionConfigurationDialog configurationDialog;
    
	protected AbstractXmlContainsAssertion(TestAssertionConfig assertionConfig,
			Assertable modelItem, boolean cloneable, boolean configurable,
			boolean multiple, boolean requiresResponseContent) {
		super(assertionConfig, modelItem, cloneable, configurable, multiple,
				requiresResponseContent);

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

	    protected void setExpectedContent(String expectedContent, boolean save) {
	        this.expectedContent = expectedContent;
	        if (save) {
	            setConfiguration(createConfiguration());
	        }
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
	    
	    public XmlObject createConfiguration() {
	        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
	        addConfigurationValues(builder);
	        return builder.finish();
	    }

	    protected void addConfigurationValues(XmlObjectConfigurationBuilder builder) {
	        builder.add("path", path);
	        builder.add("content", expectedContent);
	        builder.add("allowWildcards", allowWildcards);
	        builder.add("ignoreNamspaceDifferences", ignoreNamespaceDifferences);
	        builder.add("ignoreComments", ignoreComments);
	    }

	    protected JTextArea getPathArea() {
	        return configurationDialog == null ? null : configurationDialog.getPathArea();
	    }

	    protected JTextArea getContentArea() {
	        return configurationDialog == null ? null : configurationDialog.getContentArea();
	    }

	    @Override
	    public boolean configure() {
	        if (configurationDialog == null) {
	            configurationDialog = new AssertionConfigurationDialog(getAssertion());
	        }

	        return configurationDialog.configure();
	    }
	    
	    protected AbstractXmlContainsAssertion getAssertion() {
	        return this;
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
	    
	    public abstract String assertContent(String response, SubmitContext context, String type) throws AssertionException;
	    
	    protected String internalAssertProperty(TestPropertyHolder source, String propertyName,
                MessageExchange messageExchange, SubmitContext context) throws AssertionException {
			if (!XmlUtils.seemsToBeXml(source.getPropertyValue(propertyName))) {
				throw new AssertionException(new AssertionError("Property '" + propertyName
				+ "' has value which is not xml!"));
			}
			return assertContent(source.getPropertyValue(propertyName), context, propertyName);
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
	    
	    @Override
	    public PropertyExpansion[] getPropertyExpansions() {
	        List<PropertyExpansion> result = new ArrayList<PropertyExpansion>();

	        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(getAssertable().getModelItem(), this,
	                "expectedContent"));
	        result.addAll(PropertyExpansionUtils.extractPropertyExpansions(getAssertable().getModelItem(), this, "path"));

	        return result.toArray(new PropertyExpansion[result.size()]);
	    }
	    
	    public String getPathAreaTitle() {
	        return "Specify " + getQueryType() + " expression and expected result";
	    }

	    public String getPathAreaDescription() {
	        return "declare namespaces with <code>declare namespace &lt;prefix&gt;='&lt;namespace&gt;';</code>";
	    }



	    public String getPathAreaToolTipText() {
	        return "Specifies the " + getQueryType() + " expression to select from the message for validation";
	    }

	    public String getPathAreaBorderTitle() {
	        return getQueryType() + " Expression";
	    }

	    public String getContentAreaToolTipText() {
	        return "Specifies the expected result of the " + getQueryType() + " expression";
	    }

	    public String getConfigurationDialogTitle() {
	        return getQueryType() + " Match Configuration";
	    }

	    public String getContentAreaBorderTitle() {
	        return "Expected Result";
	    }

	    public boolean canAssertXmlContent() {
	        return true;
	    }
	    
	    protected void addMatchEditorActions(JXToolBar toolbar) {
	        configurationDialog.addMatchEditorActions(toolbar);
	    }
	    
	    protected void addPathEditorActions(JXToolBar toolbar) {
	        configurationDialog.addDeclareNamespaceButton(toolbar);
	    }
	    
	    public abstract void selectFromCurrent();
	    
	    protected abstract String getQueryType();
	    
	    public XPathReference[] getXPathReferences() {
	        List<XPathReference> result = new ArrayList<XPathReference>();

	        if (StringUtils.hasContent(getPath())) {
	            TestModelItem testStep = getAssertable().getTestStep();
	            TestProperty property = testStep instanceof WsdlTestRequestStep ? testStep.getProperty("Response")
	                    : testStep.getProperty("Request");
	            result.add(new XPathReferenceImpl(getQueryType() + " for " + getName() + " " + getQueryType() + "ContainsAssertion in "
	                    + testStep.getName(), property, this, "path"));
	        }

	        return result.toArray(new XPathReference[result.size()]);
	    }
	    
	    protected final class InternalDifferenceListener implements DifferenceListener {
	        private StringList nodesToRemove = new StringList();
	 
	        public int differenceFound(Difference diff) {
	            if (allowWildcards
	                    && (diff.getId() == DifferenceEngine.TEXT_VALUE.getId()
	                    || diff.getId() == DifferenceEngine.ATTR_VALUE.getId())) {
	                if (Tools.isSimilar(diff.getControlNodeDetail().getValue(), diff.getTestNodeDetail().getValue(), '*')) {
	                    addToNodesToRemove(diff);
	                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
	                }
	            } else if (allowWildcards && diff.getId() == DifferenceEngine.NODE_TYPE.getId()) {
	                if (Tools.isSimilar(diff.getControlNodeDetail().getNode().getNodeValue(), diff.getTestNodeDetail().getNode().getNodeValue(), '*')) {
	                    addToNodesToRemove(diff);
	                    return Diff.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
	                }
	            } else if (ignoreNamespaceDifferences && diff.getId() == DifferenceEngine.NAMESPACE_PREFIX_ID) {
	                return Diff.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
	            }

	            return Diff.RETURN_ACCEPT_DIFFERENCE;
	        }

	        private void addToNodesToRemove(Difference diff) {
	            Node node = diff.getTestNodeDetail().getNode();
	            String xp = XmlUtils.createAbsoluteXPath(node.getNodeType() == Node.ATTRIBUTE_NODE ? node : node
	                    .getParentNode());
	            nodesToRemove.add(xp);

	        }

	        public void skippedComparison(Node arg0, Node arg1) {

	        }

	        public StringList getNodesToRemove() {
	            return nodesToRemove;
	        }
	    }
}
