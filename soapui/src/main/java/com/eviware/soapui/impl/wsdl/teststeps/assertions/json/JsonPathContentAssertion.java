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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XPathContainsAssertion;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.support.JsonPathFacade;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import junit.framework.Assert;
import junit.framework.ComparisonFailure;

import javax.swing.JTextArea;
import java.util.regex.Pattern;

public class JsonPathContentAssertion extends JsonPathAssertionBase implements RequestAssertion, ResponseAssertion {

    public static final String ID = "JsonPath Match";
    public static final String LABEL = "JsonPath Match";
    public static final String DESCRIPTION = "Uses an JsonPath expression to existence of a node and compares the result to an expected value. Applicable to any property containing JSON.";
    private boolean allowWildcards;

    /**
     * Compares two string for similarity, allows wildcard.
     *
     * @param expected
     * @param real
     * @param wildcard
     * @throws ComparisonFailure
     */
    public static void assertSimilar(String expected, String real, char wildcard) throws ComparisonFailure {
        if (!isSimilar(expected, real, wildcard)) {
            throw new ComparisonFailure("Not matched", expected, real);
        }
    }

    public static boolean isSimilar(String expected, String real, char wildcard) throws ComparisonFailure {

        // expected == wildcard matches all
        if (!expected.equals(String.valueOf(wildcard))) {

            StringBuilder sb = new StringBuilder();
            if (expected.startsWith(String.valueOf(wildcard))) {
                sb.append(".*");
            }
            boolean first = true;
            for (String token : expected.split(Pattern.quote(String.valueOf(wildcard)))) {
                if (token.isEmpty()) {
                    continue;
                }
                if (!first) {
                    sb.append(".*");
                }
                first = false;
                sb.append(Pattern.quote(token));
            }
            if (expected.endsWith(String.valueOf(wildcard))) {
                sb.append(".*");
            }
            if (!Pattern.compile(sb.toString(), Pattern.DOTALL).matcher(real).matches()) {
                return false;
            }
        }
        return true;
    }

    public JsonPathContentAssertion(TestAssertionConfig assertionConfig, Assertable assertable) {
        super(assertionConfig, assertable);

        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(getConfiguration());
        allowWildcards = reader.readBoolean("allowWildcards", false);
    }

    public boolean isAllowWildcards() {
        return allowWildcards;
    }

    public void setAllowWildcards(boolean allowWildcards) {
        this.allowWildcards = allowWildcards;

        setConfiguration(createConfiguration());
    }

    @Override
    public String getHelpURL() {
        return HelpUrls.ASSERTION_JSON_CONTENT;
    }

    @Override
    protected XPathContainsAssertion getAssertion() {
        return this;
    }

    @Override
    public String getConfigurationDialogTitle() {
        return "JSONPath Match Configuration";
    }

    @Override
    public void selectFromCurrent() {
        try {
            String assertableContent = getAssertable().getAssertableContent();
            if (StringUtils.isNullOrEmpty(assertableContent)) {
                UISupport.showErrorMessage("Missing content to select from");
                return;
            }

            String pathString = getPathString();

            PropertyExpansionContext context = getPropertyExpansionContext();
            String expandedPath = PropertyExpander.expandProperties(context, pathString.trim());

            JTextArea contentArea = getContentArea();
            if (contentArea != null && contentArea.isVisible()) {
                contentArea.setText("");
            }

            String stringValue = readStringValue(assertableContent, expandedPath);
            if (stringValue == null) {
                UISupport.showErrorMessage("No match in current response");
            } else {
                if (contentArea != null && contentArea.isVisible()) {
                    contentArea.setText(stringValue);
                } else {
                    setExpectedContent(stringValue, false);
                }
            }

        } catch (Throwable e) {
            UISupport.showErrorMessage("Invalid JsonPath expression.");
            SoapUI.logError(e);
        }
    }

    @Override
    public String assertContent(String assertableContent, SubmitContext context, String type) throws AssertionException {
        try {
            if (getPath() == null) {
                return "Missing path for JsonPath assertion";
            }
            if (getExpectedContent() == null) {
                return "Missing content for JsonPath assertion";
            }
            String expandedPath = PropertyExpander.expandProperties(context, getPath());
            JsonPathFacade jsonPathFacade = new JsonPathFacade(assertableContent);
            String result = jsonPathFacade.readStringValue(expandedPath);
            String expandedResult = PropertyExpander.expandProperties(context, result);

            String expandedContent = PropertyExpander.expandProperties(context, getExpectedContent());

            if (allowWildcards) {
                assertSimilar(expandedContent, expandedResult, '*');
            } else {
                Assert.assertEquals(expandedContent, expandedResult);
            }

        } catch (Throwable exception) {
            throwAssertionException(getPath(), exception);
        }
        return type + " matches content for [" + getPath() + "]";
    }

    public static class Factory extends JsonAssertionFactory {
        public Factory() {
            super(JsonPathContentAssertion.ID, JsonPathContentAssertion.LABEL, JsonPathContentAssertion.DESCRIPTION,
                    JsonPathContentAssertion.class);
        }
    }
}
