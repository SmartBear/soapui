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
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import org.apache.xmlbeans.XmlObject;

/**
 * Assertion for verifiying that responses occurred in the desired amount of
 * time.
 *
 * @author Cory Lewis cory.lewis@genworth.com
 *         <p/>
 *         with help from
 * @author Ole.Matzura
 */

public class ResponseSLAAssertion extends WsdlMessageAssertion implements ResponseAssertion {
    public static final MessageSupport messages = MessageSupport.getMessages(ResponseSLAAssertion.class);

    public static final String ID = "Response SLA Assertion";
    public static final String LABEL = "Response SLA";
    public static final String DESCRIPTION = "Validates that the last received response time was within the defined limit. Applicable to Script TestSteps and TestSteps that send requests and receive responses.";
    private String SLA;

    private static final String SLA_VALUE_IS_NOT_NUMBER = messages.get("ResponseSLAAssertion.InfoNotNumber");
    private static final String SLA_VALUE_IS_EMPTY = messages.get("ResponseSLAAssertion.InfoEmptyValue");
    private static final String FORM_TITLE = messages.get("ResponseSLAAssertion.Form.Title");
    private static final String FORM_DESCRIPTION = messages.get("ResponseSLAAssertion.Form.Description");

    /**
     * Constructor for our assertion.
     *
     * @param assertionConfig
     * @param modelItem
     */
    public ResponseSLAAssertion(TestAssertionConfig assertionConfig, Assertable modelItem) {
        super(assertionConfig, modelItem, false, true, false, false);
        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(getConfiguration());
        SLA = reader.readString("SLA", "200");
    }

    protected String internalAssertRequest(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        return null;
    }

    protected String internalAssertResponse(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        Response response = messageExchange.getResponse();
        long timeTaken = response == null ? messageExchange.getTimeTaken() : response.getTimeTaken();

        // assert!
        Long timeExecuted;
        String propertyValue = null;
        try {
            propertyValue = PropertyExpander.expandProperties(context, SLA.trim());
            timeExecuted = Long.parseLong(propertyValue);
        } catch (NumberFormatException exp) {
            if (!StringUtils.isNullOrEmpty(SLA)) {
                if (!StringUtils.isNullOrEmpty(propertyValue)) {
                    throw new AssertionException(new AssertionError(String.format(SLA_VALUE_IS_NOT_NUMBER, propertyValue)));
                } else {
                    throw new AssertionException(new AssertionError(String.format(SLA_VALUE_IS_NOT_NUMBER, SLA)));
                }
            } else {
                throw new AssertionException(new AssertionError(SLA_VALUE_IS_EMPTY));
            }
        }

        if (timeTaken > timeExecuted) {
            throw new AssertionException(new AssertionError("Response did not meet SLA " + timeTaken + "/" + timeExecuted));
        }

        return "Response meets SLA";
    }

    @Override
    protected String internalAssertProperty(TestPropertyHolder source, String propertyName,
                                            MessageExchange messageExchange, SubmitContext context) throws AssertionException {
        return null;
    }

    /**
     * @see com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion#configure()
     */
    public boolean configure() {
        String value = getSLA();

        if (StringUtils.isNullOrEmpty(value)) {
            value = "200";
        }
        String sla = UISupport.prompt(messages.get(FORM_DESCRIPTION), messages.get(FORM_TITLE), value);
        if (sla == null) {
            return false;
        }
        setSLA(sla);
        return true;
    }

    public String getSLA() {
        return SLA;
    }

    public void setSLA(String sla) {
        SLA = sla;
        setConfiguration(createConfiguration());
    }

    /**
     * @return XmlObject, our config chunk
     */
    protected XmlObject createConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        return builder.add("SLA", SLA).finish();
    }

    public static class Factory extends AbstractTestAssertionFactory {
        public Factory() {
            super(ResponseSLAAssertion.ID, ResponseSLAAssertion.LABEL, ResponseSLAAssertion.class);
        }

        @Override
        public String getCategory() {
            return AssertionCategoryMapping.SLA_CATEGORY;
        }

        @Override
        public Class<? extends WsdlMessageAssertion> getAssertionClassType() {
            return ResponseSLAAssertion.class;
        }

        @Override
        public AssertionListEntry getAssertionListEntry() {
            return new AssertionListEntry(ResponseSLAAssertion.ID, ResponseSLAAssertion.LABEL,
                    ResponseSLAAssertion.DESCRIPTION);
        }
    }
}
