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

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * RequestFilter for removing empty elements/attributes
 * 
 * @author Ole.Matzura
 */

public class RemoveEmptyContentRequestFilter extends AbstractRequestFilter
{
	@SuppressWarnings( "unused" )
	private final static Logger log = Logger.getLogger( PropertyExpansionRequestFilter.class );

	public void filterAbstractHttpRequest( SubmitContext context, AbstractHttpRequest<?> wsdlRequest )
	{
		if( wsdlRequest != null && !wsdlRequest.isRemoveEmptyContent() )
			return;

		String content = ( String )context.getProperty( BaseHttpRequestTransport.REQUEST_CONTENT );
		if( !StringUtils.hasContent( content ) )
			return;

		String soapNamespace = null;
		String newContent = null;

		if( wsdlRequest instanceof WsdlRequest )
			soapNamespace = ( ( WsdlRequest )wsdlRequest ).getOperation().getInterface().getSoapVersion()
					.getEnvelopeNamespace();

		while( !content.equals( newContent ) )
		{
			if( newContent != null )
				content = newContent;

			newContent = removeEmptyContent( content, soapNamespace );
			if( !context.hasProperty( "RemoveEmptyRecursive" ) )
				break;
		}

		if( newContent != null )
			context.setProperty( BaseHttpRequestTransport.REQUEST_CONTENT, newContent );
	}

	public static String removeEmptyContent( String content, String soapNamespace )
	{
		XmlCursor cursor = null;

		try
		{
			XmlObject xmlObject = XmlObject.Factory.parse( content );
			cursor = xmlObject.newCursor();

			cursor.toNextToken();

			// skip root element
			cursor.toNextToken();
			boolean removed = false;

			while( !cursor.isEnddoc() )
			{
				boolean flag = false;
				if( cursor.isContainer()
						&& ( soapNamespace == null || !soapNamespace.equals( cursor.getName().getNamespaceURI() ) ) )
				{
					Element elm = ( Element )cursor.getDomNode();
					NamedNodeMap attributes = elm.getAttributes();
					if( attributes != null && attributes.getLength() > 0 )
					{
						for( int c = 0; c < attributes.getLength(); c++ )
						{
							Node node = attributes.item( c );
							if( node.getNodeValue() == null || node.getNodeValue().trim().length() == 0 )
							{
								cursor.removeAttribute( XmlUtils.getQName( node ) );
								removed = true;
							}
						}
					}

					if( cursor.getTextValue() == null || cursor.getTextValue().trim().length() == 0
							&& XmlUtils.getFirstChildElement( elm ) == null )
					{
						if( cursor.removeXml() )
						{
							removed = true;
							flag = true;
						}
					}
				}

				if( !flag )
					cursor.toNextToken();
			}

			if( removed )
			{
				return xmlObject.xmlText();
			}
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
		finally
		{
			if( cursor != null )
				cursor.dispose();
		}

		return content;
	}
}
