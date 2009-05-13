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

/**
 * General SoapUI exception class
 * 
 * @author Ole.Matzura
 */

public class SoapUIException extends Exception
{
	public SoapUIException()
	{
		super();
	}

	public SoapUIException( String message )
	{
		super( message );
	}

	public SoapUIException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public SoapUIException( Throwable cause )
	{
		super( cause );
	}
}
