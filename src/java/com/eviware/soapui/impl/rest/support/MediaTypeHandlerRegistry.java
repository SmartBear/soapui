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

package com.eviware.soapui.impl.rest.support;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.impl.rest.support.handlers.DefaultMediaTypeHandler;
import com.eviware.soapui.impl.rest.support.handlers.HtmlMediaTypeHandler;
import com.eviware.soapui.impl.rest.support.handlers.JsonMediaTypeHandler;

public class MediaTypeHandlerRegistry
{
	private static List<MediaTypeHandler> mediaTypeHandlers = new ArrayList<MediaTypeHandler>();
	private static MediaTypeHandler defaultMediaTypeHandler = new DefaultMediaTypeHandler();

	static
	{
		mediaTypeHandlers.add( new JsonMediaTypeHandler() );
		mediaTypeHandlers.add( new HtmlMediaTypeHandler() );
	}

	public static MediaTypeHandler getTypeHandler( String contentType )
	{
		for( MediaTypeHandler handler : mediaTypeHandlers )
		{
			if( handler.canHandle( contentType ) )
				return handler;
		}

		return defaultMediaTypeHandler;
	}

	public static MediaTypeHandler getDefaultMediaTypeHandler()
	{
		return defaultMediaTypeHandler;
	}

	public static void setDefaultMediaTypeHandler( MediaTypeHandler defaultMediaTypeHandler )
	{
		MediaTypeHandlerRegistry.defaultMediaTypeHandler = defaultMediaTypeHandler;
	}
}
