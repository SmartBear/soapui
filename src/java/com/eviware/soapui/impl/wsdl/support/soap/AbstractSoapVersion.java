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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlValidationError;

/**
 * Common behaviour for all SOAP Versions
 * 
 * @author ole.matzura
 */

public abstract class AbstractSoapVersion implements SoapVersion
{
	private final static Logger log = Logger.getLogger( AbstractSoapVersion.class );

	public void validateSoapEnvelope( String soapMessage, List<XmlError> errors )
	{
		List<XmlError> errorList = new ArrayList<XmlError>();

		try
		{
			XmlOptions xmlOptions = new XmlOptions();
			xmlOptions.setLoadLineNumbers();
			xmlOptions.setValidateTreatLaxAsSkip();
			xmlOptions.setLoadLineNumbers( XmlOptions.LOAD_LINE_NUMBERS_END_ELEMENT );
			XmlObject xmlObject = getSoapEnvelopeSchemaLoader().parse( soapMessage, getEnvelopeType(), xmlOptions );
			xmlOptions.setErrorListener( errorList );
			xmlObject.validate( xmlOptions );
		}
		catch( XmlException e )
		{
			if( e.getErrors() != null )
				errorList.addAll( e.getErrors() );

			errors.add( XmlError.forMessage( e.getMessage() ) );
		}
		catch( Exception e )
		{
			errors.add( XmlError.forMessage( e.getMessage() ) );
		}
		finally
		{
			for( XmlError error : errorList )
			{
				if( error instanceof XmlValidationError && shouldIgnore( ( XmlValidationError )error ) )
				{
					log.warn( "Ignoring validation error: " + error.toString() );
					continue;
				}

				errors.add( error );
			}
		}
	}

	protected abstract SchemaTypeLoader getSoapEnvelopeSchemaLoader();

	public boolean shouldIgnore( XmlValidationError error )
	{
		QName offendingQName = error.getOffendingQName();
		if( offendingQName != null )
		{
			if( offendingQName.equals( new QName( getEnvelopeNamespace(), "encodingStyle" ) ) )
			{
				return true;
			}
			else if( offendingQName.equals( new QName( getEnvelopeNamespace(), "mustUnderstand" ) ) )
			{
				return true;
			}
		}

		return false;
	}

	public abstract SchemaType getFaultType();

	public abstract SchemaType getEnvelopeType();
}