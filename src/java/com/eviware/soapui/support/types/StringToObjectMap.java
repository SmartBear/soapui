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

package com.eviware.soapui.support.types;

import java.util.HashMap;
import java.util.Map;

/**
 * HashMap&lt;String,String&gt;
 * 
 * @author Ole.Matzura
 */

public class StringToObjectMap extends HashMap<String, Object>
{
	public StringToObjectMap()
	{
		super();
	}

	public StringToObjectMap( int initialCapacity, float loadFactor )
	{
		super( initialCapacity, loadFactor );
	}

	public StringToObjectMap( int initialCapacity )
	{
		super( initialCapacity );
	}

	public StringToObjectMap( Map<? extends String, ? extends Object> m )
	{
		super( m );
	}

}
