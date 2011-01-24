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
package com.eviware.soapui.security.boundary;

import com.eviware.soapui.SoapUI;

public abstract class AbstractBoundary implements Boundary
{
	protected Integer length;
	protected Integer minLength;
	protected Integer maxLength;
	protected Integer totalDigits;
	protected Integer fractionDigits;

	public AbstractBoundary( Integer length, Integer minLength, Integer maxLength, Integer totalDigits,
			Integer fractionDigits )
	{
		this.length = length;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.totalDigits = totalDigits;
		this.fractionDigits = fractionDigits;
	}


	protected String createCharacterArray( String availableValues, Integer size )
	{
		if( size == null )
		{
			SoapUI.log.error( "size is not specified!" );
			return null;
		}
		StringBuilder sb = new StringBuilder( size );
		for( int i = 0; i < size; i++ )
		{
			sb.append( randomCharacter( availableValues ) );
		}
		return sb.toString();
	}

	private String randomCharacter( String availableValues )
	{
		int position = ( int )( Math.random() * availableValues.length() );
		return availableValues.substring( position, position + 1 );
	}

}
