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

package com.eviware.soapui.impl.wsdl.support.http;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.util.LangUtils;

public class SoapUIHostConfiguration extends HostConfiguration
{

	public static final String SOAPUI_SSL_CONFIG = "soapui.sslConfig";

	public SoapUIHostConfiguration()
	{
		super();
	}

	public SoapUIHostConfiguration( final HostConfiguration hostConfiguration )
	{
		super( hostConfiguration );
	}

	public Object clone()
	{
		return new SoapUIHostConfiguration( this );
	}

	public synchronized boolean equals( final Object o )
	{

		boolean result = super.equals( o );
		if( result && o instanceof SoapUIHostConfiguration )
		{
			SoapUIHostConfiguration that = ( SoapUIHostConfiguration )o;
			return LangUtils.equals( getParams().getParameter( SOAPUI_SSL_CONFIG ), that.getParams().getParameter(
					SOAPUI_SSL_CONFIG ) );
		}
		else
		{
			return false;
		}
	}

	public synchronized int hashCode()
	{
		int hash = super.hashCode();
		hash = LangUtils.hashCode( hash, getParams().getParameter( SOAPUI_SSL_CONFIG ) );
		return hash;
	}
}
