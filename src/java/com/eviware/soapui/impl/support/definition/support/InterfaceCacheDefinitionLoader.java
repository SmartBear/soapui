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

package com.eviware.soapui.impl.support.definition.support;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import com.eviware.soapui.impl.support.definition.DefinitionCache;
import com.eviware.soapui.impl.support.definition.InterfaceDefinitionPart;
import com.eviware.soapui.impl.wsdl.support.wsdl.AbstractWsdlDefinitionLoader;

/**
 * WsdlLoader for cached definitions
 * 
 * @author ole.matzura
 */

public class InterfaceCacheDefinitionLoader extends AbstractWsdlDefinitionLoader
{
	private String rootInConfig = "";
	private DefinitionCache config;

	public InterfaceCacheDefinitionLoader( DefinitionCache config )
	{
		super( config.getRootPart().getUrl() );
		this.config = config;
	}

	public InputStream load( String url ) throws Exception
	{
		XmlObject xmlObject = loadXmlObject( url, null );
		return xmlObject == null ? null : xmlObject.newInputStream();
	}

	public XmlObject loadXmlObject( String url, XmlOptions options ) throws Exception
	{
		// required for backwards compatibility when the entire path was stored
		if( url.endsWith( config.getRootPart().getUrl() ) )
		{
			rootInConfig = url.substring( 0, url.length() - config.getRootPart().getUrl().length() );
		}

		List<InterfaceDefinitionPart> partList = config.getDefinitionParts();
		for( InterfaceDefinitionPart part : partList )
		{
			if( ( rootInConfig + part.getUrl() ).equalsIgnoreCase( url ) )
			{
				return getPartContent( part );
			}
		}

		// hack: this could be due to windows -> unix, try again with replaced '/'
		if( File.separatorChar == '/' )
		{
			url = url.replace( '/', '\\' );

			for( InterfaceDefinitionPart part : partList )
			{
				if( ( rootInConfig + part.getUrl() ).equalsIgnoreCase( url ) )
				{
					return getPartContent( part );
				}
			}
		}
		// or the other way around..
		else if( File.separatorChar == '\\' )
		{
			url = url.replace( '\\', '/' );

			for( InterfaceDefinitionPart part : partList )
			{
				if( ( rootInConfig + part.getUrl() ).equalsIgnoreCase( url ) )
				{
					return getPartContent( part );
				}
			}
		}

		return null;
	}

	public static XmlObject getPartContent( InterfaceDefinitionPart part ) throws XmlException
	{
		return XmlObject.Factory.parse( part.getContent(), new XmlOptions().setLoadLineNumbers() );
	}

	public void close()
	{
	}

	public void setNewBaseURI( String uri )
	{
		// not implemented
	}

	public String getFirstNewURI()
	{
		return getBaseURI();
	}
}