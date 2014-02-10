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
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.XFormRadioGroup;

import static com.eviware.soapui.impl.rest.OAuth2Profile.AccessTokenPosition;
import static com.eviware.soapui.impl.rest.OAuth2Profile.AccessTokenRetrievalLocation.BODY_JSON;
import static com.eviware.soapui.impl.rest.OAuth2Profile.AccessTokenRetrievalLocation.BODY_URL_ENCODED_FORM;

/**
 *
 */
public class OAuth2AdvanceOptionsDialog
{
	public static final MessageSupport messages = MessageSupport.getMessages( OAuth2AdvanceOptionsDialog.class );

	public OAuth2AdvanceOptionsDialog( OAuth2Profile target )
	{
		XFormDialog dialog = ADialogBuilder.buildDialog( Form.class );

		setAccessTokenOptions( target, dialog );

		setAccessTokenRetrievalOptions( target, dialog );

		if( dialog.show() )
		{
			String accessTokenPosition = dialog.getValue( Form.ACCESS_TOKEN_POSITION );
			target.setAccessTokenPosition( AccessTokenPosition.valueOf( accessTokenPosition ) );

			String retrievalLocation = dialog.getValue( Form.HOW_TO_RECEIVE_ACCESS_TOKEN );
			target.setAccessTokenRetrievalLocation( OAuth2Profile.AccessTokenRetrievalLocation.valueOf( retrievalLocation ) );

		}
	}

	private void setAccessTokenRetrievalOptions( OAuth2Profile target, XFormDialog dialog )
	{
		XFormRadioGroup accessTokenRetrievalOptions = ( XFormRadioGroup )dialog.getFormField( Form.HOW_TO_RECEIVE_ACCESS_TOKEN );
		String[] options = new String[] { BODY_JSON.toString(), BODY_URL_ENCODED_FORM.toString() };
		accessTokenRetrievalOptions.setOptions( options );
		dialog.setValue( Form.HOW_TO_RECEIVE_ACCESS_TOKEN, target.getAccessTokenRetrievalLocation().toString() );
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
		@AField(description = "Form.AccessTokenPosition.Description", type = AField.AFieldType.RADIOGROUP)
		public final static String ACCESS_TOKEN_POSITION = messages.get( "Form.AccessTokenPosition.Label" );

		@AField(description = "Form.HowToReceiveAccessToken.Description", type = AField.AFieldType.RADIOGROUP)
		public final static String HOW_TO_RECEIVE_ACCESS_TOKEN = messages.get( "Form.HowToReceiveAccessToken.Label" );
	}
}
