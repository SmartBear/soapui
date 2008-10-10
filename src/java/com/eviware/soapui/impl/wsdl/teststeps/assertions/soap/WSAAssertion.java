/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps.assertions.soap;

import org.apache.xmlbeans.XmlObject;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.WsaPropertiesTable;
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

public class WSAAssertion extends WsdlMessageAssertion implements ResponseAssertion, RequestAssertion
{
	public static final String ID = "WS Addressing Assertion";
	public static final String LABEL = "WS-Addressing";
	private WsaPropertiesTable wsaPropertiesTable;
	private boolean assertWsaAction;
	private boolean assertWsaTo;
	private boolean assertWsaRelatesTo;
	private XFormDialog dialog;
	private static final String ASSERT_ACTION = "Assert wsa:Action";
	private static final String ASSERT_TO = "Assert wsa:To";
	private static final String ASSERT_RELATES_TO = "Assert wsa:RelatesTo";
   /**
	 * Constructor for our assertion.
	 * 
	 * @param assertionConfig
	 * @param modelItem
	 */
	public WSAAssertion(TestAssertionConfig assertionConfig, Assertable modelItem)
	{
		super(assertionConfig, modelItem, false, true, false, true);

		XmlObjectConfigurationReader reader = new XmlObjectConfigurationReader(getConfiguration());
		assertWsaAction = reader.readBoolean("asertWsaAction", true);
		assertWsaTo = reader.readBoolean("asertWsaTo", true);
		assertWsaRelatesTo = reader.readBoolean("asertWsaRelatesTo", false);
	}

   public static class Factory extends AbstractTestAssertionFactory
	{
		@SuppressWarnings("unchecked")
		public Factory()
		{
			super(WSAAssertion.ID, WSAAssertion.LABEL, WSAAssertion.class, new Class[] { WsdlRequest.class,
					WsdlMockResponseTestStep.class });
		}
	}

	@Override
	protected String internalAssertRequest(MessageExchange messageExchange, SubmitContext context)
			throws AssertionException
	{
		try
		{
			new WsaValidator((WsdlMessageExchange) messageExchange, wsaPropertiesTable).validateWsAddressingRequest();
		}
		catch (Exception e)
		{
			throw new AssertionException(new AssertionError(e.getMessage()));
		}

		return "Request WS-Addressing is valid";
	}

	@Override
	protected String internalAssertResponse(MessageExchange messageExchange, SubmitContext context)
			throws AssertionException
	{
		try
		{
			new WsaValidator((WsdlMessageExchange) messageExchange, wsaPropertiesTable).validateWsAddressingResponse();
		}
		catch (Exception e)
		{
			throw new AssertionException(new AssertionError(e.getMessage()));
		}

		return "Response WS-Addressing is valid";
	}

	public boolean configure()
	{
		if( dialog == null )
			buildDialog();

		StringToStringMap values = new StringToStringMap();
		values.put( ASSERT_ACTION, assertWsaAction );
		values.put( ASSERT_TO, assertWsaTo );
		values.put( ASSERT_RELATES_TO, assertWsaRelatesTo );

		values = dialog.show( values );
		if( dialog.getReturnValue() == XFormDialog.OK_OPTION )
		{
			assertWsaAction = values.getBoolean( ASSERT_ACTION );
			assertWsaTo = values.getBoolean(ASSERT_TO);
			assertWsaRelatesTo = values.getBoolean(ASSERT_RELATES_TO);
		}

		wsaPropertiesTable = new WsaPropertiesTable(assertWsaAction, assertWsaTo, assertWsaRelatesTo);
		setConfiguration( createConfiguration() );
		return true;
	}

	private void buildDialog()
	{
		XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "Ws-a properties to assert" );
		XForm mainForm = builder.createForm( "Basic" );
		mainForm.addCheckBox(ASSERT_ACTION, "Check if 'wsa:Action' exists and has the right value");
		mainForm.addCheckBox( ASSERT_TO, "Check if 'wsa:To' exists" );
		mainForm.addCheckBox( ASSERT_RELATES_TO, "Check if 'wsa:RelatesTo' exists" );

		dialog = builder.buildDialog( builder
					.buildOkCancelHelpActions( HelpUrls.SIMPLE_CONTAINS_HELP_URL ), "Specify options",
					UISupport.OPTIONS_ICON );
	}

	protected XmlObject createConfiguration()
	{
		XmlObjectConfigurationBuilder builder = new XmlObjectConfigurationBuilder();
		builder.add( "asertWsaAction", assertWsaAction );
		builder.add( "asertWsaTo", assertWsaTo );
		builder.add( "asertWsaRelatesTo", assertWsaRelatesTo );
		return builder.finish();
	}

	public boolean isAssertWsaAction()
	{
		return assertWsaAction;
	}

	public void setAssertWsaAction(boolean assertWsaAction )
	{
		this.assertWsaAction = assertWsaAction;
	}

	public boolean isAssertWsaTo()
	{
		return assertWsaTo;
	}

	public void setAssertWsaTo(boolean assertWsaTo )
	{
		this.assertWsaTo = assertWsaTo;
	}

	public boolean isAssertWsaRelatesTo()
	{
		return assertWsaRelatesTo;
	}

	public void setAssertWsaRelatesTo(boolean assertWsaRelatesTo )
	{
		this.assertWsaRelatesTo = assertWsaRelatesTo;
	}

	public WsaPropertiesTable getWsaPropertiesTable()
	{
		return wsaPropertiesTable;
	}

	public void setWsaPropertiesTable(WsaPropertiesTable wsaPropertiesTable)
	{
		this.wsaPropertiesTable = wsaPropertiesTable;
	}

}
