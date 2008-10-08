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

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.actions.project.SimpleDialog;
import com.eviware.soapui.impl.wsdl.panels.teststeps.support.WsaPropertiesTable;
import com.eviware.soapui.impl.wsdl.submit.WsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaValidator;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMessageAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.AbstractTestAssertionFactory;
import com.eviware.soapui.model.iface.MessageExchange;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.*;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;

import javax.swing.*;
import java.awt.*;

/**
 * Assertion for verifying that WS-Addressing processing was ok
 * 
 * @author dragica.soldo
 */

public class WSAAssertion extends WsdlMessageAssertion implements ResponseAssertion, RequestAssertion
{
	public static final String ID = "WS Addressing Assertion";
	public static final String LABEL = "WS-Addressing";
	private JDialog configurationDialog;
	private boolean configureResult;
	private SimpleForm editorForm;
	private WsaPropertiesTable wsaPropertiesTable;
	private boolean assertWsaAction;
	private boolean assertWsaTo;
	private boolean assertWsaRelatesTo;
	private JCheckBox assertWsaActionCheckBox;
	private JCheckBox assertWsaToCheckBox;
	private JCheckBox assertWsaRelatesToCheckBox;

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
		assertWsaAction = reader.readBoolean("action", true);
		assertWsaTo = reader.readBoolean("to", true);
		assertWsaRelatesTo = reader.readBoolean("relatesTo", false);
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
		if (configurationDialog == null)
			buildConfigurationDialog();

		assertWsaActionCheckBox.setSelected( assertWsaAction );
		assertWsaToCheckBox.setSelected( assertWsaTo );
		assertWsaRelatesToCheckBox.setSelected( assertWsaRelatesTo );

		UISupport.showDialog(configurationDialog);
		return configureResult;

	}

	protected void buildConfigurationDialog()
	{
		String onlineHelpUrl = "";
		configurationDialog = new SimpleDialog("WS-A Assertion", "Set options for WS-Addressing assertion", onlineHelpUrl, true)
		{

			@Override
			protected Component buildContent()
			{
				editorForm = new SimpleForm();
            
				assertWsaActionCheckBox = editorForm.appendCheckBox( "wsa:Action", "Asserts value of wsa:Action against WSDL metadata", assertWsaAction );
				assertWsaToCheckBox = editorForm.appendCheckBox("wsa:To", "Asserts value of wsa:To against WSDL metadata", assertWsaTo );
				assertWsaRelatesToCheckBox = editorForm.appendCheckBox("wsa:RelatesTo", "Asserts value of wsa:RelatesTo in regard to sent MessageID", assertWsaRelatesTo );

				return editorForm.getPanel();
			}

			@Override
			protected boolean handleOk()
			{
				setAssertWsaAction( assertWsaActionCheckBox.isSelected());
				setAssertWsaTo( assertWsaToCheckBox.isSelected());
				setAssertWsaRelatesTo( assertWsaRelatesToCheckBox.isSelected());
				wsaPropertiesTable = new WsaPropertiesTable( assertWsaAction, assertWsaTo, assertWsaRelatesTo );
				configureResult = true;
				configurationDialog.setVisible(false);
				return true;
			}

		};
		configurationDialog.setSize(400, 200);
		configurationDialog.setModal(true);
	}

	private JButton okButton;

	public SimpleForm getForm()
	{
		if (editorForm == null)
		{
		}

		return editorForm;
	}

	public JButton getDefaultButton()
	{
		return okButton;
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
