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

package com.eviware.soapui;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassLoader that loads external jars
 * 
 * @author Ole
 */

public class SoapUIExtensionClassLoader extends URLClassLoader
{
	public SoapUIExtensionClassLoader( URL[] urls, ClassLoader parent )
	{
		super( urls, parent );
	}

	@Override
	public void addURL( URL url )
	{
		super.addURL( url );
	}

	public void addFile( File file ) throws MalformedURLException
	{
		addURL( file.toURI().toURL() );
	}

	private static Map<ClassLoader, SoapUIClassLoaderState> clStates = new HashMap<ClassLoader, SoapUIClassLoaderState>();

	public static SoapUIClassLoaderState ensure()
	{
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		SoapUIClassLoaderState state = clStates.get( contextClassLoader );

		if( state == null )
		{
			ClassLoader cl = contextClassLoader;

			while( cl != null && !( cl instanceof SoapUIExtensionClassLoader ) )
			{
				cl = cl.getParent();
			}

			state = new SoapUIClassLoaderState( cl == null ? contextClassLoader : null );
			clStates.put( contextClassLoader, state );
		}

		return state.activate();
	}

	public static class SoapUIClassLoaderState
	{
		private ClassLoader cl;

		private SoapUIClassLoaderState( ClassLoader cl )
		{
			this.cl = cl;
		}

		private SoapUIClassLoaderState activate()
		{
			if( cl != null && SoapUI.getSoapUICore() != null )
				Thread.currentThread().setContextClassLoader( SoapUI.getSoapUICore().getExtensionClassLoader() );

			return this;
		}

		public void restore()
		{
			if( cl != null )
				Thread.currentThread().setContextClassLoader( cl );
		}
	}

	public static SoapUIExtensionClassLoader create( String root, ClassLoader parent ) throws MalformedURLException
	{
		String extDir = System.getProperty( "soapui.ext.libraries" );

		File dir = extDir != null ? new File( extDir ) : new File( new File( root ), "ext" );
		List<URL> urls = new ArrayList<URL>();

		if( dir.exists() && dir.isDirectory() )
		{
			File[] files = dir.listFiles();
			for( File file : files )
			{
				if( file.getName().toLowerCase().endsWith( ".jar" ) )
				{
					urls.add( file.toURI().toURL() );
					SoapUI.log.info( "Adding [" + file.getAbsolutePath() + "] to extensions classpath" );
				}
			}
		}
		else
		{
			SoapUI.log.warn( "Missing folder [" + dir.getAbsolutePath() + "] for external libraries" );
		}

		return new SoapUIExtensionClassLoader( urls.toArray( new URL[urls.size()] ), parent );
	}
}