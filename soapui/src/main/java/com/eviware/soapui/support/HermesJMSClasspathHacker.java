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

package com.eviware.soapui.support;

import hermes.JAXBHermesLoader;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class HermesJMSClasspathHacker
{
	private static void addFile( String s ) throws IOException
	{
		File f = new File( s );
		addFile( f );
	}// end method

	private static void addFile( File f ) throws IOException
	{
		addURL( f.toURI().toURL() );
	}// end method

	private static void addURL( URL u ) throws IOException
	{
		ClassLoader classLoader = JAXBHermesLoader.class.getClassLoader();
		// ClasspathHacker.addUrlToClassLoader( u, classLoader );
	}// end method

}// end class
