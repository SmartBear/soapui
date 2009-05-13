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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Properties;

import org.apache.commons.ssl.KeyStoreBuilder;
import org.apache.commons.ssl.Util;
import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.Merlin;

import com.eviware.soapui.config.KeyMaterialCryptoConfig;
import com.eviware.soapui.config.WSSCryptoConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.support.wss.DefaultWssContainer;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainer;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.impl.wsdl.teststeps.BeanPathPropertySupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;

public class KeyMaterialWssCrypto implements WssCrypto
{
	private KeyMaterialCryptoConfig config;
	private final WssContainer container;
	private KeyStore keyStore;
	private BeanPathPropertySupport sourceProperty;

	public KeyMaterialWssCrypto( KeyMaterialCryptoConfig config2, WssContainer container, String source, String password )
	{
		this( config2, container );
		setSource( source );
		setPassword( password );
	}

	public KeyMaterialWssCrypto( KeyMaterialCryptoConfig cryptoConfig, WssContainer container2 )
	{
		config = cryptoConfig;
		container = container2;

		sourceProperty = new BeanPathPropertySupport( ( AbstractWsdlModelItem<?> )container.getModelItem(), config,
				"source" )
		{
			@Override
			protected void notifyUpdate( String value, String old )
			{
				getWssContainer().fireCryptoUpdated( KeyMaterialWssCrypto.this );
			}
		};
	}

	public Crypto getCrypto()
	{
		try
		{
			Properties properties = new Properties();
			properties.put( "org.apache.ws.security.crypto.merlin.file", sourceProperty.expand() );
			properties.put( "org.apache.ws.security.crypto.merlin.keystore.provider", "this" );
			if( StringUtils.hasContent( getDefaultAlias() ) )
				properties.put( "org.apache.ws.security.crypto.merlin.keystore.alias", getDefaultAlias() );
			if( StringUtils.hasContent( getAliasPassword() ) )
				properties.put( "org.apache.ws.security.crypto.merlin.alias.password", getAliasPassword() );

			return new KeyMaterialCrypto( properties );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		return null;
	}

	public String getLabel()
	{
		String source = getSource();

		int ix = source.lastIndexOf( File.separatorChar );
		if( ix == -1 )
			ix = source.lastIndexOf( '/' );

		if( ix != -1 )
			source = source.substring( ix + 1 );

		return source;
	}

	public String getSource()
	{
		return sourceProperty.expand();
	}

	public void udpateConfig( KeyMaterialCryptoConfig config )
	{
		this.config = config;
		sourceProperty.setConfig( config );
	}

	public void setSource( String source )
	{
		sourceProperty.set( source, true );
		keyStore = null;
	}

	public KeyStore load() throws Exception
	{
		if( keyStore != null )
			return keyStore;

		try
		{
			UISupport.setHourglassCursor();

			if( StringUtils.hasContent( getDefaultAlias() ) && StringUtils.hasContent( getAliasPassword() ) )
			{
				keyStore = KeyStoreBuilder.build( Util.streamToBytes( new FileInputStream( sourceProperty.expand() ) ),
						getDefaultAlias().getBytes(), getPassword().toCharArray(), getAliasPassword().toCharArray() );
			}
			else
				keyStore = KeyStoreBuilder.build( Util.streamToBytes( new FileInputStream( sourceProperty.expand() ) ),
						getPassword().toCharArray() );

			return keyStore;
		}
		catch( Throwable t )
		{
			throw new Exception( t );
		}
		finally
		{
			UISupport.resetCursor();
		}
	}

	public String getStatus()
	{
		try
		{
			if( StringUtils.hasContent( getSource() ) && StringUtils.hasContent( getPassword() ) )
			{
				load();
				return "OK";
			}
			else
			{
				return "<unavailable>";
			}
		}
		catch( Exception e )
		{
			return "<error: " + e.getMessage() + ">";
		}
	}

	public String getPassword()
	{
		return config.getPassword();
	}

	public String getAliasPassword()
	{
		return config.getAliasPassword();
	}

	public String getDefaultAlias()
	{
		return config.getDefaultAlias();
	}

	public void setAliasPassword( String arg0 )
	{
		config.setAliasPassword( arg0 );
	}

	public void setDefaultAlias( String arg0 )
	{
		config.setDefaultAlias( arg0 );
	}

	public void setPassword( String arg0 )
	{
		config.setPassword( arg0 );
		keyStore = null;
		getWssContainer().fireCryptoUpdated( this );
	}

	public void udpateConfig( WSSCryptoConfig config )
	{
		// this.config = config;
	}

	public String toString()
	{
		return getLabel();
	}

	public DefaultWssContainer getWssContainer()
	{
		return ( DefaultWssContainer )container;
	}

	private class KeyMaterialCrypto extends Merlin
	{
		private KeyMaterialCrypto( Properties properties ) throws CredentialException, IOException
		{
			super( properties );
		}

		@Override
		public KeyStore load( InputStream input, String storepass, String provider, String type )
				throws CredentialException
		{
			if( "this".equals( provider ) )
			{
				try
				{
					return KeyMaterialWssCrypto.this.load();
				}
				catch( Exception e )
				{
					throw new CredentialException( 0, null, e );
				}
			}
			else
				return super.load( input, storepass, provider, type );
		}

		@Override
		protected String getCryptoProvider()
		{
			return config.getCryptoProvider();
		}
	}

	public String getCryptoProvider()
	{
		return config.getCryptoProvider();
	}

	public void setCryptoProvider( String provider )
	{
		config.setCryptoProvider( provider );
		keyStore = null;
		getWssContainer().fireCryptoUpdated( this );
	}

	public void resolve( ResolveContext context )
	{
		sourceProperty.resolveFile( context, "Missing keystore/certificate file" );
	}
}
