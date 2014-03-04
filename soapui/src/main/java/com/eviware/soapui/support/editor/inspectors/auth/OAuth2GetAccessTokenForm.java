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
import com.eviware.soapui.support.components.PropertyComponent;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.value.AbstractValueModel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class OAuth2GetAccessTokenForm
{
	private static final String ACCESS_TOKEN_FORM_DIALOG_NAME = "getAccessTokenFormDialog";
	private static final String GET_ACCESS_TOKEN_BUTTON_NAME = "getAccessTokenButtonName";
	private static final String ACCESS_TOKEN_FORM_DIALOG_TITLE = "Get Access Token";
	private static final String OAUTH_2_FLOW_COMBO_BOX_NAME = "OAuth2Flow";

	private static final String GET_ACCESS_TOKEN_FORM_LAYOUT = "7dlu:none,left:pref,10dlu,left:pref,10dlu,left:MAX(100dlu;pref),7dlu";

	private static final int BOARDER_SPACING = 15;
	private static final int NORMAL_SPACING = 10;
	private static final int GROUP_SPACING = 20;

	private static final Color CARD_BORDER_COLOR = new Color( 121, 121, 121 );

	private OAuth2Profile profile;

	public JDialog getComponent( OAuth2Profile profile )
	{
		this.profile = profile;
		SimpleBindingForm accessTokenForm = createSimpleBindingForm( profile );
		populateGetAccessTokenForm( accessTokenForm );
		return createGetAccessTokenDialog( accessTokenForm.getPanel() );
	}

	private SimpleBindingForm createSimpleBindingForm( OAuth2Profile profile )
	{
		PresentationModel presentationModel = new PresentationModel<OAuth2Profile>( profile );
		String columnsSpecs = GET_ACCESS_TOKEN_FORM_LAYOUT;
		Border border = BorderFactory.createLineBorder( CARD_BORDER_COLOR, 1 );
		return new SimpleBindingForm( presentationModel, columnsSpecs, border );
	}

	private void populateGetAccessTokenForm( SimpleBindingForm accessTokenForm )
	{
		accessTokenForm.addSpace( BOARDER_SPACING );

		accessTokenForm.appendHeading( "Get Access Token from the authorization server" );

		accessTokenForm.addSpace( NORMAL_SPACING );

		JComboBox oauth2FlowComboBox = appendOAuth2ComboBox( accessTokenForm );

		accessTokenForm.addSpace( GROUP_SPACING );

		accessTokenForm.appendTextField( OAuth2Profile.CLIENT_ID_PROPERTY, "Client Identification", "" );

		final JTextField clientSecretField = appendClientSecretField( accessTokenForm, getOAuth2FlowValueModel( accessTokenForm ) );

		accessTokenForm.addSpace( GROUP_SPACING );

		accessTokenForm.appendTextField( OAuth2Profile.AUTHORIZATION_URI_PROPERTY, "Authorization URI", "" );
		accessTokenForm.appendTextField( OAuth2Profile.ACCESS_TOKEN_URI_PROPERTY, "Access Token URI", "" );
		accessTokenForm.appendTextField( OAuth2Profile.REDIRECT_URI_PROPERTY, "Redirect URI", "" );

		accessTokenForm.addSpace( GROUP_SPACING );

		accessTokenForm.appendTextField( OAuth2Profile.SCOPE_PROPERTY, "Scope", "" );

		accessTokenForm.addSpace( NORMAL_SPACING );

		accessTokenForm.appendComponentsInOneRow( createGetAccessTokenButton(), createAccessTokenStatusText() );

		accessTokenForm.addSpace( GROUP_SPACING );

		accessTokenForm.appendLabelAsLink( "http://www.soapui.org", "How to get an access token from an authorization server" );

		accessTokenForm.addSpace( BOARDER_SPACING );

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
	}

	private AbstractValueModel getOAuth2FlowValueModel( SimpleBindingForm accessTokenForm )
	{
		return accessTokenForm.getPresentationModel().getModel( OAuth2Profile.OAUTH2_FLOW, "getOAuth2Flow", "setOAuth2Flow" );
	}

	private JComboBox appendOAuth2ComboBox( SimpleBindingForm accessTokenForm )
	{
		AbstractValueModel valueModel = getOAuth2FlowValueModel( accessTokenForm );
		ComboBoxModel oauth2FlowsModel = new DefaultComboBoxModel<OAuth2Profile.OAuth2Flow>( OAuth2Profile.OAuth2Flow.values() );
		JComboBox oauth2FlowComboBox = accessTokenForm.appendComboBox( "OAuth2.0 Flow", oauth2FlowsModel, "OAuth2.0 Authorization Flow", valueModel );
		oauth2FlowComboBox.setName( OAUTH_2_FLOW_COMBO_BOX_NAME );
		return oauth2FlowComboBox;
	}

	private JTextField appendClientSecretField( SimpleBindingForm accessTokenForm, AbstractValueModel valueModel )
	{
		final JTextField clientSecretField = accessTokenForm.appendTextField( OAuth2Profile.CLIENT_SECRET_PROPERTY, "Client Secret", "" );
		if( valueModel.getValue() == OAuth2Profile.OAuth2Flow.IMPLICIT_GRANT )
		{
			clientSecretField.setVisible( false );
		}
		return clientSecretField;
	}

	private PropertyComponent createGetAccessTokenButton()
	{
		JButton getAccessTokenButton = new JButton( new GetOAuthAccessTokenAction( profile ) );
		getAccessTokenButton.setName( GET_ACCESS_TOKEN_BUTTON_NAME );
		return new PropertyComponent( getAccessTokenButton );
	}

	private PropertyComponent createAccessTokenStatusText()
	{
		JLabel accessTokenStatusText = new JLabel();
		return new PropertyComponent( OAuth2Profile.ACCESS_TOKEN_STATUS_PROPERTY, accessTokenStatusText );

	}

	private JDialog createGetAccessTokenDialog( JPanel accessTokenFormPanel )
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