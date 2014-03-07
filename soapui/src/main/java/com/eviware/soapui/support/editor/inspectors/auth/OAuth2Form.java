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
import com.eviware.soapui.impl.rest.actions.oauth.RefreshOAuthAccessTokenAction;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.Bindings;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class OAuth2Form extends AbstractAuthenticationForm implements OAuth2AccessTokenStatusChangeListener
{
	public static final String ADVANCED_OPTIONS_BUTTON_NAME = "Advanced...";
	public static final String REFRESH_ACCESS_TOKEN_BUTTON_NAME = "refreshAccessTokenButton";

	private static final int ACCESS_TOKEN_DIALOG_HORIZONTAL_OFFSET = 120;

	private static final Dimension HORIZONAL_SPACING_IN_ACCESS_TOKEN_ROW = new Dimension( 5, 0 );
	private static final String ACCESS_TOKEN_LABEL = "Access Token";
	private static final Insets ACCESS_TOKEN_FIELD_INSETS = new Insets( 5, 5, 5, 5 );
	private static final float ACCESS_TOKEN_STATUS_TEXT_FONT_SCALE = 0.95f;
	private static final int ACCESS_TOKEN_STATUS_TEXT_WIDTH = 100;

	private final Color DEFAULT_COLOR = Color.WHITE;
	private final Color SUCCESS_COLOR = new Color( 0xccffcb );

	// FIXME This need to be changed to the real icons
	static final ImageIcon SUCCESS_ICON = UISupport.createImageIcon( "/checkmark-dummy.png" );
	static final ImageIcon WAIT_ICON = UISupport.createImageIcon( "/refresh-dummy.png" );

	private final AbstractXmlInspector inspector;
	private final OAuth2AccessTokenStatusChangeManager statusChangeManager;
	private OAuth2Profile profile;
	private JPanel formPanel;
	private boolean disclosureButtonDisabled;
	private boolean isMouseOnDisclosureLabel;

	private SimpleBindingForm oAuth2Form;

	private JTextField accessTokenField;
	private JLabel accessTokenStatusIcon;
	private JLabel accessTokenStatusText;
	private OAuth2GetAccessTokenForm accessTokenForm;

	public OAuth2Form( OAuth2Profile profile, AbstractXmlInspector inspector )
	{
		super();
		this.profile = profile;
		this.inspector = inspector;
		statusChangeManager = new OAuth2AccessTokenStatusChangeManager( this );
	}

	void release()
	{
		accessTokenForm.release();
		oAuth2Form.getPresentationModel().release();
		statusChangeManager.unregister();
	}

	@Override
	public void onAccessTokenStatusChanged( OAuth2Profile.AccessTokenStatus status )
	{
		setAccessTokenStatusFeedback( status );
	}

	@Nonnull
	@Override
	public OAuth2Profile getProfile()
	{
		return profile;
	}

	@Override
	protected JPanel buildUI()
	{
		oAuth2Form = new SimpleBindingForm( new PresentationModel<OAuth2Profile>( profile ) );
		addOAuth2Panel( oAuth2Form );
		statusChangeManager.register();
		setAccessTokenStatusFeedback( profile.getAccesTokenStatusAsEnum() );
		return formPanel;
	}

	// TODO Use the refactored SimpleBidningsForm instead
	private void addOAuth2Panel( SimpleBindingForm oAuth2Form )
	{
		populateOAuth2Form( oAuth2Form );

		formPanel = new JPanel( new BorderLayout() );

		JPanel centerPanel = oAuth2Form.getPanel();
		setBackgroundColorOnPanel( centerPanel );

		JPanel southPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		JLabel oAuthDocumentationLink = UISupport.createLabelLink( "http://www.soapui.org", "Learn about OAuth 2" );
		southPanel.add( oAuthDocumentationLink );

		southPanel.setBorder( BorderFactory.createMatteBorder( 1, 0, 0, 0, CARD_BORDER_COLOR ) );
		setBackgroundColorOnPanel( southPanel );

		formPanel.add( centerPanel, BorderLayout.CENTER );
		formPanel.add( southPanel, BorderLayout.SOUTH );

		setBorderOnPanel( formPanel );
	}

	private void populateOAuth2Form( SimpleBindingForm oAuth2Form )
	{
		initForm( oAuth2Form );

		oAuth2Form.addSpace( TOP_SPACING );

		JTextField accessTokenField = createAccessTokenField();
		JLabel accessTokenStatusIcon = createAccessTokenStatusIcon();
		JLabel accessTokenStatusText = createAccessTokenStatusText();

		final JButton refreshAccessTokenButton = createRefreshButton();

		JPanel accessTokenRowPanel = createAccessTokenRowPanel( accessTokenField, accessTokenStatusIcon, accessTokenStatusText, refreshAccessTokenButton );
		oAuth2Form.append( ACCESS_TOKEN_LABEL, accessTokenRowPanel );

		oAuth2Form.addInputFieldHintText( "Enter existing access token, or use \"Get Token\" below." );

		final JLabel disclosureButton = new JLabel( "▼ Get Token" );
		disclosureButton.setName( "oAuth2DisclosureButton" );
		oAuth2Form.addComponentWithoutLabel( disclosureButton );

		accessTokenForm = new OAuth2GetAccessTokenForm( profile );
		final JDialog accessTokenFormDialog = accessTokenForm.getComponent();

		disclosureButton.addMouseListener( new DisclosureButtonMouseListener( accessTokenFormDialog, disclosureButton ) );

		accessTokenFormDialog.addWindowFocusListener( new AccessTokenFormDialogWindowListener( accessTokenFormDialog, disclosureButton ) );

		JButton advancedOptionsButton = oAuth2Form.addButtonWithoutLabelToTheRight( ADVANCED_OPTIONS_BUTTON_NAME, new ActionListener()
		{
			@Override
			public void actionPerformed( ActionEvent e )
			{
				new OAuth2AdvanceOptionsDialog( profile, refreshAccessTokenButton );
			}
		} );
		advancedOptionsButton.setName( ADVANCED_OPTIONS_BUTTON_NAME );
	}

	private JTextField createAccessTokenField()
	{
		accessTokenField = new JTextField();
		accessTokenField.setName( OAuth2Profile.ACCESS_TOKEN_PROPERTY );
		accessTokenField.setColumns( SimpleForm.MEDIUM_TEXT_FIELD_COLUMNS );
		accessTokenField.setMargin( ACCESS_TOKEN_FIELD_INSETS );
		Bindings.bind( accessTokenField, oAuth2Form.getPresentationModel().getModel( OAuth2Profile.ACCESS_TOKEN_PROPERTY ) );
		return accessTokenField;
	}

	private JLabel createAccessTokenStatusIcon()
	{
		accessTokenStatusIcon = new JLabel();
		accessTokenStatusIcon.setVisible( false );
		return accessTokenStatusIcon;
	}

	private JLabel createAccessTokenStatusText()
	{
		accessTokenStatusText = new JLabel();
		accessTokenStatusText.setFont( scaledFont( accessTokenStatusText, ACCESS_TOKEN_STATUS_TEXT_FONT_SCALE ) );
		accessTokenStatusText.setVisible( false );
		accessTokenStatusText.setAlignmentX( Component.CENTER_ALIGNMENT );

		return accessTokenStatusText;
	}

	private JButton createRefreshButton()
	{
		final JButton refreshAccessTokenButton = new JButton( "Refresh" );
		refreshAccessTokenButton.setName( REFRESH_ACCESS_TOKEN_BUTTON_NAME );
		refreshAccessTokenButton.addActionListener( new RefreshOAuthAccessTokenAction( profile ) );
		boolean enabled = profile.getRefreshAccessTokenMethod().equals( OAuth2Profile.RefreshAccessTokenMethods.MANUAL )
				&& ( !StringUtils.isNullOrEmpty( profile.getRefreshToken() ) );
		refreshAccessTokenButton.setVisible( enabled );
		return refreshAccessTokenButton;
	}

	private JPanel createAccessTokenRowPanel( JTextField accessTokenField, JLabel accessTokenStatusIcon, JLabel accessTokenStatusText, JButton refreshAccessTokenButton )
	{
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
		panel.setBackground( CARD_BACKGROUND_COLOR );
		panel.add( accessTokenField );
		panel.add( Box.createRigidArea( HORIZONAL_SPACING_IN_ACCESS_TOKEN_ROW ) );
		panel.add( accessTokenStatusIcon );
		panel.add( Box.createRigidArea( HORIZONAL_SPACING_IN_ACCESS_TOKEN_ROW ) );
		panel.add( accessTokenStatusText );
		panel.add( Box.createRigidArea( HORIZONAL_SPACING_IN_ACCESS_TOKEN_ROW ) );
		panel.add( refreshAccessTokenButton );
		return panel;
	}

	private Font scaledFont( JComponent component, float scale )
	{
		Font currentFont = component.getFont();
		return currentFont.deriveFont( ( float )currentFont.getSize() * scale );
	}

	private String setWrappedText( String text )
	{
		return String.format( "<html><div WIDTH=%d>%s</div><html>", OAuth2Form.ACCESS_TOKEN_STATUS_TEXT_WIDTH, text );
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

	private void setAccessTokenStatusFeedback( OAuth2Profile.AccessTokenStatus status )
	{
		// These are no auth profile selected
		if( status == null )
		{
			setDefaultFeedback();
		}
		else
		{
			switch( status )
			{
				case ENTERED_MANUALLY:
				case RETRIEVED_FROM_SERVER:
					setSucessfullFeedback( status );
					break;
				case WAITING_FOR_AUTHORIZATION:
				case RECEIVED_AUTHORIZATION_CODE:
					setWaitingFeedback( status );
					break;
				default:
					setDefaultFeedback();
					break;
			}
		}
	}

	private void setSucessfullFeedback( OAuth2Profile.AccessTokenStatus status )
	{
		accessTokenField.setBackground( SUCCESS_COLOR );

		accessTokenStatusIcon.setIcon( SUCCESS_ICON );
		accessTokenStatusIcon.setVisible( true );

		accessTokenStatusText.setText( setWrappedText( status.toString() ) );
		accessTokenStatusText.setVisible( true );

		inspector.setIcon( SUCCESS_ICON );
	}

	private void setWaitingFeedback( OAuth2Profile.AccessTokenStatus status )
	{
		accessTokenField.setBackground( DEFAULT_COLOR );

		accessTokenStatusIcon.setIcon( WAIT_ICON );
		accessTokenStatusIcon.setVisible( true );

		accessTokenStatusText.setText( setWrappedText( status.toString() ) );
		accessTokenStatusText.setVisible( true );

		inspector.setIcon( WAIT_ICON );
	}

	private void setDefaultFeedback()
	{
		accessTokenField.setBackground( DEFAULT_COLOR );

		accessTokenStatusIcon.setIcon( null );
		accessTokenStatusIcon.setVisible( false );

		accessTokenStatusText.setText( "" );
		accessTokenStatusText.setVisible( false );

		inspector.setIcon( ProfileSelectionForm.AUTH_ENABLED_ICON );
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
			if( UISupport.isEnoughSpaceAvailableBelowComponent( disclosureButtonLocation, accessTokenFormDialog.getHeight(), source.getHeight() ) )
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
			if( isMouseOnComponent( SoapUI.getFrame() ) && !isMouseOnComponent( accessTokenFormDialog ) )
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

		// TODO This might be extracted to a common utils class
		private boolean isMouseOnComponent( Component component )
		{
			Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
			Point componentLocationOnScreen = component.getLocationOnScreen();
			return component.contains( mouseLocation.x - componentLocationOnScreen.x, mouseLocation.y - componentLocationOnScreen.y );
		}
	}
}
