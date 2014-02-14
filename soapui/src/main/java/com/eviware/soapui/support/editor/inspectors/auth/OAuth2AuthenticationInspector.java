package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.actions.oauth.GetOAuthAccessTokenAction;
import com.eviware.soapui.impl.rest.actions.oauth.RefreshOAuthAccessTokenAction;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.components.SimpleForm;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.value.AbstractValueModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

public final class OAuth2AuthenticationInspector extends BasicAuthenticationInspector
{
	private static final String OAUTH_2_FORM_LABEL = "OAuth 2 form";
	public static final String ADVANCED_OPTIONS = "Advanced Options";
	public static final int ACCESS_TOKEN_DIALOG_HORIZONTAL_OFFSET = 120;

	private final OAuth2Profile profile;
	private final SimpleBindingForm oAuth2Form;
	private JTextField clientSecretField;
	private JPanel wrapperPanel;
	private boolean disclosureButtonDisabled;
	private boolean isMouseOnDisclosureLabel;

	protected OAuth2AuthenticationInspector( RestRequest request )
	{
		super( request );

		profile = getOAuth2Profile( request );
		oAuth2Form = new SimpleBindingForm( new PresentationModel<OAuth2Profile>( profile ) );
		buildOAuth2Panel();
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
			layout.show( cardPanel, BASIC_FORM_LABEL );
		}
	}

	private void buildOAuth2Panel()
	{
		addOAuth2Panel();
		addOAuth2ToAuthTypeComboBox();
		selectCard();
	}

	private void addOAuth2Panel()
	{
		populateOAuth2Form( oAuth2Form );

		wrapperPanel = new JPanel( new BorderLayout() );

		JPanel centerPanel = oAuth2Form.getPanel();
		setBackgroundColorOnPanel( centerPanel );

		JPanel southPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		JLabel oAuthDocumentationLink = getLabelLink( "http://www.soapui.org", "Learn about OAuth 2" );
		southPanel.add( oAuthDocumentationLink );

		southPanel.setBorder( BorderFactory.createMatteBorder( 1, 0, 0, 0, CARD_BORDER_COLOR ) );
		setBackgroundColorOnPanel( southPanel );

		wrapperPanel.add( centerPanel, BorderLayout.CENTER );
		wrapperPanel.add( southPanel, BorderLayout.SOUTH );

		setBorderOnPanel( wrapperPanel );

		getCardPanel().add( wrapperPanel, OAUTH_2_FORM_LABEL );
	}

	private JLabel getLabelLink( final String url, String labelText )
	{
		JLabel oAuthDocumentationLink = new JLabel( labelText );
		oAuthDocumentationLink.setForeground( Color.BLUE );
		oAuthDocumentationLink.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( MouseEvent e )
			{
				Tools.openURL( url );
			}

			@Override
			public void mouseEntered( MouseEvent e )
			{
				e.getComponent().setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
			}
		} );
		return oAuthDocumentationLink;
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

	private void populateOAuth2Form( SimpleBindingForm oAuth2Form )
	{
		initForm( oAuth2Form );

		oAuth2Form.addSpace( TOP_SPACING );
		oAuth2Form.appendTextField( OAuth2Profile.ACCESS_TOKEN_PROPERTY, "Access Token", "",
				SimpleForm.MEDIUM_TEXT_FIELD_COLUMNS );
		oAuth2Form.addInputFieldHintText( "Enter existing access token, or use \"Get Token\" below." );

		SimpleBindingForm accessTokenForm = new SimpleBindingForm( new PresentationModel<OAuth2Profile>( profile ) );
		populateGetAccessTokenForm( accessTokenForm );

		final JPanel accessTokenFormPanel = accessTokenForm.getPanel();
		accessTokenFormPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder( CARD_BORDER_COLOR ),BorderFactory.createEmptyBorder( 10, 10, 10, 10 )));

		final JDialog accessTokenFormDialog = createAccessTokenDialog( accessTokenFormPanel );
		final JLabel disclosureButton = new JLabel( "▲ Get Token" );
		oAuth2Form.addComponentWithoutLabel( disclosureButton );
		disclosureButton.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( MouseEvent e )
			{
				// Check if this click is to hide the access token form dialog
				if( disclosureButtonDisabled )
				{
					disclosureButtonDisabled = false;
					return;
				}

				JLabel source = ( JLabel )e.getSource();
				Point disclosureButtonLocation = source.getLocationOnScreen();
				accessTokenFormDialog.pack();
				accessTokenFormDialog.setVisible( true );
				disclosureButton.setText( "▼ Get Token" );
				if( isEnoughSpaceAvailableBelowTheButton( disclosureButtonLocation, accessTokenFormDialog.getHeight() ) )
				{
					setAccessTokenFormDialogBoundsBelowTheButton( disclosureButtonLocation, accessTokenFormDialog, source.getHeight() );
				}
				else
				{
					setAccessTokenFormDialogBoundsAboveTheButton( disclosureButtonLocation, accessTokenFormDialog );
				}
			}

			@Override
			public void mouseEntered( MouseEvent e )
			{
				isMouseOnDisclosureLabel = true;
			}

			@Override
			public void mouseExited( MouseEvent e )
			{
				isMouseOnDisclosureLabel = false;
			}
		} );

		accessTokenFormDialog.addWindowFocusListener( new WindowFocusListener()
		{
			@Override
			public void windowGainedFocus( WindowEvent e )
			{
				disclosureButtonDisabled = true;
			}

			@Override
			public void windowLostFocus( WindowEvent e )
			{
				accessTokenFormDialog.setVisible( false );
				disclosureButton.setText( "▲ Get Token" );
				// If the focus is lost due to click on the disclosure button then don't enable it yet, since it
				// will then show the dialog directly again.
				if( !isMouseOnDisclosureLabel )
				{
					disclosureButtonDisabled = false;
				}
			}
		} );


		JButton advancedOptionsButton = oAuth2Form.addButtonWithoutLabelToTheRight( ADVANCED_OPTIONS, new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				new OAuth2AdvanceOptionsDialog( profile );
			}
		} );
		advancedOptionsButton.setName( ADVANCED_OPTIONS );
	}

	private boolean isEnoughSpaceAvailableBelowTheButton( Point disclosureButtonLocation, int accessTokenDialogHeight )
	{
		Dimension rootWindowSize = SoapUI.getFrame().getSize();
		return disclosureButtonLocation.getY() + accessTokenDialogHeight <= rootWindowSize.getHeight();
	}

	private void setAccessTokenFormDialogBoundsBelowTheButton( Point disclosureButtonLocation, JDialog accessTokenFormDialog, int disclosureButtonHeight )
	{
		accessTokenFormDialog.setLocation( ( int )disclosureButtonLocation.getX() - ACCESS_TOKEN_DIALOG_HORIZONTAL_OFFSET,
				( int )disclosureButtonLocation.getY() + disclosureButtonHeight );
	}

	private void setAccessTokenFormDialogBoundsAboveTheButton( Point disclosureButtonLocation, JDialog accessTokenFormDialog )
	{
		accessTokenFormDialog.setLocation( ( int )disclosureButtonLocation.getX() - ACCESS_TOKEN_DIALOG_HORIZONTAL_OFFSET,
				( int )disclosureButtonLocation.getY() - accessTokenFormDialog.getHeight() );
	}

	// TODO Make this reusable at some later point
	private JDialog createAccessTokenDialog( JPanel accessTokenFormPanel )
	{
		final JDialog accessTokenFormDialog = new JDialog();
		accessTokenFormDialog.setUndecorated( true );
		accessTokenFormDialog.getContentPane().add( accessTokenFormPanel );

		return accessTokenFormDialog;
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

		accessTokenForm.addSpace( GROUP_SPACING );

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
		accessTokenForm.addSpace( NORMAL_SPACING );

		JLabel accessTokenDocumentationLink = getLabelLink( "http://www.soapui.org",
				"How to get an access token from an authorization server" );
		accessTokenForm.addComponent( accessTokenDocumentationLink );
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