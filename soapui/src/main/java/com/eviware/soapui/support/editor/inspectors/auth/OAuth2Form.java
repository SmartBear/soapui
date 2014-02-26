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
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.Bindings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static javax.swing.BorderFactory.createEmptyBorder;

public class OAuth2Form extends AbstractAuthenticationForm
{
	public static final String ADVANCED_OPTIONS = "Advanced...";
	public static final int ACCESS_TOKEN_DIALOG_HORIZONTAL_OFFSET = 120;
	public static final String REFRESH_ACCESS_TOKEN_BUTTON_NAME = "refreshAccessTokenButton";

	private static final String ACCESS_TOKEN_LABEL = "Access Token";

	public static final Dimension HORIZONAL_COMPONENT_SPACING = new Dimension( 5, 0 );
	private static final Insets ACCESS_TOKEN_FIELD_INSETS = new Insets( 5, 5, 5, 5 );
	private static final float ACCESS_TOKEN_STATUS_FONT_SCALE = 0.96f;
	private final AbstractXmlInspector inspector;

	private OAuth2Profile profile;
	private JPanel formPanel;
	private boolean disclosureButtonDisabled;
	private boolean isMouseOnDisclosureLabel;

	private OAuth2StatusPropertyChangeListener oAuth2StatusPropertyChangeListener;
	private SimpleBindingForm oAuth2Form;

	public OAuth2Form( OAuth2Profile profile, AbstractXmlInspector inspector )
	{
		super();
		this.profile = profile;
		this.inspector = inspector;
	}

	protected void release()
	{
		oAuth2Form.getPresentationModel().release();
		oAuth2StatusPropertyChangeListener.release();
	}

	@Override
	protected JPanel buildUI()
	{
		oAuth2Form = new SimpleBindingForm( new PresentationModel<OAuth2Profile>( profile ) );
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
		JLabel oAuthDocumentationLink = UISupport.getLabelAsLink( "http://www.soapui.org", "Learn about OAuth 2" );
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

		JTextField accessTokenField = createAccessTokenField(  );
		JLabel accessTokenStatusIcon = createAccessTokenStatusIcon();
		JTextArea accessTokenStatusText = createAccessTokenStatusText();

		oAuth2StatusPropertyChangeListener = new OAuth2StatusPropertyChangeListener( accessTokenField, accessTokenStatusIcon, accessTokenStatusText );
		profile.addPropertyChangeListener( oAuth2StatusPropertyChangeListener );

		final JButton refreshAccessTokenButton = createRefreshButton();

		JPanel accessTokenRowPanel = createAccessTokenRowPanel( accessTokenField, accessTokenStatusIcon, accessTokenStatusText, refreshAccessTokenButton );
		oAuth2Form.append( ACCESS_TOKEN_LABEL, accessTokenRowPanel );

		oAuth2Form.addInputFieldHintText( "Enter existing access token, or use \"Get Token\" below." );

		final JLabel disclosureButton = new JLabel( "▼ Get Token" );
		disclosureButton.setName( "oAuth2DisclosureButton" );
		oAuth2Form.addComponentWithoutLabel( disclosureButton );

		OAuth2AccessTokenForm accessTokenForm = new OAuth2AccessTokenForm();
		final JDialog accessTokenFormDialog = accessTokenForm.getComponent( profile );

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

	private JTextField createAccessTokenField( )
	{
		JTextField accessTokenField = new JTextField();
		accessTokenField.setName( OAuth2Profile.ACCESS_TOKEN_PROPERTY );
		accessTokenField.setColumns( SimpleForm.MEDIUM_TEXT_FIELD_COLUMNS );
		accessTokenField.setMargin( ACCESS_TOKEN_FIELD_INSETS );
		Bindings.bind( accessTokenField, oAuth2Form.getPresentationModel().getModel( OAuth2Profile.ACCESS_TOKEN_PROPERTY ) );
		return accessTokenField;
	}

	private JLabel createAccessTokenStatusIcon()
	{
		JLabel accessTokenStatusIcon = new JLabel();
		accessTokenStatusIcon.setVisible( false );
		return accessTokenStatusIcon;
	}

	private JTextArea createAccessTokenStatusText()
	{
		JTextArea accessTokenStatusText = new JTextArea();
		accessTokenStatusText.setFont( scaledFont( accessTokenStatusText, ACCESS_TOKEN_STATUS_FONT_SCALE ) );
		accessTokenStatusText.setLineWrap( true );
		accessTokenStatusText.setColumns( 10 );
		accessTokenStatusText.setBackground( CARD_BACKGROUND_COLOR );
		accessTokenStatusText.setBorder( createEmptyBorder() );
		accessTokenStatusText.setVisible( false );
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

	private JPanel createAccessTokenRowPanel( JTextField accessTokenField, JLabel accessTokenStatusIcon, JTextArea accessTokenStatusText, JButton refreshAccessTokenButton )
	{
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );
		panel.setBackground( CARD_BACKGROUND_COLOR );
		panel.add( accessTokenField );
		panel.add( Box.createRigidArea( HORIZONAL_COMPONENT_SPACING ) );
		panel.add( accessTokenStatusIcon );
		panel.add( Box.createRigidArea( HORIZONAL_COMPONENT_SPACING ) );
		panel.add( accessTokenStatusText );
		panel.add( Box.createRigidArea( HORIZONAL_COMPONENT_SPACING ) );
		panel.add( refreshAccessTokenButton );
		return panel;
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

	private Font scaledFont( JComponent component, float scale )
	{
		Font currentFont = component.getFont();
		return currentFont.deriveFont( ( float )currentFont.getSize() * scale );
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

	private class OAuth2StatusPropertyChangeListener implements PropertyChangeListener
	{
		private final Color SUCCESS_COLOR = new Color( 0xccffcb );
		// FIXME This need to be changed to the real icon
		private final ImageIcon SUCCESS_ICON = UISupport.createImageIcon( "/testCase.gif" );


		private final JTextField accessTokenField;
		private final JLabel accessTokenStatusIcon;
		private final JTextArea accessTokenStatusText;

		public OAuth2StatusPropertyChangeListener( JTextField accessTokenField, JLabel accessTokenStatusIcon, JTextArea accessTokenStatusText )
		{
			this.accessTokenField = accessTokenField;
			this.accessTokenStatusIcon = accessTokenStatusIcon;
			this.accessTokenStatusText = accessTokenStatusText;
		}

		@Override
		public void propertyChange( PropertyChangeEvent evt )
		{
			if( evt.getPropertyName().equals( OAuth2Profile.ACCESS_TOKEN_STATUS_PROPERTY ) )
			{
				String status = ( String )evt.getNewValue();
				if( status.equals( OAuth2Profile.AccessTokenStatus.ENTERED_MANUALLY.toString() ) )
				{
					accessTokenField.setBackground( SUCCESS_COLOR );

					accessTokenStatusIcon.setIcon( SUCCESS_ICON );
					accessTokenStatusIcon.setVisible( true );

					accessTokenStatusText.setText( OAuth2Profile.AccessTokenStatus.ENTERED_MANUALLY.toString() );
					accessTokenStatusText.setVisible( true );

					inspector.setIcon( SUCCESS_ICON );
				}
			}
		}

		public void release()
		{
			profile.removePropertyChangeListener( this );
		}
	}
}
