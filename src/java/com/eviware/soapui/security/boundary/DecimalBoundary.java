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

public class DecimalBoundary extends AbstractBoundary
{
	private static final String DECIMAL_POINT = ".";
	private static final String AVAILABLE_VALUES = "1234567890";

	public DecimalBoundary( String length, String minLength, String maxLength, String totalDigits,
			String fractionDigits )
	{
		super( length, minLength, maxLength, totalDigits, fractionDigits );
	}

	@Override
	public String outOfBoundary( int restrictionAttribute )
	{
		switch( restrictionAttribute )
		{
		case LENGTH :
			return createCharacterArray( AVAILABLE_VALUES, Integer.valueOf( length ));
		case MIN_LENGTH :
			return createCharacterArray( AVAILABLE_VALUES, Integer.valueOf(minLength) - 1 );
		case MAX_LENGTH :
			return createCharacterArray( AVAILABLE_VALUES, Integer.valueOf(maxLength) - 1 );
		case TOTAL_DIGITS :
			return createCharacterArray( AVAILABLE_VALUES, Integer.valueOf(totalDigits) + 1 );
		case FRACTION_DIGITS :
			return createCharacterArray( AVAILABLE_VALUES, 1 ) + DECIMAL_POINT
					+ createCharacterArray( AVAILABLE_VALUES, Integer.valueOf(fractionDigits) + 1 );
		default :
			return null;
		}
	}
}
