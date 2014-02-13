package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.SoapUI;
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

// TODO Could we use composition instead?
public final class OAuth2AuthenticationInspector extends BasicAuthenticationInspector
{
	private static final String OAUTH_2_FORM_LABEL = "OAuth 2 form";
	public static final String ADVANCED_OPTIONS = "Advanced Options";
	public static final int ACCESS_TOKEN_DIALOG_HORIZONTAL_OFFSET = 80;

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
		oauth2Form.appendTextField( OAuth2Profile.ACCESS_TOKEN_PROPERTY, "Access Token", "",
				SimpleForm.LONG_TEXT_FIELD_COLUMNS );

		SimpleBindingForm accessTokenForm = new SimpleBindingForm( new PresentationModel<AbstractHttpRequest<?>>( profile ) );
		populateGetAccessTokenForm( accessTokenForm );

		final JPanel accessTokenFormPanel = accessTokenForm.getPanel();
		accessTokenFormPanel.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );

		final JDialog accessTokenFormDialog = createAccessTokenDialog( accessTokenFormPanel );
		final JButton disclosureButton = oauth2Form.addButtonWithoutLabel( "Get Token", new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				JButton source = ( JButton )e.getSource();
				Point disclosureButtonLocation = source.getLocationOnScreen();

				accessTokenFormDialog.setVisible( !accessTokenFormDialog.isVisible() );
				if( isEnoughSpaceAvailableBelowTheButton( disclosureButtonLocation ) )
				{
					setAccessTokenFormDialogBoundsBelowTheButton( disclosureButtonLocation, accessTokenFormDialog, source.getHeight() );
				}
				else
				{
					setAccessTokenFormDialogBoundsAboveTheButton( disclosureButtonLocation, accessTokenFormDialog );
				}
			}
		} );

		accessTokenFormPanel.setBounds( 0, -300, 400, 400 );

		oauth2Form.addSpace( GROUP_SPACING );

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

	private boolean isEnoughSpaceAvailableBelowTheButton( Point disclosureButtonLocation )
	{
		Dimension rootWindowSize = SoapUI.getFrame().getSize();
		return disclosureButtonLocation.getY() + 365 <= rootWindowSize.getHeight();
	}

	private void setAccessTokenFormDialogBoundsBelowTheButton( Point disclosureButtonLocation, JDialog accessTokenFormDialog, int disclosureButtonHeight )
	{
		accessTokenFormDialog.setBounds( ( int )disclosureButtonLocation.getX() - ACCESS_TOKEN_DIALOG_HORIZONTAL_OFFSET, ( int )disclosureButtonLocation.getY() + disclosureButtonHeight,
				500, 340 );
	}

	private void setAccessTokenFormDialogBoundsAboveTheButton( Point disclosureButtonLocation, JDialog accessTokenFormDialog )
	{
		accessTokenFormDialog.setBounds( ( int )disclosureButtonLocation.getX() - 80,
				( int )disclosureButtonLocation.getY() - 345, 500, 340 );
	}

	// TODO Make this reusable at some later point
	private JDialog createAccessTokenDialog( JPanel accessTokenFormPanel )
	{
		final JDialog accessTokenFormDialog = new JDialog();
		accessTokenFormDialog.setUndecorated( true );
		accessTokenFormDialog.getContentPane().add( accessTokenFormPanel );
		//accessTokenFormDialog.setSize( 400, 400 );
		accessTokenFormDialog.addFocusListener( new FocusListener()
		{
			// FIXME Make this work
			@Override
			public void focusGained( FocusEvent e )
			{
			}

			@Override
			public void focusLost( FocusEvent e )
			{
				accessTokenFormDialog.setVisible( false );
			}
		} );
		return accessTokenFormDialog;
	}

	private void populateGetAccessTokenForm( SimpleBindingForm accessTokenForm )
	{
		accessTokenForm.addSpace( NORMAL_SPACING );

		AbstractValueModel valueModel = accessTokenForm.getPresentationModel().getModel( OAuth2Profile.OAUTH2_FLOW,
				"getOAuth2Flow", "setOAuth2Flow" );
		ComboBoxModel oauth2FlowsModel = new DefaultComboBoxModel<OAuth2Profile.OAuth2Flow>( OAuth2Profile.OAuth2Flow.values() );
		JComboBox oauth2FlowComboBox = accessTokenForm.appendComboBox( "OAuth2.0 Flow", oauth2FlowsModel, "OAuth2.0 Authorization Flow", valueModel );

		accessTokenForm.appendTextField( OAuth2Profile.CLIENT_ID_PROPERTY, "Client Identification", "" );
		clientSecretField = accessTokenForm.appendTextField( OAuth2Profile.CLIENT_SECRET_PROPERTY, "Client Secret", "" );
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

		// TODO This should be a bit wider, but leaving it at default size for now
		accessTokenForm.addButtonWithoutLabel( "Get access token", new GetOAuthAccessTokenAction( profile ) );
		accessTokenForm.appendLabel( OAuth2Profile.ACCESS_TOKEN_STATUS_PROPERTY, "Access token status" );
		accessTokenForm.addButtonWithoutLabel( "Refresh access token", new RefreshOAuthAccessTokenAction( profile ) );
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

}