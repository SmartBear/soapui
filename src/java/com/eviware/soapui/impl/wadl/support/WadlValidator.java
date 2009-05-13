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

package com.eviware.soapui.impl.wadl.support;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.rest.RestRepresentation;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.wadl.WadlDefinitionContext;
import com.eviware.soapui.impl.wsdl.submit.RestMessageExchange;
import com.eviware.soapui.model.testsuite.AssertionError;

public class WadlValidator
{
	public WadlValidator( WadlDefinitionContext context )
	{
	}

	public AssertionError[] assertResponse( RestMessageExchange messageExchange )
	{
		RestRequest restRequest = messageExchange.getRestRequest();
		if( restRequest != null )
		{
			if( messageExchange.getResponseStatusCode() >= 400 )
			{
				return assertResponse( messageExchange, RestRepresentation.Type.FAULT );
			}
			else
			{
				return assertResponse( messageExchange, RestRepresentation.Type.RESPONSE );
			}
		}

		return new AssertionError[0];
	}

	private AssertionError[] assertResponse( RestMessageExchange messageExchange, RestRepresentation.Type type )
	{
		List<AssertionError> result = new ArrayList<AssertionError>();
		QName responseBodyElementName = getResponseBodyElementName( messageExchange );
		RestRequest restRequest = messageExchange.getRestRequest();
		boolean asserted = false;

		for( RestRepresentation representation : restRequest.getRepresentations( type, messageExchange
				.getResponseContentType() ) )
		{
			if( representation.getStatus().isEmpty()
					|| representation.getStatus().contains( messageExchange.getResponseStatusCode() ) )
			{
				SchemaType schemaType = representation.getSchemaType();
				if( schemaType != null && representation.getElement().equals( responseBodyElementName ) )
				{
					try
					{
						XmlObject xmlObject = schemaType.getTypeSystem().parse( messageExchange.getResponseContentAsXml(),
								schemaType, new XmlOptions() );

						// create internal error list
						List<?> list = new ArrayList<Object>();

						XmlOptions xmlOptions = new XmlOptions();
						xmlOptions.setErrorListener( list );
						xmlOptions.setValidateTreatLaxAsSkip();
						xmlObject.validate( xmlOptions );

						for( Object o : list )
						{
							if( o instanceof XmlError )
								result.add( new AssertionError( ( XmlError )o ) );
							else
								result.add( new AssertionError( o.toString() ) );
						}

						asserted = true;
					}
					catch( XmlException e )
					{
						SoapUI.logError( e );
					}
				}
				else
				{
					asserted = true;
				}
			}
		}

		if( !asserted && result.isEmpty() )
		{
			result.add( new AssertionError( "Missing matching representation for request with contentType ["
					+ messageExchange.getResponseContentType() + "]" ) );
		}

		return result.toArray( new AssertionError[result.size()] );
	}

	private QName getResponseBodyElementName( RestMessageExchange messageExchange )
	{
		try
		{
			XmlObject xmlObject = XmlObject.Factory.parse( messageExchange.getResponseContentAsXml() );
			Element docElement = ( ( Document )xmlObject.getDomNode() ).getDocumentElement();

			return new QName( docElement.getNamespaceURI(), docElement.getLocalName() );
		}
		catch( XmlException e )
		{
			SoapUI.logError( e );
		}

		return null;
	}
}
