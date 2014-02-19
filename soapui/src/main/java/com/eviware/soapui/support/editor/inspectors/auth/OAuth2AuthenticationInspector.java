package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.OAuth2ProfileContainer;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.actions.oauth.GetOAuthAccessTokenAction;
import com.eviware.soapui.impl.rest.actions.oauth.RefreshOAuthAccessTokenAction;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.components.SimpleForm;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.value.AbstractValueModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static javax.swing.BorderFactory.createCompoundBorder;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BorderFactory.createLineBorder;

public final class OAuth2AuthenticationInspector extends BasicAuthenticationInspector<RestRequest>
{
	public static final String OAUTH_2_FLOW_COMBO_BOX_NAME = "OAuth2Flow";

	private static final String OAUTH_2_FORM_LABEL = "OAuth 2 form";
	public static final String ADVANCED_OPTIONS = "Advanced...";
	public static final int ACCESS_TOKEN_DIALOG_HORIZONTAL_OFFSET = 120;
	public static final String REFRESH_ACCESS_TOKEN_BUTTON_NAME = "refreshAccessTokenButton";
	public static final String ACCESS_TOKEN_FORM_DIALOG_NAME = "getAccessTokenFormDialog";

	private OAuth2Profile profile;
	private SimpleBindingForm oAuth2Form;
	private JTextField clientSecretField;
	private JPanel wrapperPanel;
	private boolean disclosureButtonDisabled;
	private boolean isMouseOnDisclosureLabel;

	protected OAuth2AuthenticationInspector( RestRequest request )
	{
		super( request );
	}

	@Override
	protected void buildUI()
	{
		super.buildUI();
		profile = getOAuth2Profile( request );
		oAuth2Form = new SimpleBindingForm( new PresentationModel<OAuth2Profile>( profile ) );
		addOAuth2Panel();
	}

	@Override
	public void release()
	{
		super.release();

		oAuth2Form.getPresentationModel().release();
	}

	@Override
	String getFormTypeForSelection( String selectedItem )
	{
		if( selectedItem.equals( CredentialsConfig.AuthType.O_AUTH_2.toString() ) )
		{
			return OAUTH_2_FORM_LABEL;
		}
		else
		{
			return super.getFormTypeForSelection( selectedItem );
		}
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
		} );
		oAuthDocumentationLink.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
		return oAuthDocumentationLink;
	}

	@Override
	protected ArrayList<String> getAuthenticationTypes()
	{
		ArrayList<String> authenticationTypes = super.getAuthenticationTypes();
		authenticationTypes.add( CredentialsConfig.AuthType.O_AUTH_2.toString() );
		return authenticationTypes;
	}

	private void populateOAuth2Form( SimpleBindingForm oAuth2Form )
	{
		initForm( oAuth2Form );

		oAuth2Form.addSpace( TOP_SPACING );

		final JButton refreshAccessTokenButton = addAccessTokenFieldAndRefreshTokenButton( oAuth2Form );

		oAuth2Form.addInputFieldHintText( "Enter existing access token, or use \"Get Token\" below." );

		SimpleBindingForm accessTokenForm = new SimpleBindingForm( new PresentationModel<OAuth2Profile>( profile ) );
		populateGetAccessTokenForm( accessTokenForm );

		final JPanel accessTokenFormPanel = accessTokenForm.getPanel();
		accessTokenFormPanel.setBorder( createCompoundBorder( createLineBorder( CARD_BORDER_COLOR ),
				createEmptyBorder( 10, 10, 10, 10 ) ) );

		final JLabel disclosureButton = new JLabel( "▼ Get Token" );
		disclosureButton.setName( "oAuth2DisclosureButton" );
		oAuth2Form.addComponentWithoutLabel( disclosureButton );

		final JDialog accessTokenFormDialog = createAccessTokenDialog( accessTokenFormPanel );
		disclosureButton.addMouseListener( new DisclosureButtonMouseListener( accessTokenFormDialog, disclosureButton ) );

		accessTokenFormDialog.addWindowFocusListener( new AccessTokenFormDialogWindowListener( accessTokenFormDialog,
				disclosureButton ) );


		JButton advancedOptionsButton = oAuth2Form.addButtonWithoutLabelToTheRight( ADVANCED_OPTIONS, new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				new OAuth2AdvanceOptionsDialog( profile, refreshAccessTokenButton );
			}
		} );
		advancedOptionsButton.setName( ADVANCED_OPTIONS );
	}

	private JButton addAccessTokenFieldAndRefreshTokenButton( SimpleBindingForm oAuth2Form )
	{
		JTextField accessTokenField = new JTextField(  );
		accessTokenField.setName( OAuth2Profile.ACCESS_TOKEN_PROPERTY );
		accessTokenField.setColumns( SimpleForm.MEDIUM_TEXT_FIELD_COLUMNS  );
		Bindings.bind( accessTokenField, oAuth2Form.getPresentationModel().getModel( OAuth2Profile.ACCESS_TOKEN_PROPERTY ) );

		final JButton refreshAccessTokenButton = new JButton( "Refresh" );
		refreshAccessTokenButton.setName( REFRESH_ACCESS_TOKEN_BUTTON_NAME );
		refreshAccessTokenButton.addActionListener( new RefreshOAuthAccessTokenAction( profile ) );

		boolean enabled = profile.getRefreshAccessTokenMethod().equals( OAuth2Profile.RefreshAccessTokenMethods.MANUAL )
				&& ( !StringUtils.isNullOrEmpty( profile.getRefreshToken() ) );
		refreshAccessTokenButton.setVisible( enabled );

		JPanel wrapperPanel = new JPanel( new BorderLayout( 5,5 ) );
		wrapperPanel.setBackground( CARD_BACKGROUND_COLOR );
		wrapperPanel.add( accessTokenField, BorderLayout.WEST );
		wrapperPanel.add( refreshAccessTokenButton, BorderLayout.EAST );
		oAuth2Form.append( "Access Token", wrapperPanel );

		return refreshAccessTokenButton;
	}

	private boolean isEnoughSpaceAvailableBelowTheButton( Point disclosureButtonLocation, int accessTokenDialogHeight, int disclosureButtonHeight )
	{
		GraphicsConfiguration currentGraphicsConfiguration = getGraphicsConfigurationForPosition( disclosureButtonLocation );
		if( currentGraphicsConfiguration == null )
		{
			return true;
		}
		double bottomYCoordinate = disclosureButtonLocation.getY() + accessTokenDialogHeight + disclosureButtonHeight;
		double bottomUsableYCoordinateOnScreen = currentGraphicsConfiguration.getBounds().getMaxY()
				- Toolkit.getDefaultToolkit().getScreenInsets( currentGraphicsConfiguration ).bottom;
		return bottomYCoordinate <= bottomUsableYCoordinateOnScreen;
	}

	private GraphicsConfiguration getGraphicsConfigurationForPosition( Point point )
	{
		for( GraphicsDevice graphicsDevice : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices() )
		{
			if( graphicsDevice.getDefaultConfiguration().getBounds().contains( point ) )
			{
				return graphicsDevice.getDefaultConfiguration();
			}
		}
		return null;
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

	private JDialog createAccessTokenDialog( JPanel accessTokenFormPanel )
	{
		final JDialog accessTokenFormDialog = new JDialog();
		accessTokenFormDialog.setName( ACCESS_TOKEN_FORM_DIALOG_NAME );
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
		oauth2FlowComboBox.setName( OAUTH_2_FLOW_COMBO_BOX_NAME );

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

		accessTokenForm.addButtonWithoutLabel( "Get Access Token", new GetOAuthAccessTokenAction( profile ) );
		accessTokenForm.appendLabel( OAuth2Profile.ACCESS_TOKEN_STATUS_PROPERTY, "Access token status" );

		JLabel accessTokenDocumentationLink = getLabelLink( "http://www.soapui.org",
				"How to get an access token from an authorization server" );
		accessTokenForm.addComponent( accessTokenDocumentationLink );
	}

	/**
	 * Currently there's only support for one profile per project
	 */
	private OAuth2Profile getOAuth2Profile( RestRequest request )
	{
		OAuth2ProfileContainer oAuth2ProfileContainer = request.getOperation().getInterface().getProject()
				.getOAuth2ProfileContainer();
		if(oAuth2ProfileContainer.getOAuth2ProfileList().isEmpty())
		{
			oAuth2ProfileContainer.addNewOAuth2Profile( "OAuth 2 - Profile" );
		}
		return oAuth2ProfileContainer.getOAuth2ProfileList().get( 0 );
	}

	private class DisclosureButtonMouseListener extends MouseAdapter
	{
		private final JDialog accessTokenFormDialog;
		private final JLabel disclosureButton;

		public DisclosureButtonMouseListener( JDialog accessTokenFormDialog, JLabel disclosureButton )
		{
			this.accessTokenFormDialog = accessTokenFormDialog;
			this.disclosureButton = disclosureButton;
		}

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
			disclosureButton.setText( "▲ Get Token" );
			if( isEnoughSpaceAvailableBelowTheButton( disclosureButtonLocation, accessTokenFormDialog.getHeight(), source.getHeight() ) )
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
	}

	private class AccessTokenFormDialogWindowListener implements WindowFocusListener
	{
		private final JDialog accessTokenFormDialog;
		private final JLabel disclosureButton;

		public AccessTokenFormDialogWindowListener( JDialog accessTokenFormDialog, JLabel disclosureButton )
		{
			this.accessTokenFormDialog = accessTokenFormDialog;
			this.disclosureButton = disclosureButton;
		}

		@Override
		public void windowGainedFocus( WindowEvent e )
		{
			disclosureButtonDisabled = true;
		}

		@Override
		public void windowLostFocus( WindowEvent e )
		{
			accessTokenFormDialog.setVisible( false );
			disclosureButton.setText( "▼ Get Token" );
			// If the focus is lost due to click on the disclosure button then don't enable it yet, since it
			// will then show the dialog directly again.
			if( !isMouseOnDisclosureLabel )
			{
				disclosureButtonDisabled = false;
			}
		}
	}
}