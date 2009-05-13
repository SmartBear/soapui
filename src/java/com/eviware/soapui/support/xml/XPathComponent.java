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
package com.eviware.soapui.support.xml;

import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Separates a path component into its parts.
 * 
 * @author lars
 */
public class XPathComponent
{
	private String namespace;
	private String prefix;
	private String localNameWithoutBraces;

	// index and conditions, for example "[1]" or "[x > 3]"
	private String braces;

	public XPathComponent( String c, StringToStringMap prefixMap )
	{
		String localName;
		int ix = c.indexOf( ':' );
		if( ix >= 0 )
		{
			prefix = c.substring( 0, ix );
			localName = c.substring( ix + 1 );
			namespace = prefixMap.get( prefix );
		}
		else
		{
			prefix = null;
			localName = c;
			namespace = null;
		}
		ix = localName.indexOf( '[' );
		if( ix >= 0 )
		{
			localNameWithoutBraces = localName.substring( 0, ix );
			braces = localName.substring( ix );
		}
		else
		{
			localNameWithoutBraces = localName;
			braces = "";
		}
		assert localName.equals( localNameWithoutBraces + braces ) : localName + " != " + localNameWithoutBraces + " + "
				+ braces;
	}

	@Override
	public String toString()
	{
		if( prefix != null )
			return prefix + ":" + localNameWithoutBraces + braces;
		else
			return localNameWithoutBraces + braces;
	}

	public String getNamespace()
	{
		return namespace;
	}

	public boolean hasPrefix()
	{
		return prefix != null;
	}

	public String getPrefix()
	{
		if( prefix == null )
			return "";
		else
			return prefix;
	}

	public String getLocalName()
	{
		return localNameWithoutBraces;
	}

	public String getBraces()
	{
		return braces;
	}

	public String getFullNameWithPrefix()
	{
		return getFullNameWithPrefix( localNameWithoutBraces );
	}

	public String getFullNameWithPrefix( String aLocalName )
	{
		return ( hasPrefix() ? getPrefix() + ":" : "" ) + aLocalName + getBraces();
	}
}