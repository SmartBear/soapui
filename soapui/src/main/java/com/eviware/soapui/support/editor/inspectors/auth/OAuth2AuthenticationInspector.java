package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.actions.oauth.GetOAuthAccessTokenAction;
import com.eviware.soapui.impl.rest.actions.oauth.RefreshOAuthAccessTokenAction;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
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
import static javax.swing.BorderFactory.*;

public final class OAuth2AuthenticationInspector extends BasicAuthenticationInspector<RestRequest>
{
	public static final String OAUTH_2_FLOW_COMBO_BOX_NAME = "OAuth2Flow";

	private static final String OAUTH_2_FORM_LABEL = "OAuth 2 form";
	public static final String ADVANCED_OPTIONS = "Advanced...";
	public static final int ACCESS_TOKEN_DIALOG_HORIZONTAL_OFFSET = 120;
	public static final String REFRESH_ACCESS_TOKEN_BUTTON_NAME = "refreshAccessTokenButton";
	public static final String ACCESS_TOKEN_FORM_DIALOG_NAME = "getAccessTokenFormDialog";
	public static final String CLIENT_IDENTIFICATION = "Client Identification";
	public static final String CLIENT_SECRET = "Client Secret";
	public static final String AUTHORIZATION_URI = "Authorization URI";
	public static final String ACCESS_TOKEN_URI = "Access Token URI";
	public static final String REDIRECT_URI = "Redirect URI";
	public static final String SCOPE = "Scope";

	private static final String ACCESS_TOKEN_FORM_DIALOG_TITLE = "Get Access Token";

	private OAuth2Profile profile;
	private SimpleBindingForm oAuth2Form;
	private JTextField clientSecretField;
	private JPanel wrapperPanel;
	private boolean disclosureButtonDisabled;
	private boolean isMouseOnDisclosureLabel;
	private JDialog accessTokenFormDialog;

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
		JLabel oAuthDocumentationLink = UISupport.createLabelLink( "http://www.soapui.org", "Learn about OAuth 2" );
		southPanel.add( oAuthDocumentationLink );

		southPanel.setBorder( BorderFactory.createMatteBorder( 1, 0, 0, 0, CARD_BORDER_COLOR ) );
		setBackgroundColorOnPanel( southPanel );

		wrapperPanel.add( centerPanel, BorderLayout.CENTER );
		wrapperPanel.add( southPanel, BorderLayout.SOUTH );

		setBorderOnPanel( wrapperPanel );

		getCardPanel().add( wrapperPanel, OAUTH_2_FORM_LABEL );
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
		JPanel wrapperPanel = new JPanel( new BorderLayout() );
		wrapperPanel.add( accessTokenFormPanel, BorderLayout.NORTH );

		JLabel accessTokenDocumentationLink = UISupport.createLabelLink( "http://www.soapui.org",
				"How to get an access token from an authorization server" );
		accessTokenDocumentationLink.setBorder( createEmptyBorder( 10, 5, 0, 0 ) );
		wrapperPanel.add( accessTokenDocumentationLink, BorderLayout.SOUTH );

		wrapperPanel.setBorder( createCompoundBorder( createLineBorder( CARD_BORDER_COLOR ),
				createEmptyBorder( 10, 10, 10, 10 ) ) );

		final JLabel disclosureButton = new JLabel( "▼ Get Token" );
		disclosureButton.setName( "oAuth2DisclosureButton" );
		oAuth2Form.addComponentWithoutLabel( disclosureButton );

		accessTokenFormDialog = createAccessTokenDialog( wrapperPanel );
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
		JTextField accessTokenField = new JTextField();
		accessTokenField.setName( OAuth2Profile.ACCESS_TOKEN_PROPERTY );
		accessTokenField.setColumns( SimpleForm.MEDIUM_TEXT_FIELD_COLUMNS );
		Bindings.bind( accessTokenField, oAuth2Form.getPresentationModel().getModel( OAuth2Profile.ACCESS_TOKEN_PROPERTY ) );

		final JButton refreshAccessTokenButton = new JButton( "Refresh" );
		refreshAccessTokenButton.setName( REFRESH_ACCESS_TOKEN_BUTTON_NAME );
		refreshAccessTokenButton.addActionListener( new RefreshOAuthAccessTokenAction( profile ) );

		boolean enabled = profile.getRefreshAccessTokenMethod().equals( OAuth2Profile.RefreshAccessTokenMethods.MANUAL )
				&& ( !StringUtils.isNullOrEmpty( profile.getRefreshToken() ) );
		refreshAccessTokenButton.setVisible( enabled );

		JPanel wrapperPanel = new JPanel( new BorderLayout( 5, 5 ) );
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
		accessTokenFormDialog.setTitle( ACCESS_TOKEN_FORM_DIALOG_TITLE );
		accessTokenFormDialog.setIconImages( SoapUI.getFrameIcons() );
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

		AbstractValueModel valueModel = accessTokenForm.getPresentationModel().getModel( OAuth2Profile.OAUTH2_FLOW_PROPERTY,
				"getOAuth2Flow", "setOAuth2Flow" );
		ComboBoxModel oauth2FlowsModel = new DefaultComboBoxModel<OAuth2Profile.OAuth2Flow>( OAuth2Profile.OAuth2Flow.values() );
		JComboBox oauth2FlowComboBox = accessTokenForm.appendComboBox( "OAuth2.0 Flow", oauth2FlowsModel, "OAuth2.0 Authorization Flow", valueModel );
		oauth2FlowComboBox.setName( OAUTH_2_FLOW_COMBO_BOX_NAME );

		accessTokenForm.addSpace( GROUP_SPACING );

		accessTokenForm.appendTextField( OAuth2Profile.CLIENT_ID_PROPERTY, CLIENT_IDENTIFICATION, null );
		clientSecretField = accessTokenForm.appendTextField( OAuth2Profile.CLIENT_SECRET_PROPERTY, CLIENT_SECRET, "" );
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
					accessTokenFormDialog.pack();
				}
			}
		} );

		accessTokenForm.addSpace( GROUP_SPACING );

		accessTokenForm.appendTextField( OAuth2Profile.AUTHORIZATION_URI_PROPERTY, AUTHORIZATION_URI, "" );
		accessTokenForm.appendTextField( OAuth2Profile.ACCESS_TOKEN_URI_PROPERTY, ACCESS_TOKEN_URI, "" );
		accessTokenForm.appendTextField( OAuth2Profile.REDIRECT_URI_PROPERTY, REDIRECT_URI, "" );

		accessTokenForm.addSpace( GROUP_SPACING );

		accessTokenForm.appendTextField( OAuth2Profile.SCOPE_PROPERTY, SCOPE, "" );

		accessTokenForm.addSpace( NORMAL_SPACING );
		JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		buttonPanel.add( new JButton( new GetOAuthAccessTokenAction( profile ) ) );
		buttonPanel.add( new JButton( new EditAutomationScriptsAction( profile ) ) );
		accessTokenForm.addComponent( buttonPanel );
		accessTokenForm.appendLabel( OAuth2Profile.ACCESS_TOKEN_STATUS_PROPERTY, "Access token status" );
	}

	/**
	 * Currently there's only support for one profile per project
	 */
	private OAuth2Profile getOAuth2Profile( RestRequest request )
	{
		List<OAuth2Profile> oAuth2ProfileList = request.getOperation().getInterface().getProject()
				.getOAuth2ProfileContainer().getOAuth2ProfileList();
		checkArgument( oAuth2ProfileList.size() == 1, "There should be one OAuth 2 profile configured on the project" );
		return oAuth2ProfileList.get( 0 );
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
			Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
			if( SoapUI.getFrame().contains( mouseLocation ) )
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

	private class EditAutomationScriptsAction extends AbstractAction
	{
		private final OAuth2Profile profile;

		public EditAutomationScriptsAction( OAuth2Profile profile )
		{
			putValue( Action.NAME, "Automation..." );
			this.profile = profile;
		}

		@Override
		public void actionPerformed( ActionEvent e )
		{
			if( accessTokenFormDialog != null )
			{
				accessTokenFormDialog.setVisible( false );
				accessTokenFormDialog.dispose();
			}
			UISupport.showDesktopPanel(new OAuth2ScriptsDesktopPanel( profile ));
		}
	}
}