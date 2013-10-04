/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.testondemand;

import java.io.UnsupportedEncodingException;

import com.eviware.soapui.SoapUI;
import com.google.common.base.Charsets;

import flex.messaging.util.URLDecoder;

/**
 * @author Erik R. Yverling
 * 
 *         An AlertSite location for running the Test On Demand.
 */
public class Location
{
	private String code;
	private String name;
	private String[] serverIPAddresses;

	public Location( String code, String name, String[] serverIPAddresses )
	{
		this.code = code;
		this.name = name;
		this.serverIPAddresses = serverIPAddresses;
	}

	public String getCode()
	{
		return code;
	}

	public String getName()
	{
		return getURLDecodedName();
	}

	public String[] getServerIPAddresses()
	{
		return serverIPAddresses;
	}

	private String getURLDecodedName()
	{
		// We'll return the encoded name if the decoding fails
		String decodedName = name;
		try
		{
			decodedName = URLDecoder.decode( name, Charsets.UTF_8.toString() );
		}
		catch( UnsupportedEncodingException e )
		{
			SoapUI.logError( e );
		}
		return decodedName;
	}

	@Override
	public String toString()
	{
		return getURLDecodedName();
	}
}
