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

/**
 * @author joel.jonsson
 */
public class DebugWatch
{
	private final String name;
	long start;

	public DebugWatch( String name )
	{
		System.out.println( String.format( "START %s", name ) );
		this.name = name;
		start = System.nanoTime();
	}

	public void print( String message )
	{
		System.out.println( String.format( "%s %d %s", name, ( System.nanoTime() - start ) / 1000000, message ) );
	}
}
