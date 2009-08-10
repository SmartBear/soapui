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

package com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Vector;

import javax.xml.namespace.QName;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlHexBinary;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.filters.WssRequestFilter;
import com.eviware.soapui.impl.wsdl.submit.transports.http.ExtendedHttpMethod;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.impl.wsdl.support.xsd.SchemaUtils;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.xml.XmlUtils;

public class WsdlMimeMessageResponse extends MimeMessageResponse implements WsdlResponse
{
	private Vector<Object> wssResult;

	public WsdlMimeMessageResponse( WsdlRequest httpRequest, ExtendedHttpMethod httpMethod, String requestContent,
			PropertyExpansionContext context )
	{
		super( httpRequest, httpMethod, requestContent, context );

		WsdlRequest wsdlRequest = ( WsdlRequest )httpRequest;
		processIncomingWss( wsdlRequest, context );

		String multipartType = null;

		Header h = httpMethod.getResponseHeader( "Content-Type" );
		HeaderElement[] elements = h.getElements();

		for( HeaderElement element : elements )
		{
			String name = element.getName().toUpperCase();
			if( name.startsWith( "MULTIPART/" ) )
			{
				NameValuePair parameter = element.getParameterByName( "type" );
				if( parameter != null )
					multipartType = parameter.getValue();
			}
		}

		if( wsdlRequest.isExpandMtomResponseAttachments() && "application/xop+xml".equals( multipartType ) )
		{
			expandMtomAttachments( wsdlRequest );
		}
	}

	private void processIncomingWss( AbstractHttpRequestInterface<?> wsdlRequest, PropertyExpansionContext context )
	{
		IncomingWss incomingWss = ( IncomingWss )context.getProperty( WssRequestFilter.INCOMING_WSS_PROPERTY );
		if( incomingWss != null )
		{
			try
			{
				Document document = XmlUtils.parseXml( getMmSupport().getResponseContent() );
				wssResult = incomingWss.processIncoming( document, context );
				if( wssResult != null && wssResult.size() > 0 )
				{
					StringWriter writer = new StringWriter();
					XmlUtils.serializePretty( document, writer );
					getMmSupport().setResponseContent( writer.toString() );
				}
			}
			catch( Exception e )
			{
				if( wssResult == null )
					wssResult = new Vector<Object>();
				wssResult.add( e );
			}
		}
	}

	private void expandMtomAttachments( WsdlRequest wsdlRequest )
	{
		try
		{
			XmlObject xmlObject = XmlObject.Factory.parse( getContentAsString() );
			XmlObject[] includes = xmlObject
					.selectPath( "declare namespace xop='http://www.w3.org/2004/08/xop/include'; //xop:Include" );

			for( XmlObject include : includes )
			{
				Element elm = ( Element )include.getDomNode();
				String href = elm.getAttribute( "href" );
				Attachment attachment = getMmSupport().getAttachmentWithContentId( "<" + href.substring( 4 ) + ">" );
				if( attachment != null )
				{
					ByteArrayOutputStream data = Tools.readAll( attachment.getInputStream(), 0 );
					byte[] byteArray = data.toByteArray();

					XmlCursor cursor = include.newCursor();
					cursor.toParent();
					XmlObject parentXmlObject = cursor.getObject();
					cursor.dispose();

					SchemaType schemaType = parentXmlObject.schemaType();
					Node parentNode = elm.getParentNode();

					if( schemaType.isNoType() )
					{
						SchemaTypeSystem typeSystem = wsdlRequest.getOperation().getInterface().getWsdlContext()
								.getSchemaTypeSystem();
						SchemaGlobalElement schemaElement = typeSystem.findElement( new QName( parentNode.getNamespaceURI(),
								parentNode.getLocalName() ) );
						if( schemaElement != null )
						{
							schemaType = schemaElement.getType();
						}
					}

					String txt = null;

					if( SchemaUtils.isInstanceOf( schemaType, XmlHexBinary.type ) )
					{
						txt = new String( Hex.encodeHex( byteArray ) );
					}
					else
					{
						txt = new String( Base64.encodeBase64( byteArray ) );
					}

					parentNode.replaceChild( elm.getOwnerDocument().createTextNode( txt ), elm );
				}
			}

			getMmSupport().setResponseContent( xmlObject.toString() );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	@Override
	public WsdlRequest getRequest()
	{
		return ( WsdlRequest )super.getRequest();
	}

	public Vector<?> getWssResult()
	{
		return wssResult;
	}

}
