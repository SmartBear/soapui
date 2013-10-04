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

package com.eviware.soapui.support.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SoapUIFactoriesConfig;
import com.eviware.soapui.config.SoapUIFactoryConfig;
import com.eviware.soapui.config.SoapuiFactoriesDocumentConfig;

public class SoapUIFactoryRegistry
{
	private Map<Class<?>, List<Object>> factories = new HashMap<Class<?>, List<Object>>();
	private Map<Class<?>, SoapUIFactoryConfig> factoryConfigs = new HashMap<Class<?>, SoapUIFactoryConfig>();
	private final static Logger log = Logger.getLogger( SoapUIFactoryRegistry.class );

	public SoapUIFactoryRegistry( InputStream config )
	{
		if( config != null )
			addConfig( config, getClass().getClassLoader() );
	}

	public void addConfig( InputStream config, ClassLoader classLoader )
	{
		try
		{
			SoapuiFactoriesDocumentConfig configDocument = SoapuiFactoriesDocumentConfig.Factory.parse( config );
			SoapUIFactoriesConfig soapuiListeners = configDocument.getSoapuiFactories();

			for( SoapUIFactoryConfig factoryConfig : soapuiListeners.getFactoryList() )
			{
				try
				{
					String factoryTypeName = factoryConfig.getFactoryType();
					String factoryClassName = factoryConfig.getFactoryClass();

					Class<?> factoryType = Class.forName( factoryTypeName, true, classLoader );
					Class<?> factoryClass = Class.forName( factoryClassName, true, classLoader );

					if( !factoryType.isAssignableFrom( factoryClass ) )
					{
						throw new RuntimeException( "Factory class: " + factoryClassName + " must be of type: "
								+ factoryTypeName );
					}
					// make sure the class can be instantiated even if factory
					// will instantiate interfaces only on demand
					Object obj = factoryClass.newInstance();
					if( obj instanceof InitializableFactory )
					{
						( ( InitializableFactory )obj ).init( factoryConfig );
					}

					getLog().info( "Adding factory [" + factoryClass + "]" );
					addFactory( factoryType, obj );
				}
				catch( Exception e )
				{
					System.err.println( "Error initializing Listener: " + e );
				}
			}
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
		finally
		{
			try
			{
				config.close();
			}
			catch( IOException e )
			{
				SoapUI.logError( e );
			}
		}
	}

	private Logger getLog()
	{
		return DefaultSoapUICore.log == null ? log : DefaultSoapUICore.log;
	}

	public void addFactory( Class<?> factoryType, Object factory )
	{
		if( !factories.containsKey( factoryType ) )
			factories.put( factoryType, new ArrayList<Object>() );

		factories.get( factoryType ).add( factory );
	}

	public void removeFactory( Class<?> factoryType, Object factory )
	{
		if( factories.containsKey( factoryType ) )
			factories.get( factoryType ).remove( factory );
	}

	@SuppressWarnings( "unchecked" )
	public <T extends Object> List<T> getFactories( Class<T> factoryType )
	{
		List<T> result = new ArrayList<T>();

		if( factories.containsKey( factoryType ) )
			result.addAll( ( Collection<? extends T> )factories.get( factoryType ) );

		return result;
	}
}
