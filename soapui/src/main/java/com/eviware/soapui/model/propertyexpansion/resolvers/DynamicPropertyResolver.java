/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.model.propertyexpansion.resolvers;

import java.util.HashMap;
import java.util.Map;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.propertyexpansion.resolvers.providers.ProjectDirProvider;
import com.eviware.soapui.model.propertyexpansion.resolvers.providers.WorkspaceDirProvider;

public class DynamicPropertyResolver implements PropertyResolver
{
	private static Map<String, ValueProvider> providers = new HashMap<String, ValueProvider>();

	static
	{
		addProvider( "projectDir", new ProjectDirProvider() );
		addProvider( "workspaceDir", new WorkspaceDirProvider() );

		for( ValueProviderFactory obj : SoapUI.getFactoryRegistry().getFactories( ValueProviderFactory.class ) )
		{
			addProvider( obj.getValueId(), obj.createValueProvider() );
		}
	}

	public DynamicPropertyResolver()
	{
	}

	public String resolveProperty( PropertyExpansionContext context, String name, boolean globalOverride )
	{
		ValueProvider provider = providers.get( name );
		if( provider != null )
			return provider.getValue( context );

		return null;
	}

	public static void addProvider( String propertyName, ValueProvider provider )
	{
		providers.put( propertyName, provider );
	}

	public interface ValueProvider
	{
		String getValue( PropertyExpansionContext context );
	}

	public interface ValueProviderFactory
	{
		public ValueProvider createValueProvider();

		public String getValueId();
	}
}
