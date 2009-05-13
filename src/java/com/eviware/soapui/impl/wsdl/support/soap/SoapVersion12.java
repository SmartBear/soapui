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

package com.eviware.soapui.impl.wsdl.support.soap;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3.x2003.x05.soapEnvelope.FaultDocument;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.Constants;
import com.eviware.soapui.support.StringUtils;

/**
 * SoapVersion for SOAP 1.2
 * 
 * @author ole.matzura
 */

public class SoapVersion12 extends AbstractSoapVersion
{
	private final static QName envelopeQName = new QName( Constants.SOAP12_ENVELOPE_NS, "Envelope" );
	private final static QName bodyQName = new QName( Constants.SOAP12_ENVELOPE_NS, "Body" );
	private final static QName faultQName = new QName( Constants.SOAP11_ENVELOPE_NS, "Fault" );
	private final static QName headerQName = new QName( Constants.SOAP12_ENVELOPE_NS, "Header" );
	public final static SoapVersion12 instance = new SoapVersion12();

	private SchemaTypeLoader soapSchema;
	private XmlObject soapSchemaXml;
	private XmlObject soapEncodingXml;

	private SoapVersion12()
	{
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader( SoapUI.class.getClassLoader() );

		try
		{
			soapSchemaXml = XmlObject.Factory.parse( SoapUI.class
					.getResource( "/com/eviware/soapui/resources/xsds/soapEnvelope12.xsd" ) );
			soapSchema = XmlBeans.loadXsd( new XmlObject[] { soapSchemaXml } );
			soapEncodingXml = XmlObject.Factory.parse( SoapUI.class
					.getResource( "/com/eviware/soapui/resources/xsds/soapEncoding12.xsd" ) );
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
		finally
		{
			Thread.currentThread().setContextClassLoader( contextClassLoader );
		}
	}

	public String getEncodingNamespace()
	{
		return "http://www.w3.org/2003/05/soap-encoding";
	}

	public XmlObject getSoapEncodingSchema() throws XmlException, IOException
	{
		return soapEncodingXml;
	}

	public XmlObject getSoapEnvelopeSchema() throws XmlException, IOException
	{
		return soapSchemaXml;
	}

	public String getEnvelopeNamespace()
	{
		return Constants.SOAP12_ENVELOPE_NS;
	}

	public SchemaType getEnvelopeType()
	{
		return EnvelopeDocument.type;
	}

	public String toString()
	{
		return "SOAP 1.2";
	}

	public String getContentTypeHttpHeader( String encoding, String soapAction )
	{
		String result = getContentType();

		if( encoding != null && encoding.trim().length() > 0 )
			result += ";charset=" + encoding;

		if( StringUtils.hasContent( soapAction ) )
			result += ";action=" + StringUtils.quote( soapAction );

		return result;
	}

	public String getSoapActionHeader( String soapAction )
	{
		// SOAP 1.2 has this in the contenttype
		return null;
	}

	public String getContentType()
	{
		return "application/soap+xml";
	}

	public QName getBodyQName()
	{
		return bodyQName;
	}

	public QName getEnvelopeQName()
	{
		return envelopeQName;
	}

	public QName getHeaderQName()
	{
		return headerQName;
	}

	protected SchemaTypeLoader getSoapEnvelopeSchemaLoader()
	{
		return soapSchema;
	}

	public static QName getFaultQName()
	{
		return faultQName;
	}

	public SchemaType getFaultType()
	{
		return FaultDocument.type;
	}

	public String getName()
	{
		return "SOAP 1.2";
	}

	public String getFaultDetailNamespace()
	{
		return getEnvelopeNamespace();
	}
}
