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
import org.apache.xmlbeans.XmlOptions;
import org.xmlsoap.schemas.soap.envelope.EnvelopeDocument;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.support.Constants;

/**
 * SoapVersion for SOAP 1.1
 * 
 * @author ole.matzura
 */

public class SoapVersion11 extends AbstractSoapVersion
{
	private final static QName envelopeQName = new QName( Constants.SOAP11_ENVELOPE_NS, "Envelope" );
	private final static QName bodyQName = new QName( Constants.SOAP11_ENVELOPE_NS, "Body" );
	private final static QName faultQName = new QName( Constants.SOAP11_ENVELOPE_NS, "Fault" );
	private final static QName headerQName = new QName( Constants.SOAP11_ENVELOPE_NS, "Header" );

	SchemaTypeLoader soapSchema;
	SchemaType soapEnvelopeType;
	private XmlObject soapSchemaXml;
	private XmlObject soapEncodingXml;
	private SchemaType soapFaultType;

	public final static SoapVersion11 instance = new SoapVersion11();

	private SoapVersion11()
	{
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader( SoapUI.class.getClassLoader() );

		try
		{
			XmlOptions options = new XmlOptions();
			options.setCompileNoValidation();
			options.setCompileNoPvrRule();
			options.setCompileDownloadUrls();
			options.setCompileNoUpaRule();
			options.setValidateTreatLaxAsSkip();

			soapSchemaXml = XmlObject.Factory.parse( SoapUI.class
					.getResource( "/com/eviware/soapui/resources/xsds/soapEnvelope.xsd" ), options );
			soapSchema = XmlBeans.loadXsd( new XmlObject[] { soapSchemaXml } );

			soapEnvelopeType = soapSchema.findDocumentType( envelopeQName );
			soapFaultType = soapSchema.findDocumentType( faultQName );

			soapEncodingXml = XmlObject.Factory.parse( SoapUI.class
					.getResource( "/com/eviware/soapui/resources/xsds/soapEncoding.xsd" ), options );
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

	public SchemaType getEnvelopeType()
	{
		return EnvelopeDocument.type;
	}

	public String getEnvelopeNamespace()
	{
		return Constants.SOAP11_ENVELOPE_NS;
	}

	public String getEncodingNamespace()
	{
		return Constants.SOAP_ENCODING_NS;
	}

	public XmlObject getSoapEncodingSchema() throws XmlException, IOException
	{
		return soapEncodingXml;
	}

	public XmlObject getSoapEnvelopeSchema() throws XmlException, IOException
	{
		return soapSchemaXml;
	}

	public String toString()
	{
		return "SOAP 1.1";
	}

	public String getContentTypeHttpHeader( String encoding, String soapAction )
	{
		if( encoding == null || encoding.trim().length() == 0 )
			return getContentType();
		else
			return getContentType() + ";charset=" + encoding;
	}

	public String getSoapActionHeader( String soapAction )
	{
		if( soapAction == null || soapAction.length() == 0 )
		{
			soapAction = "\"\"";
		}
		else
		{
			soapAction = "\"" + soapAction + "\"";
		}

		return soapAction;
	}

	public String getContentType()
	{
		return "text/xml";
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

	public SchemaType getFaultType()
	{
		return soapFaultType;
	}

	public String getName()
	{
		return "SOAP 1.1";
	}

	public String getFaultDetailNamespace()
	{
		return "";
	}
}
