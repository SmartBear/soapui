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

package com.eviware.soapui.impl.wsdl.support;

import java.net.URL;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import com.eviware.soapui.impl.wsdl.support.xsd.SchemaLoader;

public class UrlSchemaLoader implements SchemaLoader
{
	private String baseURI;

	public UrlSchemaLoader( String baseURI )
	{
		this.baseURI = baseURI;
	}

	public XmlObject loadXmlObject( String wsdlUrl, XmlOptions options ) throws Exception
	{
		return XmlObject.Factory.parse( new URL( wsdlUrl ), options );
	}

	public String getBaseURI()
	{
		return baseURI;
	}
}
