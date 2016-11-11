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
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import junit.framework.Assert;

import javax.swing.JTextArea;
import java.util.regex.PatternSyntaxException;

public class JsonPathRegExAssertion extends JsonPathAssertionBase implements RequestAssertion, ResponseAssertion {

    public static final String ID = "JsonPath RegEx Match";
    public static final String LABEL = "JsonPath RegEx Match";
    public static final String DESCRIPTION = "Uses an JsonPath expression to select content from the target property and compares the result to an specified RegEx. Applicable to any property containing JSON.";
    public static final String REG_EX_PROPERTY_NAME = "regEx";
    private String regularExpression;

    public JsonPathRegExAssertion(TestAssertionConfig assertionConfig, Assertable assertable) {
        super(assertionConfig, assertable);
        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(getConfiguration());
        regularExpression = reader.readString(REG_EX_PROPERTY_NAME, null);
    }

    @Override
    public String getHelpURL() {
        return HelpUrls.ASSERTION_JSON_REGEX;
    }

    @Override
    protected JsonPathRegExAssertion getAssertion() {
        return this;
    }

    @Override
    public String getConfigurationDialogTitle() {
        return "JSONPath RegEx Match Configuration";
    }

    @Override
    protected void addConfigurationValues(XmlObjectConfigurationBuilder builder) {
        super.addConfigurationValues(builder);
        builder.add(REG_EX_PROPERTY_NAME, this.regularExpression);
    }

    @Override
    public boolean configure() {
        if (configurationDialog == null) {
            configurationDialog = new JsonPathRegExAssertionConfigurationDialog(getAssertion());
        }

        return configurationDialog.configure();
    }

    @Override
    public void selectFromCurrent() {
        try {
            String assertableContent = getAssertable().getAssertableContent();
            if (StringUtils.isNullOrEmpty(assertableContent)) {
                UISupport.showErrorMessage("Missing content to select from");
                return;
            }

            if (StringUtils.isNullOrEmpty(this.regularExpression)) {
                UISupport.showErrorMessage("Missing regular expression");
                return;
            }

            String path = getPathString();

            PropertyExpansionContext context = getPropertyExpansionContext();
            String expandedPath = PropertyExpander.expandProperties(context, path.trim());

            JTextArea contentArea = getContentArea();
            if (contentArea != null && contentArea.isVisible()) {
                contentArea.setText("");
            }

            String stringValue = readStringValue(assertableContent, expandedPath);
            if (stringValue == null) {
                setExpectedValueFromSelectedNode(contentArea, Boolean.FALSE.toString());
            } else {
                try {
                    String matches = String.valueOf(stringValue.matches(this.regularExpression));
                    setExpectedValueFromSelectedNode(contentArea, matches);
                } catch (PatternSyntaxException pse) {
                    UISupport.showErrorMessage("Invalid regular expression. " + pse.getMessage());
                    return;
                }

            }

        } catch (Throwable e) {
            UISupport.showErrorMessage("Invalid JsonPath expression.");
            SoapUI.logError(e);
        }
    }

    @Override
    public void setPath(String path) {
        if (path.indexOf("##") > 0) {
            String[] parts = path.split("##");
            if (parts.length > 2) {
                setRegularExpression(parts[2]);
            }
            super.setPath(parts[0]);
        } else {
            super.setPath(path);
        }
    }

    private void setExpectedValueFromSelectedNode(JTextArea contentArea, String stringValue) {
        if (contentArea != null && contentArea.isVisible()) {
            contentArea.setText(stringValue);
        } else {
            setExpectedContent(stringValue, false);
        }
    }

    @Override
    public String assertContent(String assertableContent, SubmitContext context, String type) throws AssertionException {
        String path = getPath();
        try {
            if (path == null) {
                return "Missing path for JsonPath assertion";
            }
            if (getExpectedContent() == null) {
                return "Missing content for JsonPath assertion";
            }

            if (this.regularExpression == null) {
                return "Missing RegEx for JsonPath assertion";
            }

            String expandedPath = PropertyExpander.expandProperties(context, path);
            String result = readStringValue(assertableContent, expandedPath);
            Boolean actualValue = Boolean.FALSE;
            if (result != null && result.matches(this.regularExpression)) {
                actualValue = Boolean.TRUE;
            }
            String expandedExpectedValue = PropertyExpander.expandProperties(context, getExpectedContent());
            Assert.assertEquals(expandedExpectedValue, actualValue.toString());
        } catch (Throwable exception) {
            throwAssertionException(getPath(), exception);
        }
        return type + " matches content for [" + path + "]";
    }

    public void setRegularExpression(String regularExpression) {
        this.regularExpression = regularExpression;
        setConfiguration(createConfiguration());
    }

    public String getRegularExpression() {
        return regularExpression;
    }

    public static class Factory extends JsonAssertionFactory {
        public Factory() {
            super(JsonPathRegExAssertion.ID, JsonPathRegExAssertion.LABEL, JsonPathRegExAssertion.DESCRIPTION,
                    JsonPathRegExAssertion.class);
        }
    }
}
