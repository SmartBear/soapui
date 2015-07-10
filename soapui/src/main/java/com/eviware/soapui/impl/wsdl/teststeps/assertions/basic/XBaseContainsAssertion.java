package com.eviware.soapui.impl.wsdl.teststeps.assertions.basic;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansion;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.model.support.XPathReferenceContainer;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.soapui.support.xml.XmlUtils;

public abstract class XBaseContainsAssertion extends WsdlMessageAssertion implements RequestAssertion, ResponseAssertion,
XPathReferenceContainer{

    protected String expectedContent;
    protected String path;

    protected boolean allowWildcards;
    protected boolean ignoreNamespaceDifferences;
    protected boolean ignoreComments;
	
	protected XBaseContainsAssertion(TestAssertionConfig assertionConfig,
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
	    
	    
}
