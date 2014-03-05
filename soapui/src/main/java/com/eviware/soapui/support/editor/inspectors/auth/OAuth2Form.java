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

import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.actions.oauth.RefreshOAuthAccessTokenAction;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.components.SimpleForm;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.Bindings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


/**
 *
 */
public class OAuth2Form extends AbstractAuthenticationForm
{


	public static final String ADVANCED_OPTIONS = "Advanced...";
	public static final int ACCESS_TOKEN_DIALOG_HORIZONTAL_OFFSET = 120;
	public static final String REFRESH_ACCESS_TOKEN_BUTTON_NAME = "refreshAccessTokenButton";

	private OAuth2Profile profile;
	private JPanel formPanel;
	private boolean disclosureButtonDisabled;
	private boolean isMouseOnDisclosureLabel;

	public OAuth2Form( OAuth2Profile profile )
	{
		super();
		this.profile = profile;
	}

	@Override
	protected JPanel buildUI()
	{
		SimpleBindingForm oAuth2Form = new SimpleBindingForm( new PresentationModel<OAuth2Profile>( profile ) );
		addOAuth2Panel( oAuth2Form );
		return formPanel;
	}

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

		final JButton refreshAccessTokenButton = addAccessTokenFieldAndRefreshTokenButton( oAuth2Form );

		oAuth2Form.addInputFieldHintText( "Enter existing access token, or use \"Get Token\" below." );

		final JLabel disclosureButton = new JLabel( "▼ Get Token" );
		disclosureButton.setName( "oAuth2DisclosureButton" );
		oAuth2Form.addComponentWithoutLabel( disclosureButton );

		OAuth2AccessTokenForm accessTokenForm = new OAuth2AccessTokenForm( profile );
		final JDialog accessTokenFormDialog = accessTokenForm.getComponent();

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
