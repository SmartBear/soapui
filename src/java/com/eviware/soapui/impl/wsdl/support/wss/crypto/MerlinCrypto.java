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

package com.eviware.soapui.impl.wsdl.support.wss.crypto;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;

import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;

import com.eviware.soapui.config.WSSCryptoConfig;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainer;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionsResult;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.components.SimpleBindingForm;
import com.eviware.soapui.support.xml.XmlObjectConfigurationBuilder;
import com.eviware.soapui.support.xml.XmlObjectConfigurationReader;
import com.jgoodies.binding.PresentationModel;

public abstract class MerlinCrypto extends WssCryptoBase
{
	public static final String TYPE = "Merlin";
	private String keystore;
	private String password;
	private String type;
	private JButton validateButton;

	@Override
	protected JComponent buildUI()
	{
		SimpleBindingForm form = new SimpleBindingForm( new PresentationModel<MerlinCrypto>( this ) );

		form.appendTextField( "keystore", "Keystore", "The keystore file" );
		form.appendTextField( "password", "Password", "The keystore password" );
		form.appendComboBox( "type", "Type", new String[] { "JKS", "PKCS12" }, "The keystore type" );

		form.addRightComponent( validateButton = new JButton( "Validate" ) );

		validateButton.addActionListener( new ActionListener()
		{

			public void actionPerformed( ActionEvent e )
			{
				if( StringUtils.isNullOrEmpty( getPassword() ) )
				{
					UISupport.showErrorMessage( "Missing password" );
					return;
				}

				try
				{
					Crypto crypto = getCrypto();
					UISupport.showInfoMessage( "Loaded keystore of type: " + crypto.getKeyStore().getType() );
				}
				catch( Throwable t )
				{
					UISupport.showErrorMessage( t );
				}

			}
		} );

		return form.getPanel();
	}

	public String getSource()
	{
		return getKeystore();
	}

	@Override
	protected void load( XmlObjectConfigurationReader reader )
	{
		keystore = reader.readString( "keystore", null );
		password = reader.readString( "password", null );
		type = reader.readString( "type", "jks" );
	}

	@Override
	protected void save( XmlObjectConfigurationBuilder builder )
	{
		builder.add( "keystore", keystore );
		builder.add( "password", password );
		builder.add( "type", type );
	}

	public Crypto getCrypto()
	{
		Properties properties = new Properties();

		properties.put( "org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin" );
		properties.put( "org.apache.ws.security.crypto.merlin.keystore.type", type.toLowerCase() );
		properties.put( "org.apache.ws.security.crypto.merlin.keystore.password", getPassword() );
		properties.put( "org.apache.ws.security.crypto.merlin.file", getKeystore() );

		Crypto crypto = CryptoFactory.getInstance( properties );

		return crypto;
	}

	public void init( WSSCryptoConfig config, WssContainer container )
	{
		super.init( config, container, TYPE );
	}

	public String getKeystore()
	{
		return keystore;
	}

	public void setKeystore( String keystore )
	{
		this.keystore = keystore;
		saveConfig();
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword( String password )
	{
		this.password = password;
		saveConfig();
	}

	public String getType()
	{
		return type;
	}

	public void setType( String type )
	{
		this.type = type;
		saveConfig();
	}

	@Override
	protected void addPropertyExpansions( PropertyExpansionsResult result )
	{
		super.addPropertyExpansions( result );

		result.extractAndAddAll( "keystore" );
	}

	@Override
	public String getLabel()
	{
		if( StringUtils.isNullOrEmpty( keystore ) )
			return super.getLabel();

		int ix = keystore.lastIndexOf( File.separatorChar );
		return ix == -1 ? keystore : keystore.substring( ix + 1 );
	}

	public WssContainer getWssContainer()
	{
		return null;
	}
}
