/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.support.definition.support;

import java.awt.Dimension;
import java.util.ArrayList;

import javax.wsdl.WSDLException;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.impl.wsdl.support.xsd.SchemaException;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;

public class InvalidDefinitionException extends SoapUIException
{
	private SchemaException schemaException;
	private WSDLException wsdlException;
	private XmlException xmlException;
	private Exception exception;
	private String message;

	public InvalidDefinitionException( SchemaException schemaException )
	{
		this.schemaException = schemaException;
	}

	public InvalidDefinitionException( WSDLException wsdlException )
	{
		this.wsdlException = wsdlException;
	}

	public InvalidDefinitionException( XmlException xmlException )
	{
		this.xmlException = xmlException;
	}

	public InvalidDefinitionException( Exception exception )
	{
		this.exception = exception;
	}

	public InvalidDefinitionException( String message )
	{
		this.message = message;
	}

	public Exception getException()
	{
		return exception;
	}

	public WSDLException getWsdlException()
	{
		return wsdlException;
	}

	public SchemaException getSchemaException()
	{
		return schemaException;
	}

	public void setMessage( String message )
	{
		this.message = message;
	}

	public String getDetailedMessage()
	{
		StringBuffer result = new StringBuffer();

		if( message != null )
		{
			result.append( message );
			result.append( "<hr>" );
		}

		if( exception != null )
		{
			result.append( exception.getMessage() );
		}
		else if( wsdlException != null )
		{
			result.append( wsdlException.getMessage() );
		}
		else if( xmlException != null )
		{
			XmlError error = xmlException.getError();
			result.append( error.getMessage() ).append( " on line " ).append( error.getLine() ).append( ", column " )
					.append( error.getColumn() );
		}
		else if( schemaException != null )
		{
			ArrayList<?> errorList = schemaException.getErrorList();

			if( errorList != null )
			{
				StringToStringMap doubles = new StringToStringMap();
				boolean appended = false;

				for( int c = 0; c < errorList.size(); c++ )
				{
					Object error = errorList.get( c );
					if( error instanceof XmlError )
					{
						XmlError xmlError = ( XmlError )error;
						String sourceName = xmlError.getSourceName();
						String message = xmlError.getMessage();

						if( !doubles.containsKey( message ) || !doubles.get( message ).equalsIgnoreCase( sourceName ) )
						{
							if( appended )
								result.append( "<hr>" );

							result.append( "<b>Source:</b> " ).append( sourceName ).append( "<br>" );
							result.append( "<b>Error:</b> " ).append( message ).append( "<br>" );
							appended = true;

							doubles.put( message, sourceName );
						}
					}
					else
					{
						if( appended )
							result.append( "<hr>" );

						result.append( "<b>Error:</b> " ).append( error.toString() ).append( "<br>" );
						appended = true;
					}
				}
			}
		}
		return result.toString();
	}

	public void show()
	{
		UISupport.showExtendedInfo( "Error loading WSDL",
				"There was something wrong with the WSDL you are trying to import",
				StringUtils.toHtml( getDetailedMessage() ), new Dimension( 600, 300 ) );
	}
}