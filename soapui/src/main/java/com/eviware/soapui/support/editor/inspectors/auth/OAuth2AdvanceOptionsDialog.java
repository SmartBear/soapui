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
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.StringUtils;
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
public class OAuth2AdvanceOptionsDialog
{
	public static final MessageSupport messages = MessageSupport.getMessages( OAuth2AdvanceOptionsDialog.class );
	private JButton refreshAccessTokenButton;


	public OAuth2AdvanceOptionsDialog( OAuth2Profile profile, JButton refreshAccessTokenButton )
	{
		this.refreshAccessTokenButton = refreshAccessTokenButton;
		XFormDialog dialog = ADialogBuilder.buildDialog( Form.class );

		setAccessTokenOptions( profile, dialog );

		setRefreshAccessTokenOptions( profile, dialog );

		if( dialog.show() )
		{
			String accessTokenPosition = dialog.getValue( Form.ACCESS_TOKEN_POSITION );
			profile.setAccessTokenPosition( AccessTokenPosition.valueOf( accessTokenPosition ) );

			String refreshAccessTokenMethod = dialog.getValue( Form.AUTOMATIC_ACCESS_TOKEN_REFRESH );
			profile.setRefreshAccessTokenMethod( valueOf( refreshAccessTokenMethod ) );

			enableRefreshAccessTokenButton(refreshAccessTokenMethod, profile.getRefreshToken());
		}
	}

	private void enableRefreshAccessTokenButton( String refreshAccessTokenMethod, String refreshToken )
	{
		boolean enabled = refreshAccessTokenMethod.equals( MANUAL.toString() ) && StringUtils.hasContent( refreshToken );
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

	@AForm(name = "Form.Title", description = "Form.Description")
	public interface Form
	{
		@AField( description = "Form.AccessTokenPosition.Description", type = AField.AFieldType.RADIOGROUP )
		public final static String ACCESS_TOKEN_POSITION = messages.get( "Form.AccessTokenPosition.Label" );

		@AField(description = "Form.AutomaticRefreshAccessToken.Description", type = AField.AFieldType.RADIOGROUP)
		public final static String AUTOMATIC_ACCESS_TOKEN_REFRESH = messages.get( "Form.AutomaticRefreshAccessToken.Label" );
	}
}
