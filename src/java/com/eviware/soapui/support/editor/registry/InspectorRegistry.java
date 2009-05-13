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

package com.eviware.soapui.support.editor.registry;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry of registered XmlInspectorFactories
 * 
 * @author ole.matzura
 */

public class InspectorRegistry
{
	private static InspectorRegistry instance;
	private List<InspectorFactory> factories = new ArrayList<InspectorFactory>();

	public InspectorRegistry()
	{
	}

	public void addFactory( InspectorFactory factory )
	{
		for( int c = 0; c < factories.size(); c++ )
		{
			InspectorFactory f = factories.get( c );
			if( f.getInspectorId().equals( factory.getInspectorId() ) )
			{
				factories.set( c, factory );
				return;
			}
		}

		factories.add( factory );
	}

	public static final InspectorRegistry getInstance()
	{
		if( instance == null )
			instance = new InspectorRegistry();

		return instance;
	}

	public void removeFactory( InspectorFactory factory )
	{
		factories.remove( factory );
	}

	public InspectorFactory[] getFactories()
	{
		return factories.toArray( new InspectorFactory[factories.size()] );
	}

	public InspectorFactory[] getFactoriesOfType( Class<?> type )
	{
		List<InspectorFactory> result = new ArrayList<InspectorFactory>();
		for( InspectorFactory factory : factories )
		{
			if( type.isAssignableFrom( factory.getClass() ) )
				result.add( factory );
		}

		return result.toArray( new InspectorFactory[result.size()] );
	}
}
