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

package com.eviware.soapui.impl.wsdl.support.wsrm;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import net.java.dev.wadl.x2009.x02.ApplicationDocument;

public class WsrmBuilder
{
	private static final String WSRM_CREATE_SEQUENCE = "CreateSequence";
	private static final String WSRM_EXPIRES = "Expires";
	private static final String WSRM_ACKNOWLEDGMENTS_TO = "AcksTo";

	private static final String WSRM_CLOSE_SEQUENCE = "CloseSequence";
	private static final String WSRM_IDENTIFIER = "Identifier";
	private static final String WSRM_LAST_MESSAGE = "LastMsgNumber";

	private WsrmConfig wsrmConfig;

	public WsrmBuilder( WsrmConfig wsrmConfig )
	{
		this.wsrmConfig = wsrmConfig;
	}

	public XmlObject constructSequenceRequest()
	{
		XmlObject object = XmlObject.Factory.newInstance();
		XmlCursor cursor = object.newCursor();
		// cursor.toNextToken();

		cursor.insertNamespace( "wsrm", wsrmConfig.getVersionNameSpace() );

		cursor.beginElement( WSRM_CREATE_SEQUENCE, wsrmConfig.getVersionNameSpace() );
		cursor.insertElementWithText( WSRM_ACKNOWLEDGMENTS_TO, wsrmConfig.getAckTo() );
		if( wsrmConfig.getSequenceExpires() != null )
			cursor.insertElementWithText( WSRM_EXPIRES, wsrmConfig.getSequenceExpires().toString() );

		cursor.dispose();

		return object;
	}

	public XmlObject constructSequenceClose()
	{
		XmlObject object = XmlObject.Factory.newInstance();
		XmlCursor cursor = object.newCursor();
		cursor.toNextToken();

		cursor.insertNamespace( "wsrm", wsrmConfig.getVersionNameSpace() );

		cursor.beginElement( WSRM_CLOSE_SEQUENCE, wsrmConfig.getVersionNameSpace() );
		cursor.insertElementWithText( WSRM_IDENTIFIER, wsrmConfig.getSequenceIdentifier() );
		// For a request, there will always be one message
		cursor.insertElementWithText( WSRM_LAST_MESSAGE, "1" );

		cursor.dispose();

		return object;
	}
}
