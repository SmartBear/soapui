/*
 * SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 * SoapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.jgoodies.binding.PresentationModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 */
public class BasicAuthenticationForm<T extends AbstractHttpRequest>  extends AbstractAuthenticationForm
{
	protected T request;
	private JRadioButton globalButton;
	private JRadioButton preemptiveButton;

	public BasicAuthenticationForm(T request)
	{
		this.request = request;
	}

	@Override
	protected JPanel buildUI()
	{
		SimpleBindingForm basicAuthenticationForm = new SimpleBindingForm( new PresentationModel<T>( request ) );
		populateBasicForm( basicAuthenticationForm );

		JPanel panel = basicAuthenticationForm.getPanel();
		setBorderAndBackgroundColorOnPanel( panel );

		return panel;
	}

	public void setButtonGroupVisibility( boolean visible )
	{
		globalButton.setVisible( visible );
		preemptiveButton.setVisible( visible );
	}

	protected void populateBasicForm( SimpleBindingForm basicConfigurationForm )
	{
		initForm( basicConfigurationForm );

		basicConfigurationForm.addSpace( TOP_SPACING );

		basicConfigurationForm.appendTextField( "username", "Username", "The username to use for HTTP Authentication" );
		basicConfigurationForm.appendPasswordField( "password", "Password", "The password to use for HTTP Authentication" );
		basicConfigurationForm.appendTextField( "domain", "Domain", "The domain to use for Authentication(NTLM/Kerberos)" );

		ButtonGroup buttonGroup = new ButtonGroup();
		globalButton = basicConfigurationForm.appendRadioButton( "", "Use global preference", buttonGroup, true);
		preemptiveButton = basicConfigurationForm.appendRadioButton( "", "Authenticate pre-emptively", buttonGroup, false );

		globalButton.addActionListener( new UseGlobalSettingsRadioButtonListener( globalButton ) );
		preemptiveButton.addActionListener( new PreemptiveRadioButtonListener( preemptiveButton ) );
	}

	private class PreemptiveRadioButtonListener implements ActionListener
	{
		private final JRadioButton preemptiveButton;

		public PreemptiveRadioButtonListener( JRadioButton preemptiveButton )
		{
			this.preemptiveButton = preemptiveButton;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			if ( preemptiveButton.isSelected())
			{
				String authType = CredentialsConfig.AuthType.PREEMPTIVE.toString();
				request.setSelectedAuthProfileAndAuthType( ProfileSelectionForm.BASIC_AUTH_PROFILE, authType );
			}
		}
	}

	private class UseGlobalSettingsRadioButtonListener implements ActionListener
	{
		private final JRadioButton globalButton;

		public UseGlobalSettingsRadioButtonListener( JRadioButton globalButton )
		{
			this.globalButton = globalButton;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			if ( globalButton.isSelected())
			{
				String authType = CredentialsConfig.AuthType.GLOBAL_HTTP_SETTINGS.toString();
				request.setSelectedAuthProfileAndAuthType( ProfileSelectionForm.BASIC_AUTH_PROFILE, authType );
			}
		}
	}
}
