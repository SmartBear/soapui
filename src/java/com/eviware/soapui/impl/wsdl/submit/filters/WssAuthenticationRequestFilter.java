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

package com.eviware.soapui.impl.wsdl.submit.filters;

import java.io.IOException;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionUtils;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Modifies the request message to include WS-Securty Username and Timestamp
 * tokens
 * 
 * @author Ole.Matzura
 */

public class WssAuthenticationRequestFilter extends AbstractWssRequestFilter
{
	private static final String WSS_USERNAME_TOKEN = "WsSecurityAuthenticationRequestFilter@UsernameToken";
	private static final String WSS_TIMESTAMP_TOKEN = "WsSecurityAuthenticationRequestFilter@TimestampToken";

	public void filterWsdlRequest( SubmitContext context, WsdlRequest wsdlRequest )
	{
		String pwType = PropertyExpansionUtils.expandProperties( context, wsdlRequest.getWssPasswordType() );
		String wsTimestamp = wsdlRequest.getWssTimeToLive();

		if( ( StringUtils.isNullOrEmpty( pwType ) || WsdlRequest.PW_TYPE_NONE.equals( pwType ) )
				&& ( StringUtils.isNullOrEmpty( wsTimestamp ) ) )
			return;
		try
		{
			String password = PropertyExpansionUtils.expandProperties( context, wsdlRequest.getPassword() );
			String username = PropertyExpansionUtils.expandProperties( context, wsdlRequest.getUsername() );

			setWssHeaders( context, username, password, pwType, wsTimestamp );
		}
		catch( Throwable e )
		{
			SoapUI.logError( e );
		}
	}

	public static void setWssHeaders( SubmitContext context, String username, String password, String pwType,
			String wsTimestamp ) throws SAXException, IOException
	{
		Document doc = getWssDocument( context );

		// create username token?
		if( StringUtils.hasContent( pwType ) && !pwType.equals( WsdlRequest.PW_TYPE_NONE )
				&& StringUtils.hasContent( username ) && StringUtils.hasContent( password ) )
		{
			// remove if already set
			Element elm = ( Element )context.getProperty( WSS_USERNAME_TOKEN );
			if( elm != null )
			{
				Element parentNode = ( Element )elm.getParentNode();
				parentNode.removeChild( elm );
			}

			// save it so it can be removed..
			context.setProperty( WSS_USERNAME_TOKEN, setWssUsernameToken( username, password, pwType, doc ) );
		}
		// remove if pwType is not null
		else if( pwType != null && context.getProperty( WSS_USERNAME_TOKEN ) != null )
		{
			Element elm = ( Element )context.getProperty( WSS_USERNAME_TOKEN );
			context.removeProperty( WSS_USERNAME_TOKEN );
			Element parentNode = ( Element )elm.getParentNode();
			parentNode.removeChild( elm );
			if( XmlUtils.getChildElements( parentNode ).getLength() == 0 )
				parentNode.getParentNode().removeChild( parentNode );
		}

		// add timestamp?
		if( StringUtils.hasContent( wsTimestamp ) )
		{
			// remove if already set
			Element elm = ( Element )context.getProperty( WSS_TIMESTAMP_TOKEN );
			if( elm != null )
			{
				Element parentNode = ( Element )elm.getParentNode();
				parentNode.removeChild( elm );
			}

			// save it so it can be removed..
			context.setProperty( WSS_TIMESTAMP_TOKEN, setWsTimestampToken( wsTimestamp, doc ) );
		}
		// remove
		else if( wsTimestamp != null && context.getProperty( WSS_TIMESTAMP_TOKEN ) != null )
		{
			Element elm = ( Element )context.getProperty( WSS_TIMESTAMP_TOKEN );
			context.removeProperty( WSS_TIMESTAMP_TOKEN );
			Element parentNode = ( Element )elm.getParentNode();
			parentNode.removeChild( elm );
			if( XmlUtils.getChildElements( parentNode ).getLength() == 0 )
				parentNode.getParentNode().removeChild( parentNode );
		}

		updateWssDocument( context, doc );
	}

	private static Element setWsTimestampToken( String ttl, Document doc )
	{
		WSSecTimestamp addTimestamp = new WSSecTimestamp();
		addTimestamp.setTimeToLive( Integer.parseInt( ttl ) );

		WSSConfig wsc = WSSConfig.getNewInstance();
		wsc.setPrecisionInMilliSeconds( false );
		wsc.setTimeStampStrict( false );
		addTimestamp.setWsConfig( wsc );

		WSSecHeader secHeader = new WSSecHeader();
		secHeader.insertSecurityHeader( doc );
		addTimestamp.build( doc, secHeader );
		return addTimestamp.getElement();
	}

	private static Element setWssUsernameToken( String username, String password, String pwType, Document doc )
	{
		WSSecUsernameToken wsa = new WSSecUsernameToken();
		if( WsdlRequest.PW_TYPE_DIGEST.equals( pwType ) )
		{
			wsa.setPasswordType( WSConstants.PASSWORD_DIGEST );
		}
		else
		{
			wsa.setPasswordType( WSConstants.PASSWORD_TEXT );
		}

		wsa.setUserInfo( username, password );
		wsa.addNonce();
		wsa.addCreated();

		WSSecHeader secHeader = new WSSecHeader();
		secHeader.insertSecurityHeader( doc );
		wsa.build( doc, secHeader );
		return wsa.getUsernameTokenElement();
	}
}
