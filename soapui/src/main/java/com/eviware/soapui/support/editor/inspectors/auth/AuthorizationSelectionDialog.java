package com.eviware.soapui.support.editor.inspectors.auth;

import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.impl.rest.OAuth2ProfileContainer;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.components.SimpleForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.impl.swing.JComboBoxFormField;
import com.eviware.x.impl.swing.JLabelFormField;
import com.eviware.x.impl.swing.JTextFieldFormField;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class AuthorizationSelectionDialog<T extends AbstractHttpRequest>
{

	private T request;
	private List<String> basicAuthTypes;
	private JTextFieldFormField profileNameField;
	private JLabelFormField hintTextLabel;

	public AuthorizationSelectionDialog( T request, List<String> basicAuthTypes )
	{
		this.request = request;
		this.basicAuthTypes = basicAuthTypes;
		buildAndShowDialog();
	}

	private void buildAndShowDialog()
	{
		FormLayout layout = new FormLayout( "5px,100px,5px,left:default,5px:grow(1.0)" );
		final XFormDialog dialog = ADialogBuilder.buildDialog( AuthorizationTypeForm.class, null, layout );

		profileNameField = ( JTextFieldFormField )dialog.getFormField( AuthorizationTypeForm.OAUTH2_PROFILE_NAME_FIELD );
		profileNameField.addFormFieldListener( new ProfileNameFieldListener( dialog ) );

		hintTextLabel = ( JLabelFormField )dialog.getFormField( AuthorizationTypeForm.OAUTH2_PROFILE_NAME_HINT_TEXT_LABEL );
		setHintTextColor();

		setProfileNameAndHintTextVisibility( request.getAuthType() );

		List<String> authTypes = getBasicAuthenticationTypes();
		authTypes.removeAll( request.getBasicAuthenticationProfiles() );
		if( request instanceof RestRequest )
		{
			authTypes.add( CredentialsConfig.AuthType.O_AUTH_2_0.toString() );

			int nextProfileIndex = getOAuth2ProfileContainer().getOAuth2ProfileList().size() + 1;
			profileNameField.setValue( "Profile " + nextProfileIndex );
		}

		setAuthTypeComboBoxOptions( dialog, authTypes );

		dialog.setValue( AuthorizationTypeForm.AUTHORIZATION_TYPE, request.getAuthType() );
		if( dialog.show() )
		{
			createProfileForSelectedAuthType( dialog );
		}
	}

	private void createProfileForSelectedAuthType( XFormDialog dialog )
	{
		String authType = dialog.getValue( AuthorizationTypeForm.AUTHORIZATION_TYPE );
		String profileName = authType;
		if( CredentialsConfig.AuthType.O_AUTH_2_0.toString().equals( authType ) )
		{
			profileName = dialog.getValue( AuthorizationTypeForm.OAUTH2_PROFILE_NAME_FIELD );
			if( getBasicAuthenticationTypes().contains( profileName ) )
			{
				UISupport.showErrorMessage( "'" + profileName + "' is a reserved profile name." );
				return;
			}
			if( getOAuth2ProfileContainer().getOAuth2ProfileNameList().contains( profileName ) )
			{
				UISupport.showErrorMessage( "There is already a profile named '" + profileName + "'" );
				return;
			}
			getOAuth2ProfileContainer().addNewOAuth2Profile( profileName );
		}
		CredentialsConfig.AuthType.Enum authTypeEnum = CredentialsConfig.AuthType.O_AUTH_2_0;
		 if(!CredentialsConfig.AuthType.O_AUTH_2_0.equals( authType ))
		 {
			 authTypeEnum = request.getBasicAuthType( authType );
		 }
		request.setSelectedAuthProfileAndAuthType( profileName, authTypeEnum );
	}

	private void setAuthTypeComboBoxOptions( XFormDialog dialog, List<String> options )
	{
		JComboBoxFormField authTypesComboBox = ( JComboBoxFormField )dialog.getFormField( AuthorizationTypeForm.AUTHORIZATION_TYPE );
		authTypesComboBox.setOptions( options.toArray( new String[options.size()] ) );
		authTypesComboBox.addFormFieldListener( new XFormFieldListener()
		{
			@Override
			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				setProfileNameAndHintTextVisibility( newValue );
			}
		} );
	}

	private void setHintTextColor()
	{
		hintTextLabel.getComponent().setForeground( SimpleForm.HINT_TEXT_COLOR );
	}

	private void setProfileNameAndHintTextVisibility( String authorizationType )
	{
		if( authorizationType.equals( CredentialsConfig.AuthType.O_AUTH_2_0.toString() ) )
		{
			( ( JLabel )profileNameField.getComponent().getClientProperty( "labeledBy" ) ).setVisible( true );
			profileNameField.getComponent().setVisible( true );
			hintTextLabel.getComponent().setVisible( true );
		}
		else
		{
			( ( JLabel )profileNameField.getComponent().getClientProperty( "labeledBy" ) ).setVisible( false );
			profileNameField.getComponent().setVisible( false );
			hintTextLabel.getComponent().setVisible( false );
		}
	}

	private OAuth2ProfileContainer getOAuth2ProfileContainer()
	{
		return request.getOperation().getInterface().getProject().getOAuth2ProfileContainer();
	}

	public List<String> getBasicAuthenticationTypes()
	{
		return basicAuthTypes;
	}

	private static class ProfileNameFieldListener implements XFormFieldListener
	{
		private final XFormDialog dialog;

		public ProfileNameFieldListener( XFormDialog dialog )
		{
			this.dialog = dialog;
		}

		@Override
		public void valueChanged( XFormField sourceField, String newValue, String oldValue )
		{
			ActionList actionsList = dialog.getActionsList();
			for( int actionIndex = 0; actionIndex < actionsList.getActionCount(); actionIndex++ )
			{
				Action action = actionsList.getActionAt( actionIndex );
				if( action.getValue( Action.NAME ).equals( "OK" ) )
				{
					if( StringUtils.isNullOrEmpty( newValue ) )
					{
						action.setEnabled( false );
					}
					else
					{
						action.setEnabled( true );
					}
				}
			}
		}
	}

	@AForm(name = "AuthorizationTypeForm.Title", description = "AuthorizationTypeForm.Description", helpUrl = HelpUrls.ADD_AUTHORIZATION)
	public interface AuthorizationTypeForm
	{
		public static final MessageSupport messages = MessageSupport.getMessages( AuthorizationTypeForm.class );

		@AField(description = "AuthorizationTypeForm.AuthorizationType.Description", type = AField.AFieldType.COMBOBOX)
		public final static String AUTHORIZATION_TYPE = messages.get( "AuthorizationTypeForm.AuthorizationType.Label" );

		@AField(description = "AuthorizationTypeForm.OAuth2ProfileName.Description", type = AField.AFieldType.STRING)
		public final static String OAUTH2_PROFILE_NAME_FIELD = messages.get( "AuthorizationTypeForm.OAuth2ProfileName.Label" );

		@AField(description = "AuthorizationTypeForm.OAuth2ProfileNameHintText.Description", type = AField.AFieldType.LABEL)
		public final static String OAUTH2_PROFILE_NAME_HINT_TEXT_LABEL = messages.get( "AuthorizationTypeForm.OAuth2ProfileNameHintText.Label" );

	}
}
