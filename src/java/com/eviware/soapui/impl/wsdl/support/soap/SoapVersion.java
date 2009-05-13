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
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlValidationError;

import com.eviware.soapui.support.StringUtils;

/**
 * Public behaviour for a SOAP Version
 * 
 * @author ole.matzura
 */

public interface SoapVersion
{
	public static final SoapVersion11 Soap11 = SoapVersion11.instance;
	public static final SoapVersion12 Soap12 = SoapVersion12.instance;

	public QName getEnvelopeQName();

	public QName getBodyQName();

	public QName getHeaderQName();

	public void validateSoapEnvelope( String soapMessage, List<XmlError> errors );

	public String getContentTypeHttpHeader( String encoding, String soapAction );

	public String getEnvelopeNamespace();

	public String getFaultDetailNamespace();

	public String getEncodingNamespace();

	public XmlObject getSoapEncodingSchema() throws XmlException, IOException;

	public XmlObject getSoapEnvelopeSchema() throws XmlException, IOException;

	/**
	 * Checks if the specified validation error should be ignored for a message
	 * with this SOAP version. (The SOAP-spec may allow some constructions not
	 * allowed by the corresponding XML-Schema)
	 */

	public boolean shouldIgnore( XmlValidationError xmlError );

	public String getContentType();

	public SchemaType getEnvelopeType();

	public SchemaType getFaultType();

	public String getName();

	/**
	 * Utilities
	 * 
	 * @author ole.matzura
	 */

	public static class Utils
	{
		public static SoapVersion getSoapVersionForContentType( String contentType, SoapVersion def )
		{
			if( StringUtils.isNullOrEmpty( contentType ) )
				return def;

			SoapVersion soapVersion = contentType.startsWith( SoapVersion.Soap11.getContentType() ) ? SoapVersion.Soap11
					: null;
			soapVersion = soapVersion == null && contentType.startsWith( SoapVersion.Soap12.getContentType() ) ? SoapVersion.Soap12
					: soapVersion;

			return soapVersion == null ? def : soapVersion;
		}
	}

	public String getSoapActionHeader( String soapAction );
}
