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

package com.eviware.soapui.impl.wsdl.teststeps.assertions.json;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XPathContainsAssertion;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.support.JsonPathFacade;
import com.eviware.soapui.support.JsonUtil;
import junit.framework.ComparisonFailure;

public abstract class JsonPathAssertionBase extends XPathContainsAssertion {
    public JsonPathAssertionBase(TestAssertionConfig assertionConfig, Assertable assertable) {
        super(assertionConfig, assertable);
    }


    @Override
    protected String internalAssertResponse(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        if (!messageExchange.hasResponse()) {
            return "Missing Response";
        } else {
            return assertContent(messageExchange.getResponseContent(), context, "Response");
        }
    }

    @Override
    protected String internalAssertRequest(MessageExchange messageExchange, SubmitContext context) throws AssertionException {
        if (!messageExchange.hasRequest(false)) {
            return "Missing Request";
        } else {
            return assertContent(messageExchange.getRequestContent(), context, "Request");
        }
    }

    @Override
    protected String internalAssertProperty(TestPropertyHolder source, String propertyName,
                                            MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        if (!JsonUtil.seemsToBeJson(source.getPropertyValue(propertyName))) {
            throw new AssertionException(new AssertionError("Property '" + propertyName
                    + "' has value which is not JSON!"));
        }
        return assertContent(source.getPropertyValue(propertyName), context, propertyName);
    }

    protected PropertyExpansionContext getPropertyExpansionContext() {
        return getAssertable().getTestStep() == null ?
                new DefaultPropertyExpansionContext(getAssertable().getModelItem()) :
                new WsdlTestRunContext(getAssertable().getTestStep());
    }

    @Override
    public boolean canAssertXmlContent() {
        return false;
    }

    protected void throwAssertionException(String path, Throwable exception) throws AssertionException {
        String msg = "";

        if (exception instanceof ComparisonFailure) {
            ComparisonFailure cf = (ComparisonFailure) exception;
            String expected = cf.getExpected();
            String actual = cf.getActual();

            msg = "Comparison failed for path [" + path + "], expecting [" + expected + "], actual was [" + actual + "]";
        } else {
            msg = "Assertion failed for path [" + path + "] : " + exception.getClass().getSimpleName() + ":"
                    + exception.getMessage();
        }

        throw new AssertionException(new AssertionError(msg));
    }

    @Override
    public String getPathAreaTitle() {
        return "Specify JSONPath expression and expected result";
    }

    @Override
    public String getPathAreaDescription() {
        return "";
    }

    @Override
    public String getPathAreaToolTipText() {
        return "Specifies the JSONPath expression to select from the message for validation";
    }

    @Override
    public String getPathAreaBorderTitle() {
        return "JSONPath Expression";
    }

    @Override
    public String getContentAreaToolTipText() {
        return "Specifies the expected result of the JSONPath expression";
    }

    @Override
    public String getContentAreaBorderTitle() {
        return "Expected Result";
    }

    protected String getPathString() {
        String path = getPathArea() == null || !getPathArea().isVisible() ? getPath() : getPathArea().getSelectedText();
        if (path == null) {
            path = getPathArea() == null ? "" : getPathArea().getText();
        }
        return path;
    }

    protected String readStringValue(String assertableContent, String expandedPath) {
        Object result = new JsonPathFacade(assertableContent).readObjectValue(expandedPath);
        return result == null ? null : result.toString();
    }

    public static class JsonAssertionFactory extends AbstractTestAssertionFactory {

        private String assertionDescription;
        private final Class<? extends JsonPathAssertionBase> assertionClass;

        public JsonAssertionFactory(String assertionId, String assertionLabel, String assertionDescription,
                                    Class assertionClass) {
            super(assertionId, assertionLabel, assertionClass);
            this.assertionDescription = assertionDescription;
            this.assertionClass = assertionClass;
        }

        @Override
        public String getCategory() {
            return AssertionCategoryMapping.VALIDATE_RESPONSE_CONTENT_CATEGORY;
        }

        @Override
        public Class<? extends WsdlMessageAssertion> getAssertionClassType() {
            return assertionClass;
        }

        @Override
        public AssertionListEntry getAssertionListEntry() {
            return new AssertionListEntry(getAssertionId(), getAssertionLabel(), assertionDescription);
        }

        @Override
        public boolean canAssert(Assertable assertable) {
            return super.canAssert(assertable) && JsonUtil.seemsToBeJson(assertable.getAssertableContent());
        }

        @Override
        public boolean canAssert(TestPropertyHolder modelItem, String property) {
            if (!modelItem.getProperty(property).getSchemaType().isPrimitiveType()) {
                return true;
            }

            String content = modelItem.getPropertyValue(property);
            return JsonUtil.seemsToBeJson(content);
        }
    }
}
