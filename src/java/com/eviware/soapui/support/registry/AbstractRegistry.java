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

package com.eviware.soapui.support.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.RegistryEntryConfig;

public abstract class AbstractRegistry<T1 extends RegistryEntry<T2, T3>, T2 extends RegistryEntryConfig, T3 extends Object>
{
	private Map<String, Class<? extends T1>> registry = new HashMap<String, Class<? extends T1>>();

	public void mapType( String type, Class<? extends T1> clazz )
	{
		registry.put( type, clazz );
	}

	public T1 create( String type, T3 parent )
	{
		if( registry.containsKey( type ) )
		{
			T2 config = addNewConfig( parent );
			config.setType( type );
			return build( config, parent );
		}
		else
			throw new RuntimeException( "Invalid type [" + type + "]" );
	}

	protected abstract T2 addNewConfig( T3 parent );

	public T1 build( T2 config, T3 parent )
	{
		try
		{
			Class<? extends T1> clazz = registry.get( config.getType() );
			if( clazz == null )
			{
				return null;
			}

			T1 entry = clazz.newInstance();
			entry.init( config, parent );
			return entry;
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

		return null;
	}

	public String[] getTypes()
	{
		return registry.keySet().toArray( new String[registry.size()] );
	}

	public String[] getTypesWithInterface( Class<?> clazz )
	{
		List<String> result = new ArrayList<String>();

		for( String type : registry.keySet() )
		{
			if( Arrays.asList( registry.get( type ).getInterfaces() ).contains( clazz ) )
				result.add( type );
		}

		return result.toArray( new String[result.size()] );
	}
}
