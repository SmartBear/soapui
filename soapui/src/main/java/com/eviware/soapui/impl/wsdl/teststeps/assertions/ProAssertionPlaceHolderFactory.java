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

package com.eviware.soapui.impl.wsdl.teststeps.assertions;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.Assertable.AssertionStatus;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.TestAssertion;

public class ProAssertionPlaceHolderFactory implements TestAssertionFactory {

    private String type;

    public ProAssertionPlaceHolderFactory(String type, String string2) {
        this.type = type;
    }

    @Override
    public boolean canAssert(Assertable assertable) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canAssert(TestPropertyHolder modelItem, String property) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public TestAssertion buildAssertion(TestAssertionConfig config, Assertable assertable) {
        return new ProAssertionPlaceHolder(config, assertable);
    }

    @Override
    public Class<? extends WsdlMessageAssertion> getAssertionClassType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAssertionId() {
        return type;
    }

    @Override
    public String getAssertionLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AssertionListEntry getAssertionListEntry() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getCategory() {
        // TODO Auto-generated method stub
        return null;
    }

    private class ProAssertionPlaceHolder extends WsdlMessageAssertion {

        protected ProAssertionPlaceHolder(TestAssertionConfig assertionConfig, Assertable modelItem, boolean cloneable,
                                          boolean configurable, boolean multiple, boolean requiresResponseContent) {
            super(assertionConfig, modelItem, cloneable, configurable, multiple, requiresResponseContent);
        }

        public ProAssertionPlaceHolder(TestAssertionConfig config, Assertable assertable) {
            this(config, assertable, false, false, false, false);
        }

        @Override
        protected String internalAssertResponse(MessageExchange messageExchange, SubmitContext context)
                throws AssertionException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected String internalAssertRequest(MessageExchange messageExchange, SubmitContext context)
                throws AssertionException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected String internalAssertProperty(TestPropertyHolder source, String propertyName,
                                                MessageExchange messageExchange, SubmitContext context) throws AssertionException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public AssertionStatus getStatus() {
            return assertionStatus.UNKNOWN;
        }

        @Override
        public boolean isDisabled() {
            return true;
        }
    }
}
