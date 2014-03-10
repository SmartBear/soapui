/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com 
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.monitor;

import org.apache.commons.lang.StringUtils;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author joel.jonsson
 */
public class ContentTypes
{
	private List<ContentType> contentTypes;

	private ContentTypes( List<ContentType> contentTypes )
	{
		this.contentTypes = contentTypes;
	}

	public static ContentTypes of( String contentTypes )
	{
		List<ContentType> contentTypeList = new ArrayList<ContentType>();
		for( String ct : contentTypes.split( "," ) )
		{
			try
			{
				contentTypeList.add( new ContentType( ct.trim() ) );
			}
			catch( ParseException ignore )
			{
			}
		}
		return new ContentTypes( contentTypeList );
	}

	public boolean matches( String value )
	{
		for( ContentType contentType : contentTypes )
		{
			try
			{
				ContentType respondedContentType = new ContentType( value );
				if( contentTypeMatches( contentType, respondedContentType ) )
				{
					return true;
				}
			}
			catch( ParseException ignore )
			{
			}
		}
		return false;
	}

	@Override
	public String toString()
	{
		return StringUtils.join( contentTypes, ", " );
	}

	private boolean contentTypeMatches( ContentType contentType, ContentType respondedContentType )
	{
		// ContentType doesn't take wildcards into account for the primary type, but we want to do that
		return contentType.match( respondedContentType ) ||
				( ( contentType.getPrimaryType().charAt( 0 ) == '*'
						|| respondedContentType.getPrimaryType().charAt( 0 ) == '*' )
						&& ( contentType.getSubType().charAt( 0 ) == '*'
						|| respondedContentType.getSubType().charAt( 0 ) == '*'
						|| contentType.getSubType().equalsIgnoreCase( respondedContentType.getSubType() ) ) );
	}
}
