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
package com.eviware.soapui.tools;

import java.io.File;

/**
 * @author joel.jonsson
 */
public class JfxrtLocator
{
	public static void main( String[] args )
	{
		String javaHome = System.getProperty( "java.home" );
		File jfxrtFile = new File( new File( javaHome, "lib" ), "jfxrt.jar" );
		System.out.println(jfxrtFile.getAbsolutePath());
	}
}
