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

public class IntegerBoundary extends AbstractBoundary
{
	private static final String AVAILABLE_VALUES = "1234567890";

	public IntegerBoundary( Integer length, Integer minLength, Integer maxLength, Integer totalDigits )
	{
		super( length, minLength, maxLength, totalDigits, null );
	}

	@Override
	public String outOfBoundary( int restrictionAttribute )
	{
		switch( restrictionAttribute )
		{
		case LENGTH :
			return createCharacterArray( AVAILABLE_VALUES, length );
		case MIN_LENGTH :
			return createCharacterArray( AVAILABLE_VALUES, minLength - 1 );
		case MAX_LENGTH :
			return createCharacterArray( AVAILABLE_VALUES, maxLength - 1 );
		case TOTAL_DIGITS :
			return createCharacterArray( AVAILABLE_VALUES, totalDigits + 1 );
		default :
			return null;
		}
	}
}
