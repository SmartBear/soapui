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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionCategoryMapping;
import com.eviware.soapui.impl.wsdl.panels.assertions.AssertionListEntry;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.WsaAssertionConfiguration;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaValidator;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.TestPropertyHolder;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
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
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * Assertion for verifying that WS-Addressing processing was ok
 *
 * @author dragica.soldo
 */

public class WSAResponseAssertion extends WsdlMessageAssertion implements RequestAssertion, ResponseAssertion {
    public static final String ID = "WS-A Response Assertion";
    public static final String LABEL = "WS-Addressing Response";
    private WsaAssertionConfiguration wsaAssertionConfiguration;
    private boolean assertWsaAction;
    private boolean assertWsaTo;
    // private boolean assertWsaReplyTo;
    // private boolean assertWsaMessageId;
    private boolean assertWsaRelatesTo;
    private boolean assertReplyToRefParams;
    private boolean assertFaultToRefParams;
    private XFormDialog dialog;
    private static final String ASSERT_ACTION = "wsa:Action";
    private static final String ASSERT_TO = "wsa:To";
    // private static final String ASSERT_REPLY_TO = "wsa:ReplyTo";
    // private static final String ASSERT_MESSAGE_ID = "wsa:MessageId";
    private static final String ASSERT_RELATES_TO = "wsa:RelatesTo";
    private static final String ASSERT_REPLY_TO_REF_PARAMS = "wsa:ReplyTo ReferenceParameters";
    private static final String ASSERT_FAULT_TO_REF_PARAMS = "wsa:FaultTo ReferenceParameters";
    public static final String DESCRIPTION = "Validates that the last received response contains valid WS-Addressing Headers. Applicable to SOAP TestRequest Steps only.";

    /**
     * Constructor for our assertion.
     *
     * @param assertionConfig
     * @param modelItem
     */
    public WSAResponseAssertion(TestAssertionConfig assertionConfig, Assertable modelItem) {
        super(assertionConfig, modelItem, false, true, false, true);

        XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(getConfiguration());
        assertWsaAction = reader.readBoolean("asertWsaAction", true);
        assertWsaTo = reader.readBoolean("asertWsaTo", false);
        // assertWsaReplyTo = reader.readBoolean("assertWsaReplyTo", false);
        // assertWsaMessageId = reader.readBoolean("assertWsaMessageId", false);
        assertWsaRelatesTo = reader.readBoolean("asertWsaRelatesTo", false);
        assertReplyToRefParams = reader.readBoolean("assertReplyToRefParams", false);
        assertFaultToRefParams = reader.readBoolean("assertFaultToRefParams", false);
        wsaAssertionConfiguration = new WsaAssertionConfiguration(assertWsaAction, assertWsaTo, false, false,
                assertWsaRelatesTo, assertReplyToRefParams, assertFaultToRefParams);
    }

    public static class Factory extends AbstractTestAssertionFactory {
        public Factory() {
            super(WSAResponseAssertion.ID, WSAResponseAssertion.LABEL, WSAResponseAssertion.class, WsdlRequest.class);
        }

        @Override
        public String getCategory() {
            return AssertionCategoryMapping.STATUS_CATEGORY;
        }

        @Override
        public Class<? extends WsdlMessageAssertion> getAssertionClassType() {
            return WSAResponseAssertion.class;
        }

        @Override
        public AssertionListEntry getAssertionListEntry() {
            return new AssertionListEntry(WSAResponseAssertion.ID, WSAResponseAssertion.LABEL,
                    WSAResponseAssertion.DESCRIPTION);
        }
    }

    @Override
    protected String internalAssertRequest(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        // try
        // {
        // new WsaValidator( (WsdlMessageExchange) messageExchange,
        // wsaAssertionConfiguration ).validateWsAddressingResponse();
        // }
        // catch( AssertionException e )
        // {
        // throw new AssertionException( new AssertionError( e.getMessage() ) );
        // }
        // catch( XmlException e )
        // {
        // SoapUI.logError( e );
        // throw new AssertionException(
        // new AssertionError(
        // "There has been some XmlException, ws-a couldn't be validated properly."
        // ) );
        // }
        //
        // return "Request WS-Addressing is valid";
        return null;
    }

    @Override
    protected String internalAssertResponse(MessageExchange messageExchange, SubmitContext context)
            throws AssertionException {
        try {
            new WsaValidator((WsdlMessageExchange) messageExchange, wsaAssertionConfiguration)
                    .validateWsAddressingResponse();
        } catch (AssertionException e) {
            throw new AssertionException(new AssertionError(e.getMessage()));
        } catch (XmlException e) {
            SoapUI.logError(e);
            throw new AssertionException(new AssertionError(
                    "There has been some XmlException, WS-A couldn't be validated properly."));
        }

        return "Response WS-Addressing is valid";
    }

    protected String internalAssertProperty(TestPropertyHolder source, String propertyName,
                                            MessageExchange messageExchange, SubmitContext context) throws AssertionException {
        return null;
    }

    public boolean configure() {
        if (dialog == null) {
            buildDialog();
        }

        StringToStringMap values = new StringToStringMap();
        values.put(ASSERT_ACTION, assertWsaAction);
        values.put(ASSERT_TO, assertWsaTo);
        // values.put(ASSERT_REPLY_TO, assertWsaReplyTo);
        // values.put(ASSERT_MESSAGE_ID, assertWsaMessageId);
        values.put(ASSERT_RELATES_TO, assertWsaRelatesTo);
        values.put(ASSERT_REPLY_TO_REF_PARAMS, assertReplyToRefParams);
        values.put(ASSERT_FAULT_TO_REF_PARAMS, assertFaultToRefParams);

        values = dialog.show(values);
        if (dialog.getReturnValue() == XFormDialog.OK_OPTION) {
            assertWsaAction = values.getBoolean(ASSERT_ACTION);
            assertWsaTo = values.getBoolean(ASSERT_TO);
            // assertWsaReplyTo = values.getBoolean(ASSERT_REPLY_TO);
            // assertWsaMessageId = values.getBoolean(ASSERT_MESSAGE_ID);
            assertWsaRelatesTo = values.getBoolean(ASSERT_RELATES_TO);
            assertReplyToRefParams = values.getBoolean(ASSERT_REPLY_TO_REF_PARAMS);
            assertFaultToRefParams = values.getBoolean(ASSERT_FAULT_TO_REF_PARAMS);
        }

        wsaAssertionConfiguration = new WsaAssertionConfiguration(assertWsaAction, assertWsaTo, false, false,
                assertWsaRelatesTo, assertReplyToRefParams, assertFaultToRefParams);
        setConfiguration(createConfiguration());
        return true;
    }

    private void buildDialog() {
        XFormDialogBuilder builder = XFormFactory.createDialogBuilder("WS-A properties to assert");
        XForm mainForm = builder.createForm("Basic");
        mainForm.addCheckBox(ASSERT_ACTION, "Check if 'wsa:Action' exists and has the right value");
        mainForm.addCheckBox(ASSERT_TO, "Check if 'wsa:To' exists");
        // mainForm.addCheckBox(ASSERT_REPLY_TO, "Check if 'wsa:ReplyTo' exists");
        // mainForm.addCheckBox(ASSERT_MESSAGE_ID,
        // "Check if 'wsa:MessageId' exists");
        mainForm.addCheckBox(ASSERT_RELATES_TO, "Check if 'wsa:RelatesTo' exists and is equal to request MessageID");
        mainForm.addCheckBox(ASSERT_REPLY_TO_REF_PARAMS, "Check if 'wsa:ReplyTo' ReferenceParameters exist");
        mainForm.addCheckBox(ASSERT_FAULT_TO_REF_PARAMS, "Check if 'wsa:FaultTo' ReferenceParameters exist");

        dialog = builder.buildDialog(builder.buildOkCancelHelpActions(HelpUrls.SIMPLE_CONTAINS_HELP_URL),
                "Specify options", UISupport.OPTIONS_ICON);
    }

    protected XmlObject createConfiguration() {
        XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
        builder.add("asertWsaAction", assertWsaAction);
        builder.add("asertWsaTo", assertWsaTo);
        // builder.add("assertWsaReplyTo", assertWsaReplyTo);
        // builder.add("assertWsaMessageId", assertWsaMessageId);
        builder.add("asertWsaRelatesTo", assertWsaRelatesTo);
        builder.add("assertReplyToRefParams", assertReplyToRefParams);
        builder.add("assertFaultToRefParams", assertFaultToRefParams);
        return builder.finish();
    }

}
