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

package com.eviware.soapui.impl.wsdl.teststeps.assertions.soap;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlValidator;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;

/**
 * Asserts that the specified message is a valid SOAP Message
 *
 * @author ole.matzura
 */

public class SoapRequestAssertion extends WsdlMessageAssertion implements RequestAssertion {
    public static final String ID = "SOAP Request";
    public static final String LABEL = "SOAP Request";
    public static final String DESCRIPTION = "Validates that the last received request is a valid SOAP Request. Applicable to MockResponse TestSteps only.";

    public SoapRequestAssertion(TestAssertionConfig assertionConfig, Assertable assertable) {
        super(assertionConfig, assertable, false, false, false, false);
    }

    @Override
    protected String internalAssertRequest(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        WsdlContext wsdlContext = ((WsdlMessageExchange) messageExchange).getOperation().getInterface()
                .getWsdlContext();
        WsdlValidator validator = new WsdlValidator(wsdlContext);

        try {
            AssertionError[] errors = validator.assertRequest((WsdlMessageExchange) messageExchange, true);
            if (errors.length > 0) {
                throw new AssertionException(errors);
            }
        } catch (AssertionException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionException(new AssertionError(e.getMessage()));
        }

        return "Request Envelope OK";
    }

    protected String internalAssertProperty(TestPropertyHolder source, String propertyName,
                                            MessageExchange messageExchange, SubmitContext context) throws AssertionException {
        return null;
    }

    public static class Factory extends AbstractTestAssertionFactory {
        public Factory() {
            super(SoapRequestAssertion.ID, SoapRequestAssertion.LABEL, SoapRequestAssertion.class,
                    WsdlMockResponseTestStep.class);
        }

        @Override
        public String getCategory() {
            return AssertionCategoryMapping.STATUS_CATEGORY;
        }

        @Override
        public Class<? extends WsdlMessageAssertion> getAssertionClassType() {
            return SoapRequestAssertion.class;
        }

        @Override
        public AssertionListEntry getAssertionListEntry() {
            return new AssertionListEntry(SoapRequestAssertion.ID, SoapRequestAssertion.LABEL,
                    SoapRequestAssertion.DESCRIPTION);
        }
    }

    @Override
    protected String internalAssertResponse(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        return null;
    }
}
