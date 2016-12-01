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

package com.eviware.soapui.security.assertion;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.security.SecurityScan;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import org.apache.xmlbeans.XmlObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Asserts Http status code in response
 *
 * @author nebojsa.tasic
 */

public class ValidHttpStatusCodesAssertion extends WsdlMessageAssertion implements ResponseAssertion {
    public static final String ID = "Valid HTTP Status Codes";
    public static final String LABEL = "Valid HTTP Status Codes";

    private String codes;
    private XFormDialog dialog;
    private static final String CODES = "codes";
    public static final String DESCRIPTION = "Checks that the target TestStep received an HTTP result with a status code in the list of defined codes. Applicable to any TestStep that receives HTTP messages.";

    public ValidHttpStatusCodesAssertion(TestAssertionConfig assertionConfig, Assertable assertable) {
        super(assertionConfig, assertable, false, true, false, false);

        init();
    }

    private void init() {
        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(getConfiguration());
        codes = reader.readString(CODES, "");
    }

    @Override
    protected String internalAssertResponse(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {

        List<String> codeList = extractCodes(context);
        String[] statusElements = null;
        try {
            statusElements = messageExchange.getResponseHeaders().get("#status#", "-1").split(" ");

        } catch (NullPointerException npe) {
            SoapUI.logError(npe, "Header #status# is missing!");
        }

        if (statusElements.length >= 2) {
            String statusCode = statusElements[1].trim();
            if (!codeList.contains(statusCode)) {
                String message = "Response status code:" + statusCode + " is not in acceptable list of status codes";
                throw new AssertionException(new AssertionError(message));
            }
        } else {
            throw new AssertionException(new AssertionError("Status code extraction error! "));
        }

        return "OK";
    }

    protected String internalAssertProperty(TestPropertyHolder source, String propertyName,
                                            MessageExchange messageExchange, SubmitContext context) throws AssertionException {
        return null;
    }

    private List<String> extractCodes(SubmitContext context) {
        String expandedCodes = context.expand(codes);
        List<String> codeList = new ArrayList<String>();
        for (String str : expandedCodes.split(",")) {
            codeList.add(str.trim());
        }
        return codeList;
    }

    public static class Factory extends AbstractTestAssertionFactory {
        @SuppressWarnings("unchecked")
        public Factory() {
            super(ValidHttpStatusCodesAssertion.ID, ValidHttpStatusCodesAssertion.LABEL,
                    ValidHttpStatusCodesAssertion.class, new Class[]{SecurityScan.class, AbstractHttpRequest.class});
        }

        @Override
        public String getCategory() {
            return AssertionCategoryMapping.STATUS_CATEGORY;
        }

        @Override
        public Class<? extends WsdlMessageAssertion> getAssertionClassType() {
            return ValidHttpStatusCodesAssertion.class;
        }

        @Override
        public AssertionListEntry getAssertionListEntry() {
            return new AssertionListEntry(ValidHttpStatusCodesAssertion.ID, ValidHttpStatusCodesAssertion.LABEL,
                    ValidHttpStatusCodesAssertion.DESCRIPTION);
        }
    }

    @Override
    protected String internalAssertRequest(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        return null;
    }

    protected XmlObject createConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        builder.add(CODES, codes);
        return builder.finish();
    }

    @Override
    public boolean configure() {
        if (dialog == null) {
            buildDialog();
        }

        StringToStringMap values = new StringToStringMap();
        values.put(CODES, codes);

        values = dialog.show(values);
        if (dialog.getReturnValue() == XFormDialog.OK_OPTION) {
            codes = values.get(CODES);
        }

        setConfiguration(createConfiguration());
        return true;
    }

    public void setCodes(String codes) {
        this.codes = codes;
        setConfiguration(createConfiguration());
    }

    public String getCodes() {
        return codes;
    }

    private void buildDialog() {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Valid HTTP status codes Assertion");
        XForm mainForm = builder.createForm("Basic");

        mainForm.addTextField(CODES, "Comma-separated acceptable status codes", XForm.FieldType.TEXTAREA).setWidth(40);

        // TODO : update help URL
        dialog = builder.buildDialog(
                builder.buildOkCancelHelpActions(HelpUrls.SECURITY_VALID_HTTP_CODES_ASSERTION_HELP), "Specify codes",
                UISupport.OPTIONS_ICON);
    }

}
