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

package com.eviware.soapui.impl.wsdl.teststeps.assertions.jms;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.submit.transports.jms.HermesJmsRequestTransport;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
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

/**
 * Asserts JMS response within timeout
 *
 * @author nebojsa.tasic
 * 04/14/2015 improved by Avdeev
 */

public class JMSTimeoutAssertion extends WsdlMessageAssertion implements ResponseAssertion, RequestAssertion {
    public static final String JMS_TIMEOUT_DURATION = "JMS timeout duration";
    private static final String JMS_TIMEOUT_SETTING = "timeout";
    public static final String JMS_TIMEOUT_OK = "JMS Timeout OK";
    private XFormDialog dialog;
    public static final String ID = "JMS Timeout";
    public static final String LABEL = "JMS Timeout";
    public static final String DESCRIPTION = "Validates that the JMS statement of the target TestStep did not take longer than the specified duration. Applicable to Request TestSteps with a JMS endpoint.";
    private long jmsTimeoutDuration;
    private static int magicUnreachableNumber = -120184;
    private static long defaultJmsAssertionTimeout = 100;
    private LastJmsResponseResult lastJmsResponseResult;

    private class LastJmsResponseResult {
        public long timeTaken;
        public boolean isRecieved;

        public LastJmsResponseResult (){
            timeTaken = magicUnreachableNumber;
            isRecieved = false;
        }
    }

    public JMSTimeoutAssertion(TestAssertionConfig assertionConfig, Assertable assertable) {
        super(assertionConfig, assertable, false, true, false, true);

        lastJmsResponseResult = new LastJmsResponseResult();

        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(getConfiguration());
        jmsTimeoutDuration = reader.readLong(JMS_TIMEOUT_SETTING, magicUnreachableNumber);
        if (jmsTimeoutDuration == magicUnreachableNumber){
            jmsTimeoutDuration = defaultJmsAssertionTimeout;
        }
    }

    @Override
    protected String internalAssertResponse(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        PropertyExpansionContext propertyExpansionContext = (PropertyExpansionContext) context;
        boolean isRun = propertyExpansionContext.hasProperty(HermesJmsRequestTransport.IS_JMS_MESSAGE_RECEIVED);
        Boolean temp = (Boolean) context.getProperty(HermesJmsRequestTransport.IS_JMS_MESSAGE_RECEIVED);
        Boolean messageReceived = temp != null ? temp : false;
        if (isRun) {
            lastJmsResponseResult.timeTaken = messageExchange.getTimeTaken();
            lastJmsResponseResult.isRecieved = messageReceived;
        }


        Long timeout = jmsTimeoutDuration;
        String jmsTimeoutError = "JMS Message timeout error! Message is not received in " + timeout + " ms.";
        if (isRun) { // runtime
            if (!messageReceived || lastJmsResponseResult.timeTaken > timeout) {
                throw new AssertionException(new AssertionError(jmsTimeoutError));
            }
        } else { //design time
            if (lastJmsResponseResult.timeTaken > timeout || !lastJmsResponseResult.isRecieved) {
                throw new AssertionException(new AssertionError(jmsTimeoutError));
            }
        }
        return JMS_TIMEOUT_OK;
    }

    @Override
    protected String internalAssertRequest(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        return JMS_TIMEOUT_OK;
    }

    @Override
    public boolean configure() {
        if (dialog == null) {
            buildDialog();
        }

        StringToStringMap values = new StringToStringMap();
        values.put(JMS_TIMEOUT_DURATION, new Long(jmsTimeoutDuration).toString());

        values = dialog.show(values);
        if (dialog.getReturnValue() == XFormDialog.OK_OPTION) {
            Long newJmsTimeoutDuration = new Long(values.get(JMS_TIMEOUT_DURATION));
            if (jmsTimeoutDuration != newJmsTimeoutDuration){
                jmsTimeoutDuration = newJmsTimeoutDuration;
                setConfiguration(createConfiguration());
                return true;
            }
        }

        return false;
    }

    protected XmlObject createConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        builder.add(JMS_TIMEOUT_SETTING, jmsTimeoutDuration);
        return builder.finish();
    }

    private XFormDialog buildDialog() {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("JMS timeout assertion");
        XForm mainForm = builder.createForm("Basic");
        mainForm.addTextField(JMS_TIMEOUT_DURATION, JMS_TIMEOUT_DURATION, XForm.FieldType.TEXT);
        dialog = builder.buildDialog(builder.buildOkCancelHelpActions(HelpUrls.ASSERTION_JMS_TIMEOUT_EDITOR_HELP_URL), "Specify option", UISupport.OPTIONS_ICON);
        return dialog;
    }

    protected String internalAssertProperty(TestPropertyHolder source, String propertyName,
                                            MessageExchange messageExchange, SubmitContext context) throws AssertionException {
        return null;
    }

    public static class Factory extends AbstractTestAssertionFactory {
        public Factory() {
            super(JMSTimeoutAssertion.ID, JMSTimeoutAssertion.LABEL, JMSTimeoutAssertion.class, WsdlRequest.class);
        }

        @Override
        public String getCategory() {
            return AssertionCategoryMapping.JMS_CATEGORY;
        }

        @Override
        public Class<? extends WsdlMessageAssertion> getAssertionClassType() {
            return JMSTimeoutAssertion.class;
        }

        @Override
        public AssertionListEntry getAssertionListEntry() {
            return new AssertionListEntry(JMSTimeoutAssertion.ID, JMSTimeoutAssertion.LABEL,
                    JMSTimeoutAssertion.DESCRIPTION);
        }
    }
}
