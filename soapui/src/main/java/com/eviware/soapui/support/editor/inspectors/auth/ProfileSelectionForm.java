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
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.DefaultActionList;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.editor.EditorView;
import com.eviware.soapui.support.editor.inspectors.AbstractXmlInspector;
import com.eviware.soapui.support.editor.views.xml.raw.RawXmlEditorFactory;
import com.eviware.soapui.support.editor.xml.XmlDocument;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JComboBoxFormField;
import com.eviware.x.impl.swing.JTextFieldFormField;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

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
	private OAuth2Form oAuth2Form;

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

	@Override
	public void release()
	{
		super.release();
		oAuth2Form.release();
	}

	protected void buildUI()
	{
		SimpleBindingForm authTypeForm = new SimpleBindingForm( new PresentationModel<AbstractHttpRequest<?>>( request ) );
		createProfileSelectionComboBox( authTypeForm );
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
		comboBoxPanel.add( profileSelectionComboBox, cc.xy( 4, 1 ) );

		JPanel wrapperPanel = new JPanel( new BorderLayout() );
		wrapperPanel.add( comboBoxPanel, BorderLayout.LINE_START );
		wrapperPanel.add( UISupport.createFormButton( new ShowOnlineHelpAction( "http://www.soapui.org" ) ),
				BorderLayout.AFTER_LINE_ENDS );
		return wrapperPanel;
	}

	private void createProfileSelectionComboBox( SimpleBindingForm authTypeForm )
	{
		String[] existingProfiles = createOptionsForAuthorizationCombo();

		profileSelectionComboBox = new JComboBox<String>( existingProfiles );
		profileSelectionComboBox.setName( AUTHORIZATION_TYPE_COMBO_BOX_NAME );
		profileSelectionComboBox.addActionListener( new ProfileSelectionListener() );
	}

	private void setAuthenticationTypeAndShowCard( String selectedOption )
	{
		if( getAddEditOptions().contains( selectedOption ) )
		{
			performAddEditOperation( request.getSelectedAuthProfile(), selectedOption );
		}
		request.setSelectedAuthProfile( selectedOption );
		if( createOAuth2ProfileNameList().contains( selectedOption ) )
		{
			request.setAuthType( CredentialsConfig.AuthType.O_AUTH_2.toString() );
			oAuth2Form = new OAuth2Form( getProfileForName( selectedOption ), this );
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

	private void performAddEditOperation( String currentProfile, String selectedOption )
	{
		AddEditOptions addEditOption = getAddEditOptionForDescription( selectedOption );
		switch( addEditOption )
		{
			case ADD:
				showSelectAuthorizationTypeForm();
				break;
			case DELETE:
				deleteCurrentProfile( currentProfile );
				break;
			case RENAME:
				renameCurrentProfile( currentProfile );
				break;
			default:
				break;
		}
	}

	private void renameCurrentProfile( String profileName )
	{
	}

	private void deleteCurrentProfile( String profileName )
	{
		if( createOAuth2ProfileNameList().contains( profileName ) )
		{
			getOAuth2ProfileContainer().removeProfile( profileName );
		}
		else if( getBasicAuthenticationTypes().contains( profileName ) )
		{
			request.removeBasicAuthenticationProfile( profileName );
		}

		refreshProfileSelectionComboBox( NO_AUTHORIZATION );
	}

	private void showSelectAuthorizationTypeForm()
	{
		DefaultActionList actions = new DefaultActionList( "Actions" );
		final XFormDialog dialog = ADialogBuilder.buildDialog( AuthorizationTypeForm.class );

		ArrayList<String> options = new ArrayList<String>();
		options.add( CredentialsConfig.AuthType.GLOBAL_HTTP_SETTINGS.toString() );
		options.add( CredentialsConfig.AuthType.PREEMPTIVE.toString() );
		options.add( CredentialsConfig.AuthType.NTLM.toString() );
		options.add( CredentialsConfig.AuthType.SPNEGO_KERBEROS.toString() );

		if( request instanceof RestRequest )
		{
			options.add( CredentialsConfig.AuthType.O_AUTH_2.toString() );
		}

		options.removeAll( request.getBasicAuthenticationProfiles() );

		final JTextFieldFormField profileNameField = ( JTextFieldFormField )dialog.getFormField( AuthorizationTypeForm.OAUTH2_PROFILE_NAME_FIELD );
		profileNameField.getComponent().setVisible( false );
		profileNameField.addFormFieldListener( new XFormFieldListener()
		{
			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				if( StringUtils.isNullOrEmpty( newValue ) )
				{
					//TODO: disable Ok button
				}
			}
		} );

		JComboBoxFormField accessTokenPositionField = ( JComboBoxFormField )dialog.getFormField( AuthorizationTypeForm.AUTHORIZATION_TYPE );
		accessTokenPositionField.setOptions( options.toArray( new String[options.size()] ) );
		accessTokenPositionField.addFormFieldListener( new XFormFieldListener()
		{
			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				if( newValue.equals( CredentialsConfig.AuthType.O_AUTH_2.toString() ) )
				{
					profileNameField.getComponent().setVisible( true );
				}
				else
				{
					profileNameField.getComponent().setVisible( false );
				}
			}
		} );

		dialog.setValue( AuthorizationTypeForm.AUTHORIZATION_TYPE, request.getAuthType() );
		if( dialog.show() )
		{
			final String authType = dialog.getValue( AuthorizationTypeForm.AUTHORIZATION_TYPE );
			if( getBasicAuthenticationTypes().contains( authType ) )
			{
				request.addBasicAuthenticationProfile( authType );
				SwingUtilities.invokeLater( new Runnable()
				{
					@Override
					public void run()
					{
						refreshProfileSelectionComboBox( authType );
					}
				} );
			}
			else if( CredentialsConfig.AuthType.O_AUTH_2.toString().equals( authType ) )
			{
				String profileName = dialog.getValue( AuthorizationTypeForm.OAUTH2_PROFILE_NAME_FIELD );
				//TODO: check for unique profileName
				final OAuth2Profile profile = getOAuth2ProfileContainer().addNewOAuth2Profile( profileName );
				SwingUtilities.invokeLater( new Runnable()
				{
					@Override
					public void run()
					{
						refreshProfileSelectionComboBox( profile.getName() );
					}
				} );
			}


		}
	}

	private void refreshProfileSelectionComboBox( String selectedProfile )
	{
		profileSelectionComboBox.setModel( new DefaultComboBoxModel( createOptionsForAuthorizationCombo() ) );
		profileSelectionComboBox.removeActionListener( profileSelectionComboBox.getActionListeners()[0] );
		profileSelectionComboBox.addActionListener( new ProfileSelectionListener() );
		profileSelectionComboBox.setSelectedItem( selectedProfile );
	}

	private void showCard( String cardName )
	{
		CardLayout layout = ( CardLayout )cardPanel.getLayout();
		layout.show( cardPanel, cardName );
	}

	private ArrayList<String> createOAuth2ProfileNameList()
	{
		ArrayList<String> profileNameList = new ArrayList<String>();
		for( OAuth2Profile profile : getOAuth2ProfileList() )
		{
			profileNameList.add( profile.getName() );
		}
		return profileNameList;
	}

	private OAuth2Profile getProfileForName( String profileName )
	{
		for( OAuth2Profile profile : getOAuth2ProfileList() )
		{
			if( profile.getName().equals( profileName ) )
			{
				return profile;
			}
		}
		return null;
	}

	private List<OAuth2Profile> getOAuth2ProfileList()
	{
		return getOAuth2ProfileContainer().getOAuth2ProfileList();
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

	private String[] createOptionsForAuthorizationCombo()
	{
		ArrayList<String> options = new ArrayList<String>();
		options.add( NO_AUTHORIZATION );
		ArrayList<String> oAuth2Profiles = createOAuth2ProfileNameList();
		options.addAll( oAuth2Profiles );
		options.addAll( request.getBasicAuthenticationProfiles() );
		options.add( "------------------" );
		options.addAll( getAddEditOptions() );
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


	@AForm(name = "AuthorizationTypeForm.Title", description = "AuthorizationTypeForm.Description")
	public interface AuthorizationTypeForm
	{
		public static final MessageSupport messages = MessageSupport.getMessages( ProfileSelectionForm.class );

		@AField(description = "Form.AccessTokenPosition.Description", type = AField.AFieldType.COMBOBOX)
		public final static String AUTHORIZATION_TYPE = messages.get( "Form.AccessTokenPosition.Label" );

		@AField(description = "Form.OAuth2ProfileName.Description", type = AField.AFieldType.STRING)
		public final static String OAUTH2_PROFILE_NAME_FIELD = messages.get( "Form.OAuth2ProfileName.Label" );

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