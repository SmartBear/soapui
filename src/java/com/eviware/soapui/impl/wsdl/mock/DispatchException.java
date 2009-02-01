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

package com.eviware.soapui.impl.wsdl.mock;

/**
 * Exception thrown during dispatching of HTTP Requests to a WsdlMockService
 * 
 * @author ole.matzura
 */

public class DispatchException extends Exception
{
	public DispatchException( String arg0, Throwable arg1 )
	{
		super( arg0, arg1 );
	}

	public DispatchException( String arg0 )
	{
		super( arg0 );
	}

	public DispatchException( Throwable arg0 )
	{
		super( arg0 );
	}
}
