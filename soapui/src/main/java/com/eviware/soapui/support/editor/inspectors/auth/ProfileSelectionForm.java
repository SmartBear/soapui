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

import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.rest.OAuth2Profile;
import com.eviware.soapui.impl.rest.OAuth2ProfileContainer;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

/**
 *
 */
public class ProfileSelectionForm<T extends AbstractHttpRequest> extends AbstractXmlInspector
{

	public static final String PROFILE_COMBO_BOX = "Authorization:";

	public static final String BASIC_FORM_LABEL = "Legacy form";
	public static final String WSS_FORM_LABEL = "WSS form";
	public static final String OPTIONS_SEPARATOR = "------------------";
	public static final String DELETE_PROFILE_DIALOG_TITLE = "Delete Profile";
	public static final String RENAME_PROFILE_DIALOG_TITLE = "Rename Profile";

	private static final String OAUTH_2_FORM_LABEL = "OAuth 2 form";
	public static final String EMPTY_PANEL = "EmptyPanel";

	private T request;
	private final JPanel outerPanel = new JPanel( new BorderLayout() );
	private final JPanel cardPanel = new JPanel( new CardLayout() );
	private JComboBox profileSelectionComboBox;
	private CellConstraints cc = new CellConstraints();
	private BasicAuthenticationForm<T> authenticationForm;

	protected ProfileSelectionForm( T request )
	{
		super( AuthInspectorFactory.INSPECTOR_ID, "Authentication and Security-related settings",
				true, AuthInspectorFactory.INSPECTOR_ID );
		this.request = request;

		buildUI();
	}

	@Override
	public JComponent getComponent()
	{
		profileSelectionComboBox.setSelectedItem( request.getSelectedAuthProfile() );
		return outerPanel;
	}

	@Override
	public boolean isEnabledFor( EditorView<XmlDocument> view )
	{
		return !view.getViewId().equals( RawXmlEditorFactory.VIEW_ID );
	}

	protected void buildUI()
	{
		JPanel innerPanel = new JPanel( new BorderLayout() );
		innerPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		JPanel comboBoxPanel = createAuthorizationLabelAndComboBox();

		innerPanel.add( comboBoxPanel, BorderLayout.PAGE_START );

		cardPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		cardPanel.add( createEmptyPanel(), EMPTY_PANEL );
		innerPanel.add( cardPanel, BorderLayout.CENTER );

		authenticationForm = new BasicAuthenticationForm<T>( request );
		cardPanel.add( authenticationForm.getComponent(), BASIC_FORM_LABEL );

		if( isSoapRequest( request ) )
		{
			WSSAuthenticationForm wssAuthenticationForm = new WSSAuthenticationForm( ( WsdlRequest )request );
			cardPanel.add( wssAuthenticationForm.getComponent(), WSS_FORM_LABEL );
		}

		outerPanel.add( new JScrollPane( innerPanel ), BorderLayout.CENTER );
	}

	private JPanel createEmptyPanel()
	{
		JPanel panelWithText = new JPanel( new BorderLayout() );
		String helpText = "<html><body><div style=\"text-align:center\">This request currently has no authorization configuration associated with it." +
				"<br>If you need to access a protected service, just add your " +
				"<br>configuration here, using the drop down above.</div></body></html>";
		JLabel label = new JLabel( helpText );
		label.setHorizontalAlignment( SwingConstants.CENTER );
		panelWithText.add( label, BorderLayout.CENTER );
		panelWithText.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createLineBorder( AbstractAuthenticationForm.CARD_BORDER_COLOR ),
				BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) ) );
		panelWithText.setBackground( AbstractAuthenticationForm.CARD_BACKGROUND_COLOR );
		return panelWithText;
	}

	private boolean isSoapRequest( T request )
	{
		return request instanceof WsdlRequest;
	}

	private JPanel createAuthorizationLabelAndComboBox()
	{
		FormLayout formLayout = new FormLayout( "5px:none,left:pref,40px,left:default,5px:grow(1.0)" );
		JPanel comboBoxPanel = new JPanel( formLayout );
		comboBoxPanel.setBorder( BorderFactory.createEmptyBorder( 0, 10, 0, 10 ) );

		JLabel authorizationLabel = new JLabel( PROFILE_COMBO_BOX );
		authorizationLabel.setBorder( BorderFactory.createEmptyBorder( 3, 0, 0, 0 ) );

		formLayout.appendRow( new RowSpec( "top:pref" ) );
		comboBoxPanel.add( authorizationLabel, cc.xy( 2, 1 ) );

		createProfileSelectionComboBox();
		comboBoxPanel.add( profileSelectionComboBox, cc.xy( 4, 1 ) );

		JPanel wrapperPanel = new JPanel( new BorderLayout( 5, 5 ) );
		wrapperPanel.add( comboBoxPanel, BorderLayout.LINE_START );
		wrapperPanel.add( UISupport.createFormButton( new ShowOnlineHelpAction( "http://www.soapui.org" ) ),
				BorderLayout.AFTER_LINE_ENDS );
		return wrapperPanel;
	}

	private void createProfileSelectionComboBox()
	{
		String[] existingProfiles = createOptionsForAuthorizationCombo( request.getSelectedAuthProfile() );

		profileSelectionComboBox = new JComboBox<String>( existingProfiles );
		profileSelectionComboBox.setName( PROFILE_COMBO_BOX );
		profileSelectionComboBox.addItemListener( new ProfileSelectionListener() );
	}

	private void setAuthenticationTypeAndShowCard( String selectedOption )
	{
		if( getAddEditOptions().contains( selectedOption ) )
		{
			performAddEditOperation( request.getSelectedAuthProfile(), selectedOption );
			return;
		}

		if( getBasicAuthenticationTypes().contains( selectedOption ) )
		{
			request.setSelectedAuthProfileAndAuthType( selectedOption, selectedOption );
			authenticationForm.setButtonGroupVisibility( selectedOption.equals( AbstractHttpRequest.BASIC_AUTH_PROFILE ) );
			if( isSoapRequest( request ) )
			{
				showCard( WSS_FORM_LABEL );
			}
			else
			{
				showCard( BASIC_FORM_LABEL );
			}
		}
		else if( isRestRequest( request ) && getOAuth2ProfileContainer().getOAuth2ProfileNameList().contains( selectedOption ) )
		{
			request.setSelectedAuthProfileAndAuthType( selectedOption, CredentialsConfig.AuthType.O_AUTH_2_0.toString() );
			OAuth2Form oAuth2Form = new OAuth2Form( getOAuth2ProfileContainer().getProfileByName( selectedOption ) );
			cardPanel.add( oAuth2Form.getComponent(), OAUTH_2_FORM_LABEL );
			showCard( OAUTH_2_FORM_LABEL );
		}
		else    //selectedItem : No Authorization
		{
			request.setSelectedAuthProfileAndAuthType( selectedOption, CredentialsConfig.AuthType.NO_AUTHORIZATION.toString() );
			showCard( EMPTY_PANEL );
		}
	}

	private void performAddEditOperation( final String currentProfile, String selectedOption )
	{
		AddEditOptions addEditOption = getAddEditOptionForDescription( selectedOption );
		switch( addEditOption )
		{
			case ADD:
				SwingUtilities.invokeLater( new Runnable()
				{
					@Override
					public void run()
					{
						new AuthorizationSelectionDialog<T>( request, getBasicAuthenticationTypes() );
						refreshProfileSelectionComboBox( request.getSelectedAuthProfile() );
					}
				} );
				break;
			case DELETE:
				SwingUtilities.invokeLater( new Runnable()
				{
					@Override
					public void run()
					{
						deleteCurrentProfile( currentProfile );
					}
				} );
				break;
			case RENAME:
				SwingUtilities.invokeLater( new Runnable()
				{
					@Override
					public void run()
					{
						renameCurrentProfile( currentProfile );
					}
				} );
				break;
			default:
				break;
		}
	}

	private void renameCurrentProfile( String profileOldName )
	{
		String newName = UISupport.prompt( "Specify name of Profile", RENAME_PROFILE_DIALOG_TITLE, profileOldName );
		if( newName == null || profileOldName.equals( newName ) )
		{
			profileSelectionComboBox.setSelectedItem( profileOldName );
			return;
		}

		if( newName.trim().equals( "" ) )
		{
			UISupport.showErrorMessage( "New name can't be empty." );
			profileSelectionComboBox.setSelectedItem( profileOldName );
			return;
		}

		if( getOAuth2ProfileContainer().getOAuth2ProfileNameList().contains( newName ) )
		{
			UISupport.showErrorMessage( "There is already a profile named '" + newName + "'" );
			profileSelectionComboBox.setSelectedItem( profileOldName );
			return;
		}

		OAuth2Profile profile = getOAuth2ProfileContainer().getProfileByName( profileOldName );
		profile.setName( newName );
		request.setSelectedAuthProfileAndAuthType( newName, request.getAuthType() );
		refreshProfileSelectionComboBox( newName );
	}

	private void deleteCurrentProfile( String profileName )
	{
		boolean confirmedDeletion = UISupport.confirm( "Do you really want to delete profile '" + profileName + "' ?",
				DELETE_PROFILE_DIALOG_TITLE );
		if( !confirmedDeletion )
		{
			refreshProfileSelectionComboBox( profileName );
			return;
		}

		if( isRestRequest( request ) && getOAuth2ProfileContainer().getOAuth2ProfileNameList().contains( profileName ) )
		{
			getOAuth2ProfileContainer().removeProfile( profileName );
		}
		else if( getBasicAuthenticationTypes().contains( profileName ) )
		{
			request.removeBasicAuthenticationProfile( profileName );
		}
		refreshProfileSelectionComboBox( CredentialsConfig.AuthType.NO_AUTHORIZATION.toString() );
	}

	private void refreshProfileSelectionComboBox( String selectedProfile )
	{
		DefaultComboBoxModel model = new DefaultComboBoxModel( createOptionsForAuthorizationCombo( selectedProfile ) );
		model.setSelectedItem( OPTIONS_SEPARATOR );
		profileSelectionComboBox.setModel( model );

		profileSelectionComboBox.removeItemListener( profileSelectionComboBox.getItemListeners()[0] );
		profileSelectionComboBox.addItemListener( new ProfileSelectionListener() );

		profileSelectionComboBox.setSelectedItem( selectedProfile );
	}

	private void showCard( String cardName )
	{
		CardLayout layout = ( CardLayout )cardPanel.getLayout();
		layout.show( cardPanel, cardName );
	}


	private OAuth2ProfileContainer getOAuth2ProfileContainer()
	{
		return request.getOperation().getInterface().getProject().getOAuth2ProfileContainer();
	}

	protected ArrayList<String> getBasicAuthenticationTypes()
	{
		ArrayList<String> options = new ArrayList<String>();
		options.add( AbstractHttpRequest.BASIC_AUTH_PROFILE );
		options.add( CredentialsConfig.AuthType.NTLM.toString() );
		options.add( CredentialsConfig.AuthType.SPNEGO_KERBEROS.toString() );
		return options;
	}

	private String[] createOptionsForAuthorizationCombo( String selectedAuthProfile )
	{
		ArrayList<String> options = new ArrayList<String>();
		options.add( CredentialsConfig.AuthType.NO_AUTHORIZATION.toString() );
		options.addAll( request.getBasicAuthenticationProfiles() );

		ArrayList<String> addEditOptions = getAddEditOptions();

		ArrayList<String> oAuth2Profiles = null;
		if( isRestRequest( request ) )
		{
			oAuth2Profiles = getOAuth2ProfileContainer().getOAuth2ProfileNameList();
			options.addAll( oAuth2Profiles );

		}
		if( oAuth2Profiles==null || !oAuth2Profiles.contains( selectedAuthProfile ) )
		{
			addEditOptions.remove( AddEditOptions.RENAME.getDescription() );
		}

		if( options.size() <= 1 || CredentialsConfig.AuthType.NO_AUTHORIZATION.toString().equals( selectedAuthProfile ) )
		{
			addEditOptions.remove( AddEditOptions.DELETE.getDescription() );
		}

		options.add( OPTIONS_SEPARATOR );
		options.addAll( addEditOptions );

		return options.toArray( new String[options.size()] );
	}

	private boolean isRestRequest( T request )
	{
		return request instanceof RestRequest;
	}

	private ArrayList<String> getAddEditOptions()
	{
		ArrayList<String> addEditOptions = new ArrayList<String>();
		addEditOptions.add( AddEditOptions.ADD.getDescription() );
		addEditOptions.add( AddEditOptions.RENAME.getDescription() );
		addEditOptions.add( AddEditOptions.DELETE.getDescription() );

		return addEditOptions;
	}

	private AddEditOptions getAddEditOptionForDescription( String description )
	{
		for( AddEditOptions option : AddEditOptions.values() )
		{
			if( option.getDescription().equals( description ) )
			{
				return option;
			}
		}
		return null;
	}

	public enum AddEditOptions
	{
		ADD( "Add New Authorization..." ),
		RENAME( "Rename current..." ),
		DELETE( "Delete current" );
		private String description;

		AddEditOptions( String description )
		{
			this.description = description;
		}

		public String getDescription()
		{
			return description;
		}
	}

	private class ProfileSelectionListener implements ItemListener
	{
		@Override
		public void itemStateChanged( ItemEvent e )
		{
			if( e.getStateChange() == ItemEvent.SELECTED )
			{
				String selectedProfile = ( String )e.getItem();

				setAuthenticationTypeAndShowCard( selectedProfile );
				if( !getAddEditOptions().contains( selectedProfile ) )
				{
					DefaultComboBoxModel profileComboBoXModel = new DefaultComboBoxModel(
							createOptionsForAuthorizationCombo( selectedProfile ) );
					profileComboBoXModel.setSelectedItem( selectedProfile );
					profileSelectionComboBox.setModel( profileComboBoXModel );
				}
			}
		}
	}
}