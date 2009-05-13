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

import com.eviware.soapui.config.WSSCryptoConfig;

public class WssCryptoRegistry // extends
											// AbstractRegistry<WssCrypto,WSSCryptoConfig,WssContainer>
{
	private static WssCryptoRegistry instance;

	public WssCryptoRegistry()
	{
		// mapType( MerlinCrypto.TYPE, MerlinCrypto.class );
	}

	public static synchronized WssCryptoRegistry get()
	{
		if( instance == null )
			instance = new WssCryptoRegistry();

		return instance;
	}

	protected WSSCryptoConfig addNewConfig( WssContainer container )
	{
		return null; // ((DefaultWssContainer)container).getConfig().addNewCrypto();
	}
}
