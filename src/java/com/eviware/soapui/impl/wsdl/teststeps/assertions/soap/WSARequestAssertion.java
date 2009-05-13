/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps.assertions.soap;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.WsaAssertionConfiguration;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaValidator;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
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

/**
 * Assertion for verifying that WS-Addressing processing was ok
 * 
 * @author dragica.soldo
 */

public class WSARequestAssertion extends WsdlMessageAssertion implements RequestAssertion, ResponseAssertion
{
	public static final String ID = "WS-A Request Assertion";
	public static final String LABEL = "WS-Addressing Request";
	private WsaAssertionConfiguration wsaAssertionConfiguration;
	private boolean assertWsaAction;
	private boolean assertWsaTo;
	private boolean assertWsaReplyTo;
	private boolean assertWsaMessageId;
	// private boolean assertWsaRelatesTo;
	// private boolean assertReplyToRefParams;
	// private boolean assertFaultToRefParams;
	private XFormDialog dialog;
	private static final String ASSERT_ACTION = "wsa:Action";
	private static final String ASSERT_TO = "wsa:To";
	private static final String ASSERT_REPLY_TO = "wsa:ReplyTo";
	private static final String ASSERT_MESSAGE_ID = "wsa:MessageId";

	// private static final String ASSERT_RELATES_TO = "wsa:RelatesTo";
	// private static final String ASSERT_REPLY_TO_REF_PARAMS =
	// "wsa:ReplyTo ReferenceParameters";
	// private static final String ASSERT_FAULT_TO_REF_PARAMS =
	// "wsa:FaultTo ReferenceParameters";

	/**
	 * Constructor for our assertion.
	 * 
	 * @param assertionConfig
	 * @param modelItem
	 */
	public WSARequestAssertion( TestAssertionConfig assertionConfig, Assertable modelItem )
	{
		super( assertionConfig, modelItem, false, true, false, true );

		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader( getConfiguration() );
		assertWsaAction = reader.readBoolean( "asertWsaAction", true );
		assertWsaTo = reader.readBoolean( "asertWsaTo", false );
		assertWsaReplyTo = reader.readBoolean( "assertWsaReplyTo", false );
		assertWsaMessageId = reader.readBoolean( "assertWsaMessageId", false );
		// assertWsaRelatesTo = reader.readBoolean("asertWsaRelatesTo", false);
		// assertReplyToRefParams = reader.readBoolean("assertReplyToRefParams",
		// false);
		// assertFaultToRefParams = reader.readBoolean("assertFaultToRefParams",
		// false);
		wsaAssertionConfiguration = new WsaAssertionConfiguration( assertWsaAction, assertWsaTo, assertWsaReplyTo,
				assertWsaMessageId, false, false, false );
	}

	public static class Factory extends AbstractTestAssertionFactory
	{
		@SuppressWarnings( "unchecked" )
		public Factory()
		{
			super( WSARequestAssertion.ID, WSARequestAssertion.LABEL, WSARequestAssertion.class,
					WsdlMockResponseTestStep.class );
		}
	}

	@Override
	protected String internalAssertRequest( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
		try
		{
			new WsaValidator( ( WsdlMessageExchange )messageExchange, wsaAssertionConfiguration )
					.validateWsAddressingRequest();
		}
		catch( AssertionException e )
		{
			throw new AssertionException( new AssertionError( e.getMessage() ) );
		}
		catch( XmlException e )
		{
			SoapUI.logError( e );
			throw new AssertionException( new AssertionError(
					"There has been some XmlException, ws-a couldn't be validated properly." ) );
		}

		return "Request WS-Addressing is valid";
	}

	@Override
	protected String internalAssertResponse( MessageExchange messageExchange, SubmitContext context )
			throws AssertionException
	{
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

		// return "Response WS-Addressing is valid";
		return null;
	}

	public boolean configure()
	{
		if( dialog == null )
			buildDialog();

		StringToStringMap values = new StringToStringMap();
		values.put( ASSERT_ACTION, assertWsaAction );
		values.put( ASSERT_TO, assertWsaTo );
		values.put( ASSERT_REPLY_TO, assertWsaReplyTo );
		values.put( ASSERT_MESSAGE_ID, assertWsaMessageId );
		// values.put(ASSERT_RELATES_TO, assertWsaRelatesTo);
		// values.put(ASSERT_REPLY_TO_REF_PARAMS, assertReplyToRefParams);
		// values.put(ASSERT_FAULT_TO_REF_PARAMS, assertFaultToRefParams);

		values = dialog.show( values );
		if( dialog.getReturnValue() == XFormDialog.OK_OPTION )
		{
			assertWsaAction = values.getBoolean( ASSERT_ACTION );
			assertWsaTo = values.getBoolean( ASSERT_TO );
			assertWsaReplyTo = values.getBoolean( ASSERT_REPLY_TO );
			assertWsaMessageId = values.getBoolean( ASSERT_MESSAGE_ID );
			// assertWsaRelatesTo = values.getBoolean(ASSERT_RELATES_TO);
			// assertReplyToRefParams = values
			// .getBoolean(ASSERT_REPLY_TO_REF_PARAMS);
			// assertFaultToRefParams = values
			// .getBoolean(ASSERT_FAULT_TO_REF_PARAMS);
		}

		wsaAssertionConfiguration = new WsaAssertionConfiguration( assertWsaAction, assertWsaTo, assertWsaReplyTo,
				assertWsaMessageId, false, false, false );
		setConfiguration( createConfiguration() );
		return true;
	}

	private void buildDialog()
	{
		XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "Ws-a properties to assert" );
		XForm mainForm = builder.createForm( "Basic" );
		mainForm.addCheckBox( ASSERT_ACTION, "Check if 'wsa:Action' exists" );
		mainForm.addCheckBox( ASSERT_TO, "Check if 'wsa:To' exists" );
		mainForm.addCheckBox( ASSERT_REPLY_TO, "Check if 'wsa:ReplyTo' exists" );
		mainForm.addCheckBox( ASSERT_MESSAGE_ID, "Check if 'wsa:MessageId' exists" );
		// mainForm.addCheckBox(ASSERT_RELATES_TO,
		// "Check if 'wsa:RelatesTo' exists");
		// mainForm.addCheckBox(ASSERT_REPLY_TO_REF_PARAMS,
		// "Check if 'wsa:ReplyTo' ReferenceParameters exist");
		// mainForm.addCheckBox(ASSERT_FAULT_TO_REF_PARAMS,
		// "Check if 'wsa:FaultTo' ReferenceParameters exist");

		dialog = builder.buildDialog( builder.buildOkCancelHelpActions( HelpUrls.SIMPLE_CONTAINS_HELP_URL ),
				"Specify options", UISupport.OPTIONS_ICON );
	}

	protected XmlObject createConfiguration()
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		builder.add( "asertWsaAction", assertWsaAction );
		builder.add( "asertWsaTo", assertWsaTo );
		builder.add( "assertWsaReplyTo", assertWsaReplyTo );
		builder.add( "assertWsaMessageId", assertWsaMessageId );
		// builder.add("asertWsaRelatesTo", assertWsaRelatesTo);
		// builder.add("assertReplyToRefParams", assertReplyToRefParams);
		// builder.add("assertFaultToRefParams", assertFaultToRefParams);
		return builder.finish();
	}

}
