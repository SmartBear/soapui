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

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;

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
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionError;
import com.eviware.soapui.model.testsuite.AssertionException;
import com.eviware.soapui.model.testsuite.RequestAssertion;
import com.eviware.soapui.model.testsuite.ResponseAssertion;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;

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
	private boolean asertWsaAction;
	private boolean asertWsaTo;
	private boolean asertWsaRelatesTo;
	private JCheckBox asertWsaActionCheckBox;
	private JCheckBox asertWsaToCheckBox;
	private JCheckBox asertWsaRelatesToCheckBox;

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
		asertWsaAction = reader.readBoolean("action", true);
		asertWsaTo = reader.readBoolean("to", true);
		asertWsaRelatesTo = reader.readBoolean("relatesTo", false);
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

		asertWsaActionCheckBox.setSelected(asertWsaAction);
		asertWsaToCheckBox.setSelected(asertWsaTo);
		asertWsaRelatesToCheckBox.setSelected(asertWsaRelatesTo);

		UISupport.showDialog(configurationDialog);
		return configureResult;

	}

	protected void buildConfigurationDialog()
	{
		String onlineHelpUrl = "";
		configurationDialog = new SimpleDialog("Ws-a properties to assert","", onlineHelpUrl, true)
		{

			@Override
			protected Component buildContent()
			{
				editorForm = new SimpleForm();
				asertWsaActionCheckBox = new JCheckBox("Assert wsa:Action", asertWsaAction);
				editorForm.append(asertWsaActionCheckBox);
				asertWsaToCheckBox = new JCheckBox("Assert wsa:To", asertWsaTo);
				editorForm.append(asertWsaToCheckBox);

				asertWsaRelatesToCheckBox = new JCheckBox("Assert wsa:RelatesTo", asertWsaRelatesTo);
				editorForm.append(asertWsaRelatesToCheckBox);

				return editorForm.getPanel();
			}

			@Override
			protected boolean handleOk()
			{
				setAsertWsaAction(asertWsaActionCheckBox.isSelected());
				setAsertWsaTo(asertWsaToCheckBox.isSelected());
				setAsertWsaRelatesTo(asertWsaRelatesToCheckBox.isSelected());
				wsaPropertiesTable = new WsaPropertiesTable(asertWsaAction, asertWsaTo, asertWsaRelatesTo);
				configureResult = true;
				configurationDialog.setVisible(false);
				return true;
			}

		};
		configurationDialog.setSize(300, 200);
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

	public boolean isAsertWsaAction()
	{
		return asertWsaAction;
	}

	public void setAsertWsaAction(boolean asertWsaAction)
	{
		this.asertWsaAction = asertWsaAction;
	}

	public boolean isAsertWsaTo()
	{
		return asertWsaTo;
	}

	public void setAsertWsaTo(boolean asertWsaTo)
	{
		this.asertWsaTo = asertWsaTo;
	}

	public boolean isAsertWsaRelatesTo()
	{
		return asertWsaRelatesTo;
	}

	public void setAsertWsaRelatesTo(boolean asertWsaRelatesTo)
	{
		this.asertWsaRelatesTo = asertWsaRelatesTo;
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
