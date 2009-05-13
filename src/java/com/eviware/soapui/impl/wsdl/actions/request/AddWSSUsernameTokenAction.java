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

package com.eviware.soapui.impl.wsdl.actions.request;

import java.awt.event.ActionEvent;
import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Prompts to add a WSS Username Token to the specified WsdlRequests
 * requestContent
 * 
 * @author Ole.Matzura
 */

public class AddWSSUsernameTokenAction extends AbstractAction
{
	private final WsdlRequest request;

	public AddWSSUsernameTokenAction( WsdlRequest request )
	{
		super( "Add WSS Username Token" );
		this.request = request;
	}

	public void actionPerformed( ActionEvent e )
	{
		if( ( request.getUsername() == null || request.getUsername().length() == 0 )
				&& ( request.getPassword() == null || request.getPassword().length() == 0 ) )
		{
			UISupport.showErrorMessage( "Request is missing username and password" );
			return;
		}

		String req = request.getRequestContent();

		try
		{
			String passwordType = ( String )UISupport.prompt( "Add WSS Username Token", "Specify Password Type",
					new String[] { WsdlRequest.PW_TYPE_TEXT, WsdlRequest.PW_TYPE_DIGEST } );

			if( passwordType == null )
				return;

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware( true );
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse( new InputSource( new StringReader( req ) ) );
			WSSecUsernameToken addUsernameToken = new WSSecUsernameToken();

			if( WsdlRequest.PW_TYPE_DIGEST.equals( passwordType ) )
			{
				addUsernameToken.setPasswordType( WSConstants.PASSWORD_DIGEST );
			}
			else
			{
				addUsernameToken.setPasswordType( WSConstants.PASSWORD_TEXT );
			}

			addUsernameToken.setUserInfo( request.getUsername(), request.getPassword() );
			addUsernameToken.addNonce();
			addUsernameToken.addCreated();

			StringWriter writer = new StringWriter();

			WSSecHeader secHeader = new WSSecHeader();
			secHeader.insertSecurityHeader( doc );
			XmlUtils.serializePretty( addUsernameToken.build( doc, secHeader ), writer );
			request.setRequestContent( writer.toString() );
		}
		catch( Exception e1 )
		{
			UISupport.showErrorMessage( e1 );
		}
	}
}