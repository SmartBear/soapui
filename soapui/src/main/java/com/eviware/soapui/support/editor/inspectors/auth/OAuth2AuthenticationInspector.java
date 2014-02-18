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
import com.jgoodies.binding.value.AbstractValueModel;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

// TODO Could we use composition instead?
public final class OAuth2AuthenticationInspector extends BasicAuthenticationInspector
{
	private static final String OAUTH_2_FORM_LABEL = "OAuth 2 form";
	public static final String ADVANCED_OPTIONS = "Advanced Options";

	private final OAuth2Profile profile;
	private final SimpleBindingForm oAuth2Form;
	private JTextField clientSecretField;

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

		AbstractValueModel valueModel = oauth2Form.getPresentationModel().getModel( OAuth2Profile.OAUTH2_FLOW_PROPERTY,
				"getOAuth2Flow", "setOAuth2Flow" );
		ComboBoxModel oauth2FlowsModel = new DefaultComboBoxModel<OAuth2Profile.OAuth2Flow>( OAuth2Profile.OAuth2Flow.values() );
		JComboBox oauth2FlowComboBox = oauth2Form.appendComboBox( "OAuth2.0 Flow", oauth2FlowsModel, "OAuth2.0 Authorization Flow", valueModel );

		oauth2Form.appendTextField( OAuth2Profile.CLIENT_ID_PROPERTY, "Client Identification", "" );
		clientSecretField = oauth2Form.appendTextField( OAuth2Profile.CLIENT_SECRET_PROPERTY, "Client Secret", "" );
		if( valueModel.getValue() == OAuth2Profile.OAuth2Flow.IMPLICIT_GRANT )
		{
			clientSecretField.setVisible( false );
		}
		oauth2FlowComboBox.addItemListener( new ItemListener()
		{
			@Override
			public void itemStateChanged( ItemEvent e )
			{
				if (e.getStateChange() == ItemEvent.SELECTED)
				{
					clientSecretField.setVisible( e.getItem() != OAuth2Profile.OAuth2Flow.IMPLICIT_GRANT );
				}
			}
		} );


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
		oauth2Form.addButtonWithoutLabel( "Edit scripts", new EditScriptsAction( profile ) );

		oauth2Form.addSpace( GROUP_SPACING );

		oauth2Form.appendTextField( OAuth2Profile.ACCESS_TOKEN_PROPERTY, "Access Token", "", SimpleForm.LONG_TEXT_FIELD_COLUMNS );

		JButton advanceOptionsButton = oauth2Form.addButtonWithoutLabel( ADVANCED_OPTIONS, new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				new OAuth2AdvanceOptionsDialog( profile );
			}
		} );
		advanceOptionsButton.setName( ADVANCED_OPTIONS );
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

	private class EditScriptsAction implements ActionListener
	{
		private final OAuth2Profile profile;

		public EditScriptsAction( OAuth2Profile profile )
		{
			this.profile = profile;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			OAuth2ScriptsEditor.Dialog dialog = new OAuth2ScriptsEditor.Dialog( profile );
			dialog.setVisible( true );
			if (dialog.getScripts() != null)
			{
				profile.setJavaScripts( dialog.getScripts() );
			}

		}
	}
}