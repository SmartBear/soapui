/*
 * SoapUI, copyright (C) 2004-2013 smartbear.com
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
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.XFormRadioGroup;

import javax.swing.*;

import static com.eviware.soapui.impl.rest.OAuth2Profile.AccessTokenPosition;
import static com.eviware.soapui.impl.rest.OAuth2Profile.RefreshAccessTokenMethods.*;

/**
 *
 */
public class OAuth2AdvancedOptionsDialog
{
	public static final MessageSupport messages = MessageSupport.getMessages( OAuth2AdvancedOptionsDialog.class );
	private ExpirationTimeChooser expirationTimeComponent;
	private JButton refreshAccessTokenButton;


	public OAuth2AdvancedOptionsDialog( OAuth2Profile profile, JButton refreshAccessTokenButton )
	{
		this.refreshAccessTokenButton = refreshAccessTokenButton;
		expirationTimeComponent = new ExpirationTimeChooser( profile );
		XFormDialog dialog = ADialogBuilder.buildDialog( Form.class );

		dialog.getFormField( Form.ACCESS_TOKEN_EXPIRATION_TIME ).setProperty( "component", expirationTimeComponent );


		setAccessTokenOptions( profile, dialog );

		setRefreshAccessTokenOptions( profile, dialog );

		if( dialog.show() )
		{
			String accessTokenPosition = dialog.getValue( Form.ACCESS_TOKEN_POSITION );
			profile.setAccessTokenPosition( AccessTokenPosition.valueOf( accessTokenPosition ) );

			String refreshAccessTokenMethod = dialog.getValue( Form.AUTOMATIC_ACCESS_TOKEN_REFRESH );
			profile.setRefreshAccessTokenMethod( valueOf( refreshAccessTokenMethod ) );

			long manualExpirationTime = expirationTimeComponent.getAccessTokenExpirationTimeInSeconds();
			if( manualExpirationTime != -1 )
			{
				profile.setUseManualAccessTokenExpirationTime( true );
				profile.setManualAccessTokenExpirationTime( manualExpirationTime );
			}

			enableRefreshAccessTokenButton( profile );
		}
	}

	private void enableRefreshAccessTokenButton( OAuth2Profile profile )
	{
		boolean enabled = profile.getRefreshAccessTokenMethod().equals( OAuth2Profile.RefreshAccessTokenMethods.MANUAL )
				&& ( !org.apache.commons.lang.StringUtils.isEmpty( profile.getRefreshToken() ) );
		refreshAccessTokenButton.setEnabled( enabled );
		refreshAccessTokenButton.setVisible( enabled );
	}

	private void setRefreshAccessTokenOptions( OAuth2Profile profile, XFormDialog dialog )
	{
		XFormRadioGroup refreshOptions = ( XFormRadioGroup )dialog.getFormField( Form.AUTOMATIC_ACCESS_TOKEN_REFRESH );
		refreshOptions.setOptions( values() );
		refreshOptions.setValue( profile.getRefreshAccessTokenMethod().toString() );
	}

	private void setAccessTokenOptions( OAuth2Profile target, XFormDialog dialog )
	{
		XFormRadioGroup accessTokenPositionField = ( XFormRadioGroup )dialog.getFormField( Form.ACCESS_TOKEN_POSITION );
		String[] accessTokenPositions = new String[] { AccessTokenPosition.HEADER.toString(),
				AccessTokenPosition.QUERY.toString() };

		accessTokenPositionField.setOptions( accessTokenPositions );

		dialog.setValue( Form.ACCESS_TOKEN_POSITION, target.getAccessTokenPosition().toString() );
	}

	@AForm(name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.OAUTH_ADVANCED_OPTIONS)
	public interface Form
	{
		@AField( description = "Form.AccessTokenPosition.Description", type = AField.AFieldType.RADIOGROUP )
		public final static String ACCESS_TOKEN_POSITION = messages.get( "Form.AccessTokenPosition.Label" );

		@AField( description = "Form.AutomaticRefreshAccessToken.Description", type = AField.AFieldType.RADIOGROUP )
		public final static String AUTOMATIC_ACCESS_TOKEN_REFRESH = messages.get( "Form.AutomaticRefreshAccessToken.Label" );

		@AField( description = "Form.AccessTokenExpirationTime.Description", type = AField.AFieldType.COMPONENT )
		public final static String ACCESS_TOKEN_EXPIRATION_TIME = messages.get( "Form.AccessTokenExpirationTime.Label" );
	}
}
