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

package com.eviware.soapui.impl.wsdl.support.wss.saml.callback;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.security.auth.callback.CallbackHandler;

import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.saml.ext.bean.KeyInfoBean.CERT_IDENTIFIER;

import com.eviware.soapui.impl.wsdl.support.wss.saml.callback.AbstractSAMLCallbackHandler.Statement;

/**
 * @author Erik R. Yverling
 * 
 *         A generic SAML callback handler.
 */
public interface SAMLCallbackHandler extends CallbackHandler
{

	public abstract void setAlias( String alias );

	public abstract String getAlias();

	public abstract void setCrypto( Crypto crypto );

	public abstract Crypto getCrypto();

	public abstract void setCustomAttributeValues( List<?> customAttributeValues );

	public abstract void setResource( String resource );

	public abstract void setSubjectLocality( String ipAddress, String dnsAddress );

	public abstract void setSubjectNameIDFormat( String subjectNameIDFormat );

	public abstract void setIssuer( String issuer );

	public void setSubjectName( String subjectName );

	public void setSubjectQualifier( String subjectQualifier );

	public abstract byte[] getEphemeralKey();

	public abstract void setCerts( X509Certificate[] certs );

	public abstract void setCertIdentifier( CERT_IDENTIFIER certIdentifier );

	public abstract void setStatement( String statement );

	public abstract void setConfirmationMethod( String confMethod );

	public abstract void setCustomAttributeName( String customAttributeName );

}
