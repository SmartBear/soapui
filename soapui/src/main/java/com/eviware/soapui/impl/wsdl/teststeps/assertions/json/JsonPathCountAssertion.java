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
import junit.framework.Assert;

import javax.swing.JTextArea;
import java.util.List;

public class JsonPathCountAssertion extends JsonPathAssertionBase implements RequestAssertion, ResponseAssertion {

    public static final String ID = "JsonPath Count";
    public static final String LABEL = "JsonPath Count";
    public static final String DESCRIPTION = "Uses an JsonPath expression to count the occurrences of an element." +
            " Applicable to any property containing JSON.";

    public JsonPathCountAssertion(TestAssertionConfig assertionConfig, Assertable assertable) {
        super(assertionConfig, assertable);
    }

    @Override
    public String getHelpURL() {
        return HelpUrls.ASSERTION_JSON_COUNT;
    }

    @Override
    protected XPathContainsAssertion getAssertion() {
        return this;
    }

    @Override
    public String getConfigurationDialogTitle() {
        return "JSONPath Count Match Configuration";
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

            Object result = new JsonPathFacade(assertableContent).readObjectValue(expandedPath);
            if (result == null) {
                UISupport.showErrorMessage("No match in current response");
            } else {
                Integer count = getResultSize(result);
                if (contentArea != null && contentArea.isVisible()) {
                    contentArea.setText(count.toString());
                } else {
                    setExpectedContent(count.toString(), false);
                }
            }

        } catch (Throwable e) {
            UISupport.showErrorMessage("Invalid JsonPath expression.");
            SoapUI.logError(e);
        }
    }

    private Integer getResultSize(Object result) {
        Integer count = 0;
        if (result instanceof List) {
            count = ((List) result).size();
        } else if (result != null) {
            count = 1;
        }
        return count;
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
            String expandedPath = PropertyExpander.expandProperties(context, path);
            Object result = new JsonPathFacade(assertableContent).readObjectValue(expandedPath);
            String resultSize = getResultSize(result).toString();

            String expandedContent = PropertyExpander.expandProperties(context, getExpectedContent());
            Assert.assertEquals(expandedContent, resultSize);
        } catch (Throwable exception) {
            throwAssertionException(path, exception);
        }
        return type + " matches content for [" + path + "]";
    }

    public static class Factory extends JsonAssertionFactory {
        public Factory() {
            super(JsonPathCountAssertion.ID, JsonPathCountAssertion.LABEL, JsonPathCountAssertion.DESCRIPTION,
                    JsonPathCountAssertion.class);
        }
    }
}
