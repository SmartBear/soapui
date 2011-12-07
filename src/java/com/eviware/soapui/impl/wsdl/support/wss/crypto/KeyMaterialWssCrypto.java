/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
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
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.List;
import java.util.Properties;

import org.apache.ws.security.components.crypto.CredentialException;
import org.apache.ws.security.components.crypto.Merlin;
import org.apache.ws.security.util.Loader;

import com.eviware.soapui.config.KeyMaterialCryptoConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.support.ExternalDependency;
import com.eviware.soapui.impl.wsdl.support.PathPropertyExternalDependency;
import com.eviware.soapui.impl.wsdl.support.wss.DefaultWssContainer;
import com.eviware.soapui.impl.wsdl.support.wss.WssContainer;
import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
import com.eviware.soapui.impl.wsdl.teststeps.BeanPathPropertySupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;

import edu.umd.cs.findbugs.annotations.NonNull;

public class KeyMaterialWssCrypto implements WssCrypto
{
	private KeyMaterialCryptoConfig config;
	private final WssContainer container;
	private KeyStore keyStore;
	private BeanPathPropertySupport sourceProperty;

	public KeyMaterialWssCrypto( KeyMaterialCryptoConfig config2, WssContainer container, String source,
			String password, CryptoType type )
	{
		this( config2, container );
		setSource( source );
		setPassword( password );
		this.setType( type );
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

	public Merlin getCrypto()
	{
		try
		{
			Properties properties = new Properties();

			properties.put( "org.apache.ws.security.crypto.merlin.keystore.provider", "this" );

			if( getType() == CryptoType.TRUSTSTORE )
			{
				properties.put( "org.apache.ws.security.crypto.merlin.truststore.file", sourceProperty.expand() );
			}
			else
			{
				properties.put( "org.apache.ws.security.crypto.merlin.keystore.file", sourceProperty.expand() );
				if( StringUtils.hasContent( getDefaultAlias() ) )
					properties.put( "org.apache.ws.security.crypto.merlin.keystore.alias", getDefaultAlias() );
			}

			KeyMaterialCrypto keyMaterialCrypto = new KeyMaterialCrypto( properties );
			return keyMaterialCrypto;
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

	/*
	 * This loads the keystore / truststore file
	 */
	public KeyStore load() throws Exception
	{
		if( keyStore != null )
			return keyStore;

		try
		{
			UISupport.setHourglassCursor();

			ClassLoader loader = Loader.getClassLoader( KeyMaterialWssCrypto.class );
			InputStream input = Merlin.loadInputStream( loader, sourceProperty.expand() );
			keyStore = KeyStore.getInstance( KeyStore.getDefaultType() );
			keyStore.load( input, getPassword().toCharArray() );
			return keyStore;
		}
		catch( Throwable t )
		{
			keyStore = null;
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
			if( StringUtils.hasContent( getSource() ) )
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
		public String getCryptoProvider()
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

	public CryptoType getType()
	{
		String typeConfig = config.getType();

		// Default to Keystore if type is not saved in configuration
		if( typeConfig == null )
		{
			typeConfig = CryptoType.KEYSTORE.name();
		}
		CryptoType type = CryptoType.valueOf( typeConfig );
		return type;
	}

	public void setType( @NonNull CryptoType type )
	{
		config.setType( type.name() );
	}

	public void resolve( ResolveContext<?> context )
	{
		sourceProperty.resolveFile( context, "Missing keystore/certificate file" );
	}

	public void addExternalDependency( List<ExternalDependency> dependencies )
	{
		dependencies.add( new PathPropertyExternalDependency( sourceProperty ) );
	}
}
