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

package com.eviware.soapui.support;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;

import java.io.File;

import com.eviware.soapui.SoapUI;

public class ScriptingSupport
{
	public static SoapUIGroovyShell createGsroovyShell( Binding binding )
	{
		// LoaderConfiguration config = new LoaderConfiguration();
		//		
		// String libraries = SoapUI.getSettings().getString(
		// ToolsSettings.SCRIPT_LIBRARIES, null );
		// if( libraries != null )
		// {
		// File libs = new File( libraries );
		// File[] list = libs.listFiles();
		//			
		// for( File lib : list)
		// {
		// if( lib.getName().toLowerCase().endsWith( ".jar" ))
		// {
		// config.addFile( lib );
		// }
		// }
		// }

		// RootLoader loader = new RootLoader( config.getClassPathUrls(), );
		GroovyClassLoader groovyClassLoader = new GroovyClassLoader( SoapUI.class.getClassLoader() );
		SoapUIGroovyShell groovyShell = binding == null ? new SoapUIGroovyShell( groovyClassLoader )
				: new SoapUIGroovyShell( groovyClassLoader, binding );

		return groovyShell;
	}

	public static class SoapUIGroovyShell extends GroovyShell
	{
		private final GroovyClassLoader classLoader;

		protected SoapUIGroovyShell( GroovyClassLoader classLoader, Binding binding )
		{
			super( classLoader, binding );

			this.classLoader = classLoader;

			reloadExternalClasses();
		}

		protected SoapUIGroovyShell( GroovyClassLoader classLoader )
		{
			super( classLoader );

			this.classLoader = classLoader;

			reloadExternalClasses();
		}

		public void reloadExternalClasses()
		{
			resetLoadedClasses();
			classLoader.clearCache();

			try
			{
				File scripts = new File( new File( "" ).getAbsolutePath() + File.separatorChar + "scripts" );
				if( scripts.exists() && scripts.isDirectory() )
				{
					File[] listFiles = scripts.listFiles();
					for( File file : listFiles )
					{
						if( file.isDirectory() || !file.getName().endsWith( ".groovy" ) )
							continue;

						System.out.println( "parsing " + file.getAbsolutePath() );
						classLoader.parseClass( file );
					}
				}
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}
	}
}
