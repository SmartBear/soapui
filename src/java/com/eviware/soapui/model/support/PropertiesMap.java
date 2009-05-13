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

package com.eviware.soapui.model.support;

import java.util.Map;

import com.eviware.soapui.support.types.StringToObjectMap;

/**
 * Specialization of HashMap&lt;String,Object&gt;
 * 
 * @author Ole.Matzura
 */

public class PropertiesMap extends StringToObjectMap
{
	public PropertiesMap()
	{
		super();
	}

	public PropertiesMap( int initialCapacity, float loadFactor )
	{
		super( initialCapacity, loadFactor );
	}

	public PropertiesMap( int initialCapacity )
	{
		super( initialCapacity );
	}

	public PropertiesMap( Map<? extends String, ? extends Object> m )
	{
		super( m );
	}
}
