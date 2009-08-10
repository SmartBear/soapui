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

package com.eviware.soapui.impl.wsdl.support.wss;

import java.io.IOException;
import java.util.Vector;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.codec.binary.Base64;
import org.apache.ws.security.WSPasswordCallback;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.IncomingWssConfig;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.resolver.ResolveContext;

public class IncomingWss
{
	private IncomingWssConfig wssConfig;
	private final WssContainer container;

	public IncomingWss( IncomingWssConfig wssConfig, WssContainer container )
	{
		this.wssConfig = wssConfig;
		this.container = container;
	}

	public WssContainer getWssContainer()
	{
		return container;
	}

	public String getDecryptCrypto()
	{
		return wssConfig.getDecryptCrypto();
	}

	public String getDecryptPassword()
	{
		return wssConfig.getDecryptPassword();
	}

	public String getName()
	{
		return wssConfig.getName();
	}

	public String getSignatureCrypto()
	{
		return wssConfig.getSignatureCrypto();
	}

	public void setDecryptCrypto( String arg0 )
	{
		wssConfig.setDecryptCrypto( arg0 );
	}

	public void setDecryptPassword( String arg0 )
	{
		wssConfig.setDecryptPassword( arg0 );
	}

	public void setName( String arg0 )
	{
		wssConfig.setName( arg0 );
	}

	public void setSignatureCrypto( String arg0 )
	{
		wssConfig.setSignatureCrypto( arg0 );
	}

	@SuppressWarnings( "unchecked" )
	public Vector<Object> processIncoming( Document soapDocument, PropertyExpansionContext context )
			throws WSSecurityException
	{
		Element header = WSSecurityUtil.findWsseSecurityHeaderBlock( soapDocument, soapDocument.getDocumentElement(),
				false );
		if( header == null )
			return null;

		try
		{
			WSSecurityEngine wssecurityEngine = WSSecurityEngine.getInstance();
			WssCrypto signatureCrypto = getWssContainer().getCryptoByName( getSignatureCrypto() );
			WssCrypto decryptCrypto = getWssContainer().getCryptoByName( getDecryptCrypto() );
			Crypto sig = signatureCrypto == null ? null : signatureCrypto.getCrypto();
			Crypto dec = decryptCrypto == null ? null : decryptCrypto.getCrypto();

			if( sig == null && dec == null )
				throw new WSSecurityException( "Missing cryptos" );

			if( sig == null )
				sig = dec;
			else if( dec == null )
				dec = sig;

			return wssecurityEngine.processSecurityHeader( soapDocument, ( String )null, new WSSCallbackHandler( dec ),
					sig, dec );
		}
		catch( WSSecurityException e )
		{
			SoapUI.logError( e );
			throw e;
		}
	}

	public class WSSCallbackHandler implements CallbackHandler
	{
		private final Crypto dec;

		public WSSCallbackHandler( Crypto dec )
		{
			this.dec = dec;
		}

		public void handle( Callback[] callbacks ) throws IOException, UnsupportedCallbackException
		{
			for( Callback callback : callbacks )
			{
				if( callback instanceof WSPasswordCallback )
				{
					WSPasswordCallback cb = ( WSPasswordCallback )callback;
					if( StringUtils.hasContent( getDecryptPassword() ) )
						cb.setPassword( getDecryptPassword() );
					else
						cb.setPassword( UISupport.prompt( "Password required for WSS processing", "Specify Password", "" ) );

					if( cb.getUsage() == WSPasswordCallback.ENCRYPTED_KEY_TOKEN )
					{
						byte[] str = Base64.decodeBase64( cb.getIdentifier().getBytes() );
					}
				}
			}
		}
	}

	public void updateConfig( IncomingWssConfig config )
	{
		this.wssConfig = config;
	}

	public void resolve( ResolveContext<?> context )
	{
	}
}
