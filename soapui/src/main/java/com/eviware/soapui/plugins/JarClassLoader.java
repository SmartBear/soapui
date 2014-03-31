/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarInputStream;

public class JarClassLoader extends URLClassLoader
{
	public JarClassLoader( File jarFile, ClassLoader parent ) throws IOException
	{
		super( new URL[] { jarFile.toURI().toURL()}, parent );
		addLibrariesIn( jarFile );
	}

	private void addLibrariesIn( File jarFile ) throws IOException
	{
		JarInputStream jarInputStream = new JarInputStream( new FileInputStream( jarFile ) );
		//TODO: extract JAR entries to temp files, then add file:// URLs
	}


}
