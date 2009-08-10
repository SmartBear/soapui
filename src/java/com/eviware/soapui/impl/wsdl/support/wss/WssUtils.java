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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaContainer;
import com.eviware.soapui.support.xml.XmlUtils;

public class WssUtils
{
	public final static String WSSE_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	public final static String WSU_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

	String content;
	SoapVersion soapVersion;

	public static String removeWSSOutgoing( String content, WsaContainer wsaContainer )
	{
		try
		{
			SoapVersion soapVersion = wsaContainer.getOperation().getInterface().getSoapVersion();
			XmlObject xmlContentObject = XmlObject.Factory.parse( content );
			Element header = ( Element )SoapUtils.getHeaderElement( xmlContentObject, soapVersion, true ).getDomNode();

			NodeList headerProps = XmlUtils.getChildElements( header );
			for( int i = 0; i < headerProps.getLength(); i++ )
			{
				Node headerChild = headerProps.item( i );
				if( headerChild.getNamespaceURI().equals( WSSE_NAMESPACE ) )
				{
					header.removeChild( headerChild );
				}
			}
			content = xmlContentObject.xmlText();
		}
		catch( XmlException e )
		{
			SoapUI.logError( e );
		}
		return content;
	}
}
