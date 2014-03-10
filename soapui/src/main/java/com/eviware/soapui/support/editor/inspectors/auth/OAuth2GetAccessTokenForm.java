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
import com.eviware.soapui.support.components.PropertyComponent;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.value.AbstractValueModel;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class OAuth2GetAccessTokenForm implements OAuth2AccessTokenStatusChangeListener
{
	public static final String CLIENT_ID_TITLE = "Client Identification";
	public static final String CLIENT_SECRET_TITLE = "Client Secret";
	public static final String AUTHORIZATION_URI_TITLE = "Authorization URI";
	public static final String ACCESS_TOKEN_URI_TITLE = "Access Token URI";
	public static final String REDIRECT_URI_TITLE = "Redirect URI";
	public static final String SCOPE_TITLE = "Scope";
	public static final String OAUTH_2_FLOW_COMBO_BOX_NAME = "OAuth2Flow";
	public static final String ACCESS_TOKEN_FORM_DIALOG_NAME = "getAccessTokenFormDialog";

	private static final String GET_ACCESS_TOKEN_BUTTON_NAME = "getAccessTokenButtonName";
	private static final String ACCESS_TOKEN_FORM_DIALOG_TITLE = "Get Access Token";
	private static final String AUTOMATION_BUTTON_TITLE = "Automation...";

	private static final String GET_ACCESS_TOKEN_FORM_LAYOUT = "7dlu:none,left:pref,10dlu,left:pref,10dlu,left:MAX(112dlu;pref),7dlu";

	private static final int BOARDER_SPACING = 15;
	private static final int NORMAL_SPACING = 10;
	private static final int GROUP_SPACING = 20;

	private static final Color CARD_BORDER_COLOR = new Color( 121, 121, 121 );

	static final ImageIcon DEFAULT_ICON = null;

	private OAuth2Profile profile;
	private JLabel accessTokenStatusText;
	private OAuth2AccessTokenStatusChangeManager statusChangeManager;
	private JDialog accessTokenDialog;

	public OAuth2GetAccessTokenForm( OAuth2Profile profile )
	{
		this.profile = profile;
	}

	public JDialog getComponent()
	{
		SimpleBindingForm accessTokenForm = createSimpleBindingForm( profile );
		statusChangeManager = new OAuth2AccessTokenStatusChangeManager( this );
		populateGetAccessTokenForm( accessTokenForm );
		statusChangeManager.register();
		setOAuth2StatusFeedback( profile.getAccesTokenStatusAsEnum() );
		accessTokenDialog = createGetAccessTokenDialog( accessTokenForm.getPanel() );
		return accessTokenDialog;
	}

	@Override
	public void onAccessTokenStatusChanged( OAuth2Profile.AccessTokenStatus status )
	{
		setOAuth2StatusFeedback( status );
	}

	@Nonnull
	@Override
	public OAuth2Profile getProfile()
	{
		return profile;
	}

	void release()
	{
		statusChangeManager.unregister();
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

		accessTokenForm.appendTextField( OAuth2Profile.CLIENT_ID_PROPERTY, CLIENT_ID_TITLE, "" );

		final JTextField clientSecretField = appendClientSecretField( accessTokenForm, getOAuth2FlowValueModel( accessTokenForm ) );

		accessTokenForm.addSpace( GROUP_SPACING );

		accessTokenForm.appendTextField( OAuth2Profile.AUTHORIZATION_URI_PROPERTY, AUTHORIZATION_URI_TITLE, "" );
		accessTokenForm.appendTextField( OAuth2Profile.ACCESS_TOKEN_URI_PROPERTY, ACCESS_TOKEN_URI_TITLE, "" );
		accessTokenForm.appendTextField( OAuth2Profile.REDIRECT_URI_PROPERTY, REDIRECT_URI_TITLE, "" );

		accessTokenForm.addSpace( GROUP_SPACING );

		accessTokenForm.appendTextField( OAuth2Profile.SCOPE_PROPERTY, SCOPE_TITLE, "" );

		accessTokenForm.addSpace( NORMAL_SPACING );

		accessTokenForm.appendComponentsInOneRow( createGetAccessTokenButton(), createAccessTokenStatusText() );
		accessTokenForm.appendButtonWithoutLabel( AUTOMATION_BUTTON_TITLE, new EditAutomationScriptsAction( profile ) );

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
		return accessTokenForm.getPresentationModel().getModel( OAuth2Profile.OAUTH2_FLOW_PROPERTY, "getOAuth2Flow", "setOAuth2Flow" );
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
		final JTextField clientSecretField = accessTokenForm.appendTextField( OAuth2Profile.CLIENT_SECRET_PROPERTY, CLIENT_SECRET_TITLE, "" );
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
		accessTokenStatusText = new JLabel();
		return new PropertyComponent( accessTokenStatusText );
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

	private void setOAuth2StatusFeedback( OAuth2Profile.AccessTokenStatus status )
	{
		// There are no auth profile selected
		if( status == null )
		{
			setDefaultFeedback();
		}
		else
		{
			switch( status )
			{
				case WAITING_FOR_AUTHORIZATION:
				case RECEIVED_AUTHORIZATION_CODE:
					setWaitingFeedback( status );
					break;
				case ENTERED_MANUALLY:
				case RETRIEVED_FROM_SERVER:
				default:
					setDefaultFeedback();
					break;
			}
		}
	}

	private void setWaitingFeedback( OAuth2Profile.AccessTokenStatus status )
	{
		accessTokenStatusText.setText( status.toString() );
		accessTokenStatusText.setIcon( OAuth2Form.WAIT_ICON );
	}

	private void setDefaultFeedback()
	{
		accessTokenStatusText.setText( "" );
		accessTokenStatusText.setIcon( DEFAULT_ICON );
		closeGetAccessTokenDialog();
	}

	private void closeGetAccessTokenDialog()
	{
		if( accessTokenDialog != null )
		{
			accessTokenDialog.setVisible( false );
			accessTokenDialog.dispose();
		}
	}


	private class EditAutomationScriptsAction extends AbstractAction
	{
		private final OAuth2Profile profile;

		public EditAutomationScriptsAction( OAuth2Profile profile )
		{
			putValue( Action.NAME, AUTOMATION_BUTTON_TITLE );
			this.profile = profile;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			closeGetAccessTokenDialog();
			UISupport.showDesktopPanel( new OAuth2ScriptsDesktopPanel( profile ) );
		}
	}
}