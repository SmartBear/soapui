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

import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecTimestamp;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Prompts to add a WSS Timestamp Token to the specified WsdlRequests
 * requestContent
 * 
 * @author Ole.Matzura
 */

public class AddWSTimestampAction extends AbstractAction
{
	private final WsdlRequest request;

	public AddWSTimestampAction( WsdlRequest request )
	{
		super( "Add WS-Timestamp" );
		this.request = request;
	}

	public void actionPerformed( ActionEvent e )
	{
		String req = request.getRequestContent();

		try
		{
			String ttlString = UISupport.prompt( "Add WS-Timestamp", "Specify Time-To-Live value", "60" );
			if( ttlString == null )
				return;

			int ttl = 0;
			try
			{
				ttl = Integer.parseInt( ttlString );
			}
			catch( Exception ex )
			{
			}

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware( true );
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse( new InputSource( new StringReader( req ) ) );
			WSSecTimestamp addTimestamp = new WSSecTimestamp();
			addTimestamp.setTimeToLive( ttl );

			StringWriter writer = new StringWriter();
			WSSecHeader secHeader = new WSSecHeader();
			secHeader.insertSecurityHeader( doc );
			XmlUtils.serializePretty( addTimestamp.build( doc, secHeader ), writer );
			request.setRequestContent( writer.toString() );
		}
		catch( Exception e1 )
		{
			UISupport.showErrorMessage( e1 );
		}
	}
}