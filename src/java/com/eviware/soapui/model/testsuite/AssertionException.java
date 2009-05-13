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

package com.eviware.soapui.model.testsuite;

import com.eviware.soapui.support.SoapUIException;

/**
 * Exception thrown during assertion
 * 
 * @author Ole.Matzura
 */

public class AssertionException extends SoapUIException
{
	private AssertionError[] errors;

	public AssertionException( AssertionError error )
	{
		this( new AssertionError[] { error } );
	}

	public AssertionException( AssertionError[] errors )
	{
		this.errors = new AssertionError[errors.length];
		for( int c = 0; c < errors.length; c++ )
			this.errors[c] = errors[c];
	}

	public int getErrorCount()
	{
		return errors.length;
	}

	public AssertionError getErrorAt( int c )
	{
		return errors[c];
	}

	public AssertionError[] getErrors()
	{
		return errors;
	}

	public String getMessage()
	{
		StringBuffer result = new StringBuffer();
		for( int c = 0; c < errors.length; c++ )
		{
			if( c > 0 )
				result.append( '\n' );
			result.append( errors[c].getMessage() );
		}

		return result.toString();
	}

}
