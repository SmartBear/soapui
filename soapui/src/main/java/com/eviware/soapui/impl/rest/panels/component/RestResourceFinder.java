/*
 *  SoapUI, copyright (C) 2004-2013 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.rest.panels.component;

import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.support.RestUtils;

/**
 * @author Anders Jaensson
 */
public class RestResourceFinder
{
	private RestResource resource;

	public RestResourceFinder( RestResource resource )
	{
		this.resource = resource;
	}

	public RestResource findResourceAt( int caretPosition )
	{
		for( RestResource r : RestUtils.extractAncestorsParentFirst( resource ) )
		{
			if( caretPosition <= r.getFullPath().length() )
			{
				return r;
			}
		}
		return resource;
	}
}
