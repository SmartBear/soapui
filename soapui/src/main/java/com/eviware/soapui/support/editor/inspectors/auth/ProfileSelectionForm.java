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
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.actions.ShowOnlineHelpAction;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 *
 */
public class ProfileSelectionForm<T extends AbstractHttpRequest> extends AbstractXmlInspector
{

	public static final String AUTHORIZATION_TYPE_COMBO_BOX_NAME = "Authorization:";

	public static final String BASIC_FORM_LABEL = "Legacy form";

	public static final String NO_AUTHORIZATION = "No Authorization";
	private static final String OAUTH_2_FORM_LABEL = "OAuth 2 form";
	public static final String EMPTY_PANEL = "EmptyPanel";

	private T request;
	private final JPanel outerPanel = new JPanel( new BorderLayout() );
	private final JPanel cardPanel = new JPanel( new CardLayout() );
	private JComboBox profileSelectionComboBox;
	private CellConstraints cc = new CellConstraints();

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
		String selectedAuthProfile = request.getSelectedAuthProfile() == null ? NO_AUTHORIZATION :
				request.getSelectedAuthProfile();
		profileSelectionComboBox.setSelectedItem( selectedAuthProfile );
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

		JPanel comboBoxPanel = createAuthorizationLabelAndComboBox();

		innerPanel.add( comboBoxPanel, BorderLayout.PAGE_START );

		cardPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		cardPanel.add( new JPanel(), EMPTY_PANEL );
		innerPanel.add( cardPanel, BorderLayout.CENTER );

		BasicAuthenticationForm<T> authenticationForm = new BasicAuthenticationForm<T>( request );
		cardPanel.add( authenticationForm.getComponent(), BASIC_FORM_LABEL );

		outerPanel.add( new JScrollPane( innerPanel ), BorderLayout.CENTER );
	}

	private JPanel createAuthorizationLabelAndComboBox()
	{
		FormLayout formLayout = new FormLayout( "5px:none,left:pref,10px,left:default,5px:grow(1.0)" );
		JPanel comboBoxPanel = new JPanel( formLayout );
		comboBoxPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 0, 10 ) );

		JLabel authorizationLabel = new JLabel( AUTHORIZATION_TYPE_COMBO_BOX_NAME );
		authorizationLabel.setBorder( BorderFactory.createEmptyBorder( 3, 0, 0, 0 ) );

		formLayout.appendRow( new RowSpec( "top:pref" ) );
		comboBoxPanel.add( authorizationLabel, cc.xy( 2, 1 ) );

		createProfileSelectionComboBox(  );
		comboBoxPanel.add( profileSelectionComboBox, cc.xy( 4, 1 ) );

		JPanel wrapperPanel = new JPanel( new BorderLayout() );
		wrapperPanel.add( comboBoxPanel, BorderLayout.LINE_START );
		wrapperPanel.add( UISupport.createFormButton( new ShowOnlineHelpAction( "http://www.soapui.org" ) ),
				BorderLayout.AFTER_LINE_ENDS );
		return wrapperPanel;
	}

	private void createProfileSelectionComboBox(  )
	{
		String[] existingProfiles = createOptionsForAuthorizationCombo( request.getSelectedAuthProfile() );

		profileSelectionComboBox = new JComboBox<String>( existingProfiles );
		profileSelectionComboBox.setName( AUTHORIZATION_TYPE_COMBO_BOX_NAME );
		profileSelectionComboBox.addActionListener( new ProfileSelectionListener() );
	}

	private void setAuthenticationTypeAndShowCard( String selectedOption )
	{
		if( getAddEditOptions().contains( selectedOption ) )
		{
			performAddEditOperation( request.getSelectedAuthProfile(), selectedOption );
			return;
		}
		request.setSelectedAuthProfile( selectedOption );
		if( getOAuth2ProfileContainer().getOAuth2ProfileNameList().contains( selectedOption ) )
		{
			request.setAuthType( CredentialsConfig.AuthType.O_AUTH_2.toString() );
			OAuth2Form oAuth2Form = new OAuth2Form( getOAuth2ProfileContainer().getProfileByName( selectedOption ) );
			cardPanel.add( oAuth2Form.getComponent(), OAUTH_2_FORM_LABEL );
			showCard( OAUTH_2_FORM_LABEL );
		}
		else if( getBasicAuthenticationTypes().contains( selectedOption ) )
		{
			request.setAuthType( selectedOption );
			showCard( BASIC_FORM_LABEL );
		}
		else
		{
			request.setAuthType( null );
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
						new AuthorizationSelectionDialog<T>( request );
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
		String newName = UISupport.prompt( "Specify name of Profile", "Rename Profile", profileOldName );
		if( newName == null || profileOldName.equals( newName ))
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

		OAuth2Profile profile = getOAuth2ProfileContainer().getProfileByName( profileOldName );
		profile.setName( newName );
		request.setSelectedAuthProfile( newName );
		refreshProfileSelectionComboBox( newName );
	}

	private void deleteCurrentProfile( String profileName )
	{
		if( getOAuth2ProfileContainer().getOAuth2ProfileNameList().contains( profileName ) )
		{
			getOAuth2ProfileContainer().removeProfile( profileName );
		}
		else if( getBasicAuthenticationTypes().contains( profileName ) )
		{
			request.removeBasicAuthenticationProfile( profileName );
		}

		refreshProfileSelectionComboBox( NO_AUTHORIZATION );
	}

	private void refreshProfileSelectionComboBox( String selectedProfile )
	{
		profileSelectionComboBox.setModel( new DefaultComboBoxModel( createOptionsForAuthorizationCombo( selectedProfile ) ) );
		profileSelectionComboBox.removeActionListener( profileSelectionComboBox.getActionListeners()[0] );
		profileSelectionComboBox.addActionListener( new ProfileSelectionListener() );
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
		options.add( CredentialsConfig.AuthType.GLOBAL_HTTP_SETTINGS.toString() );
		options.add( CredentialsConfig.AuthType.PREEMPTIVE.toString() );
		options.add( CredentialsConfig.AuthType.NTLM.toString() );
		options.add( CredentialsConfig.AuthType.SPNEGO_KERBEROS.toString() );
		return options;
	}

	private String[] createOptionsForAuthorizationCombo( String selectedAuthProfile )
	{
		ArrayList<String> options = new ArrayList<String>();
		options.add( NO_AUTHORIZATION );
		ArrayList<String> oAuth2Profiles = getOAuth2ProfileContainer().getOAuth2ProfileNameList();
		options.addAll( oAuth2Profiles );
		options.addAll( request.getBasicAuthenticationProfiles() );

		ArrayList<String> addEditOptions = getAddEditOptions();
		if( !oAuth2Profiles.contains( selectedAuthProfile ) )
		{
			addEditOptions.remove( AddEditOptions.RENAME.getDescription() );
		}

		if( options.size() <= 1 )
		{
			addEditOptions.remove( AddEditOptions.DELETE.getDescription() );
		}

		options.add( "------------------" );
		options.addAll( addEditOptions );

		return options.toArray( new String[oAuth2Profiles.size()] );
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

	private enum AddEditOptions
	{
		ADD( "Add New Authorization..." ),
		RENAME( "Rename current..." ),
		DELETE( "Delete current" );
		private String description;

		AddEditOptions( String description )
		{
			this.description = description;
		}

		private String getDescription()
		{
			return description;
		}
	}


	private class ProfileSelectionListener implements ActionListener
	{
		@Override
		public void actionPerformed( ActionEvent e )
		{
			Object selectedItem = profileSelectionComboBox.getSelectedItem();
			if( selectedItem != null )
			{
				setAuthenticationTypeAndShowCard( selectedItem.toString() );
			}
		}
	}
}