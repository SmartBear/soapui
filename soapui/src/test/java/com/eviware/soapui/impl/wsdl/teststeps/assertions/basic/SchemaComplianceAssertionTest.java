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
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.utils.ModelItemFactory;
import com.eviware.soapui.utils.StubbedDialogs;
import com.eviware.x.dialogs.XDialogs;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.eviware.soapui.utils.CommonMatchers.anEmptyCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for SchemaComplianceAssertion.
 */
public class SchemaComplianceAssertionTest {

    private SchemaComplianceAssertion assertion;
    private XDialogs originalDialogs;
    private StubbedDialogs stubbedDialogs;

    @Before
    public void setUp() throws Exception {
        assertion = new SchemaComplianceAssertion(TestAssertionConfig.Factory.newInstance(), ModelItemFactory.makeTestRequestStep());
        originalDialogs = UISupport.getDialogs();
        stubbedDialogs = new StubbedDialogs();
        UISupport.setDialogs(stubbedDialogs);
    }

    @After
    public void tearDown() throws Exception {
        UISupport.setDialogs(originalDialogs);
    }

    @Test
    public void configureAbortedWithErrorMessageWhenNoValidDefinitionSelected() throws Exception {
        stubbedDialogs.mockPromptWithReturnValue("<invalid URL>");

        assertThat(assertion.configure(), is(false));
        assertThat(stubbedDialogs.getErrorMessages(), is(not(anEmptyCollection())));
    }

    @Test
    public void noErrorMessageShownWhenConfigureIsCancelled() throws Exception {
        stubbedDialogs.mockPromptWithReturnValue(null);

        assertThat(assertion.configure(), is(false));
        assertThat(stubbedDialogs.getErrorMessages(), is(anEmptyCollection()));
    }

    @Test
    public void canBeConfiguredWithAValidWSDL() throws Exception {
        String validWsdlURL = SchemaComplianceAssertionTest.class.getResource("/attachment-test.wsdl").toString();
        stubbedDialogs.mockPromptWithReturnValue(validWsdlURL);

        assertThat(assertion.configure(), is(true));
        assertThat(stubbedDialogs.getErrorMessages(), is(anEmptyCollection()));
    }

    @Test
    public void canBeConfiguredWithAValidWADL() throws Exception {
        String validWadlURL = SchemaComplianceAssertionTest.class.getResource("/wadl/YahooSearch.wadl").toString();
        stubbedDialogs.mockPromptWithReturnValue(validWadlURL);

        assertThat(assertion.configure(), is(true));
        assertThat(stubbedDialogs.getErrorMessages(), is(anEmptyCollection()));
    }
}
