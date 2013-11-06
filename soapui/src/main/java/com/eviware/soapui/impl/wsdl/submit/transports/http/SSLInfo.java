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

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import java.security.Principal;
import java.security.cert.Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

/**
 * Holder for SSL-Related details for a request/response interchange
 * 
 * @author ole.matzura
 */

public class SSLInfo
{
	private String cipherSuite;
	private Principal localPrincipal;
	private Certificate[] localCertificates;
	private Principal peerPrincipal;
	private Certificate[] peerCertificates;
	private boolean peerUnverified;

	public SSLInfo( SSLSession session )
	{
		cipherSuite = session.getCipherSuite();
		localPrincipal = session.getLocalPrincipal();
		localCertificates = session.getLocalCertificates();
		try
		{
			peerPrincipal = session.getPeerPrincipal();
			peerCertificates = session.getPeerCertificates();
		}
		catch( SSLPeerUnverifiedException e )
		{
			peerUnverified = true;
		}
	}

	public String getCipherSuite()
	{
		return cipherSuite;
	}

	public Certificate[] getLocalCertificates()
	{
		return localCertificates;
	}

	public Principal getLocalPrincipal()
	{
		return localPrincipal;
	}

	public Certificate[] getPeerCertificates()
	{
		return peerCertificates;
	}

	public Principal getPeerPrincipal()
	{
		return peerPrincipal;
	}

	public boolean isPeerUnverified()
	{
		return peerUnverified;
	}
}
