/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.support.wss.entries;

import java.security.MessageDigest;

import javax.swing.JComponent;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.apache.ws.security.util.Base64;
import org.w3c.dom.Document;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.WSSEntryConfig;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.jgoodies.binding.PresentationModel;

public class AddUsernameEntry extends WssEntryBase
{
	private static final String PASSWORD_DIGEST_EXT = "PasswordDigest Ext";

	private static final String PASSWORD_DIGEST = "PasswordDigest";

	private static final String PASSWORD_TEXT = "PasswordText";

	public static final String TYPE = "Username";

	private boolean addCreated;
	private boolean addNonce;
	private String passwordType;

	public void init( WSSEntryConfig config, OutgoingWss container )
	{
		super.init( config, container, TYPE );
	}

	public void process( WSSecHeader secHeader, Document doc, PropertyExpansionContext context )
	{
		WSSecUsernameToken token = new WSSecUsernameToken();
		if( addCreated )
			token.addCreated();

		if( addNonce )
			token.addNonce();

		if( StringUtils.hasContent( passwordType ) )
		{
			if( passwordType.equals( PASSWORD_TEXT ) )
				token.setPasswordType( WSConstants.PASSWORD_TEXT );
			else if( passwordType.equals( PASSWORD_DIGEST ) || passwordType.equals( PASSWORD_DIGEST_EXT ) )
				token.setPasswordType( WSConstants.PASSWORD_DIGEST );
		}

		String password = context.expand( getPassword() );

		if( PASSWORD_DIGEST_EXT.equals( password ) )
		{
			try
			{
				MessageDigest sha = MessageDigest.getInstance( "SHA-1" );
				sha.reset();
				sha.update( password.getBytes( "UTF-8" ) );
				password = Base64.encode( sha.digest() );
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}

		token.setUserInfo( context.expand( getUsername() ), password );

		token.build( doc, secHeader );
	}

	@Override
	protected JComponent buildUI()
	{
		SimpleBindingForm form = new SimpleBindingForm( new PresentationModel<AddUsernameEntry>( this ) );
		form.addSpace( 5 );
		form.appendTextField( "username", "Username", "The username for this token" );
		form.appendPasswordField( "password", "Password", "The password for this token" );

		form.appendCheckBox( "addNonce", "Add Nonce", "Adds a nonce" );
		form.appendCheckBox( "addCreated", "Add Created", "Adds a created" );

		form.appendComboBox( "passwordType", "Password Type", new String[] { PASSWORD_TEXT, PASSWORD_DIGEST,
				PASSWORD_DIGEST_EXT }, "The password type to generate" );

		return form.getPanel();
	}

	@Override
	protected void load( XmlObjectConfigurationReader reader )
	{
		addCreated = reader.readBoolean( "addCreated", true );
		addNonce = reader.readBoolean( "addNonce", true );
		passwordType = reader.readString( "passwordType", WSConstants.PASSWORD_DIGEST );
	}

	@Override
	protected void save( XmlObjectConfigurationBuilder builder )
	{
		builder.add( "addCreated", addCreated );
		builder.add( "addNonce", addNonce );
		builder.add( "passwordType", passwordType );
	}

	public boolean isAddCreated()
	{
		return addCreated;
	}

	public void setAddCreated( boolean addCreated )
	{
		this.addCreated = addCreated;
		saveConfig();
	}

	public boolean isAddNonce()
	{
		return addNonce;
	}

	public void setAddNonce( boolean addNonce )
	{
		this.addNonce = addNonce;
		saveConfig();
	}

	public String getPasswordType()
	{
		return passwordType;
	}

	public void setPasswordType( String passwordType )
	{
		this.passwordType = passwordType;
		saveConfig();
	}
}
