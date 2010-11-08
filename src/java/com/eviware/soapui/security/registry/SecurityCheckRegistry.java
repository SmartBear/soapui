/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.registry;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.config.TestStepConfig;

/**
 * Registry of SecurityCheck factories
 * 
 * @author soapUI team
 */

public class SecurityCheckRegistry
{
	private static SecurityCheckRegistry instance;
	private List<SecurityCheckFactory> factories = new ArrayList<SecurityCheckFactory>();

	public SecurityCheckRegistry()
	{
		addFactory( new GroovySecurityCheckFactory() );
	
	}

	public SecurityCheckFactory getFactory( String type )
	{
		for( SecurityCheckFactory factory : factories )
			if( factory.getType().equals( type ) )
				return factory;

		return null;
	}

	public void addFactory( SecurityCheckFactory factory )
	{
		removeFactory( factory.getType() );
		factories.add( factory );
	}

	public void removeFactory( String type )
	{
		for( SecurityCheckFactory factory : factories )
		{
			if( factory.getType().equals( type ) )
			{
				factories.remove( factory );
				break;
			}
		}
	}

	public static synchronized SecurityCheckRegistry getInstance()
	{
		if( instance == null )
			instance = new SecurityCheckRegistry();

		return instance;
	}

	public SecurityCheckFactory[] getFactories()
	{
		return factories.toArray( new SecurityCheckFactory[factories.size()] );
	}

	public boolean hasFactory( SecurityCheckConfig config )
	{
		return getFactory( config.getType() ) != null;
	}
}
