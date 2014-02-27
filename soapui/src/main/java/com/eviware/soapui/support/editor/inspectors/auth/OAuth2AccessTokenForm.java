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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.actions.oauth.GetOAuthAccessTokenAction;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.value.AbstractValueModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static javax.swing.BorderFactory.*;

public class OAuth2AccessTokenForm
{

	private static final String ACCESS_TOKEN_FORM_DIALOG_NAME = "getAccessTokenFormDialog";
	private static final String GET_ACCESS_TOKEN_BUTTON_NAME = "getAccessTokenButtonName";
	private static final String ACCESS_TOKEN_FORM_DIALOG_TITLE = "Get Access Token";
	private static final int NORMAL_SPACING = 10;
	private static final int GROUP_SPACING = 20;
	private static final String OAUTH_2_FLOW_COMBO_BOX_NAME = "OAuth2Flow";
	private static final Color CARD_BORDER_COLOR = new Color( 121, 121, 121 );

	private OAuth2Profile profile;

	public JDialog getComponent( OAuth2Profile profile )
	{
		this.profile = profile;
		SimpleBindingForm accessTokenForm = new SimpleBindingForm( new PresentationModel<OAuth2Profile>( profile ) );
		populateGetAccessTokenForm( accessTokenForm );

		JPanel panel = accessTokenForm.getPanel();
		panel.setBorder( createCompoundBorder( createLineBorder( CARD_BORDER_COLOR ),
				createEmptyBorder( 10, 10, 10, 10 ) ) );
		return createAccessTokenDialog( panel );
	}

	private void populateGetAccessTokenForm( SimpleBindingForm accessTokenForm )
	{
		JLabel formTitleLabel = new JLabel( "Get Access Token from the authorization server" );
		Font font = formTitleLabel.getFont();
		Font fontBold = new Font( font.getName(), Font.BOLD, font.getSize() );
		formTitleLabel.setFont( fontBold );
		accessTokenForm.addComponent( formTitleLabel );

		accessTokenForm.addSpace( NORMAL_SPACING );

		AbstractValueModel valueModel = accessTokenForm.getPresentationModel().getModel( OAuth2Profile.OAUTH2_FLOW,
				"getOAuth2Flow", "setOAuth2Flow" );
		ComboBoxModel oauth2FlowsModel = new DefaultComboBoxModel<OAuth2Profile.OAuth2Flow>( OAuth2Profile.OAuth2Flow.values() );
		JComboBox oauth2FlowComboBox = accessTokenForm.appendComboBox( "OAuth2.0 Flow", oauth2FlowsModel, "OAuth2.0 Authorization Flow", valueModel );
		oauth2FlowComboBox.setName( OAUTH_2_FLOW_COMBO_BOX_NAME );

		accessTokenForm.addSpace( GROUP_SPACING );

		accessTokenForm.appendTextField( OAuth2Profile.CLIENT_ID_PROPERTY, "Client Identification", "" );
		final JTextField clientSecretField = accessTokenForm.appendTextField( OAuth2Profile.CLIENT_SECRET_PROPERTY,
				"Client Secret", "" );
		if( valueModel.getValue() == OAuth2Profile.OAuth2Flow.IMPLICIT_GRANT )
		{
			clientSecretField.setVisible( false );
		}
		oauth2FlowComboBox.addItemListener( new ItemListener()
		{
			@Override
			public void itemStateChanged( ItemEvent e )
			{
				if( e.getStateChange() == ItemEvent.SELECTED )
				{
					clientSecretField.setVisible( e.getItem() != OAuth2Profile.OAuth2Flow.IMPLICIT_GRANT );
				}
			}
		} );

		accessTokenForm.addSpace( GROUP_SPACING );

		accessTokenForm.appendTextField( OAuth2Profile.AUTHORIZATION_URI_PROPERTY, "Authorization URI", "" );
		accessTokenForm.appendTextField( OAuth2Profile.ACCESS_TOKEN_URI_PROPERTY, "Access Token URI", "" );
		accessTokenForm.appendTextField( OAuth2Profile.REDIRECT_URI_PROPERTY, "Redirect URI", "" );

		accessTokenForm.addSpace( GROUP_SPACING );

		accessTokenForm.appendTextField( OAuth2Profile.SCOPE_PROPERTY, "Scope", "" );

		accessTokenForm.addSpace( NORMAL_SPACING );

		JButton getAccessTokenButton = accessTokenForm.addButtonWithoutLabel( "Get Access Token", new GetOAuthAccessTokenAction( profile ) );
		getAccessTokenButton.setName( GET_ACCESS_TOKEN_BUTTON_NAME );
		accessTokenForm.appendLabel( OAuth2Profile.ACCESS_TOKEN_STATUS_PROPERTY, "Access token status" );

		JLabel accessTokenDocumentationLink = UISupport.getLabelAsLink( "http://www.soapui.org",
				"How to get an access token from an authorization server" );
		accessTokenForm.addComponent( accessTokenDocumentationLink );
	}

	private JDialog createAccessTokenDialog( JPanel accessTokenFormPanel )
	{
		final JDialog accessTokenFormDialog = new JDialog();
		accessTokenFormDialog.setName( ACCESS_TOKEN_FORM_DIALOG_NAME );
		accessTokenFormDialog.setTitle( ACCESS_TOKEN_FORM_DIALOG_TITLE );
		accessTokenFormDialog.setIconImages( SoapUI.getFrameIcons() );
		accessTokenFormDialog.setUndecorated( true );
		accessTokenFormDialog.getContentPane().add( accessTokenFormPanel );

		return accessTokenFormDialog;
	}
}
