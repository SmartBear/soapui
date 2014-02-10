package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.actions.oauth.GetOAuthAccessTokenAction;
import com.eviware.soapui.impl.rest.actions.oauth.RefreshOAuthAccessTokenAction;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.components.SimpleForm;
import com.jgoodies.binding.PresentationModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

// TODO Could we use composition instead?
public final class OAuth2AuthenticationInspector extends BasicAuthenticationInspector
{
	private static final String OAUTH_2_FORM_LABEL = "OAuth 2 form";

	private final OAuth2Profile profile;
	private final SimpleBindingForm oAuth2Form;

	protected OAuth2AuthenticationInspector( RestRequest request )
	{
		super( request );

		profile = getOAuth2Profile( request );
		oAuth2Form = new SimpleBindingForm( new PresentationModel<AbstractHttpRequest<?>>( profile ) );
		addOAuth2Panel();
		addOAuth2ToAuthTypeComboBox();
		selectCard();
	}

	@Override
	public void release()
	{
		super.release();

		oAuth2Form.getPresentationModel().release();
	}

	@Override
	void selectCard()
	{
		SimpleForm authTypeForm = getAuthTypeForm();
		Container cardPanel = getCardPanel();

		Component component = authTypeForm.getComponent( COMBO_BOX_LABEL );
		JComboBox comboBox = ( JComboBox )component;
		CardLayout layout = ( CardLayout )cardPanel.getLayout();
		if( comboBox.getSelectedItem().equals( CredentialsConfig.AuthType.O_AUTH_2.toString() ) )
		{
			layout.show( cardPanel, OAUTH_2_FORM_LABEL );
		}
		else
		{
			layout.show( cardPanel, LEGACY_FORM_LABEL );
		}
	}

	private void addOAuth2Panel()
	{
		populateOAuth2Form( oAuth2Form );
		getCardPanel().add( oAuth2Form.getPanel(), OAUTH_2_FORM_LABEL );
	}

	private void addOAuth2ToAuthTypeComboBox()
	{
		String[] options = {
				CredentialsConfig.AuthType.GLOBAL_HTTP_SETTINGS.toString(),
				CredentialsConfig.AuthType.PREEMPTIVE.toString(),
				CredentialsConfig.AuthType.NTLM.toString(),
				CredentialsConfig.AuthType.SPNEGO_KERBEROS.toString(),
				CredentialsConfig.AuthType.O_AUTH_2.toString()
		};

		JComboBox comboBox = ( JComboBox )getAuthTypeForm().getComponent( COMBO_BOX_LABEL );
		getAuthTypeForm().setComboBoxItems( AUTH_TYPE_PROPERTY_NAME, comboBox, options );
	}

	private void populateOAuth2Form( SimpleBindingForm oauth2Form )
	{
		initForm( oauth2Form );

		oauth2Form.addSpace( TOP_SPACING );

		oauth2Form.appendTextField( OAuth2Profile.CLIENT_ID_PROPERTY, "Client Identification", "" );
		oauth2Form.appendTextField( OAuth2Profile.CLIENT_SECRET_PROPERTY, "Client Secret", "" );

		oauth2Form.addSpace( GROUP_SPACING );

		oauth2Form.appendTextField( OAuth2Profile.AUTHORIZATION_URI_PROPERTY, "Authorization URI", "" );
		oauth2Form.appendTextField( OAuth2Profile.ACCESS_TOKEN_URI_PROPERTY, "Access Token URI", "" );
		oauth2Form.appendTextField( OAuth2Profile.REDIRECT_URI_PROPERTY, "Redirect URI", "" );

		oauth2Form.addSpace( GROUP_SPACING );

		oauth2Form.appendTextField( OAuth2Profile.SCOPE_PROPERTY, "Scope", "" );

		oauth2Form.addSpace( NORMAL_SPACING );

		// TODO This should be a bit wider, but leaving it at default size for now
		oauth2Form.addButtonWithoutLabel( "Get access token", new GetOAuthAccessTokenAction( profile ) );
		oauth2Form.appendLabel( OAuth2Profile.ACCESS_TOKEN_STATUS_PROPERTY, "Access token status" );
		oauth2Form.addButtonWithoutLabel( "Refresh access token", new RefreshOAuthAccessTokenAction( profile ) );

		oauth2Form.addSpace( GROUP_SPACING );

		oauth2Form.appendTextField( OAuth2Profile.ACCESS_TOKEN_PROPERTY, "Access Token", "", SimpleForm.LONG_TEXT_FIELD_COLUMNS );

		oauth2Form.addButtonWithoutLabel( "Advanced Options", new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				new OAuth2AdvanceOptionsDialog( profile );
			}
		} );
	}

	/**
	 * Currently there's only support for one profile per project
	 */
	private OAuth2Profile getOAuth2Profile( RestRequest request )
	{
		List<OAuth2Profile> oAuth2ProfileList = request.getOperation().getInterface().getProject().getOAuth2ProfileContainer().getOAuth2ProfileList();
		checkArgument( oAuth2ProfileList.size() == 1, "There should be one OAuth 2 profile configured on the project" );
		return oAuth2ProfileList.get( 0 );
	}

	private void getAccessToken( ActionEvent e )
	{
		GetOAuthAccessTokenAction getAccessTokenAction = new GetOAuthAccessTokenAction( profile );
		getAccessTokenAction.actionPerformed( e );
	}
}