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
package com.eviware.soapui.security.boundary;

/**
 * @author nebojsa.tasic
 */
public class DecimalBoundary extends AbstractBoundary
{
	private static final String DECIMAL_POINT = ".";
	private static final String AVAILABLE_VALUES = "1234567890";

	@Override
	public String outOfBoundary( int restrictionAttribute, String value )
	{
		switch( restrictionAttribute )
		{
		case LENGTH :
			return BoundaryUtils.createCharacterArray( AVAILABLE_VALUES, Integer.valueOf( value ) );
		case MIN_LENGTH :
			return BoundaryUtils.createCharacterArray( AVAILABLE_VALUES, Integer.valueOf( value ) - 1 );
		case MAX_LENGTH :
			return BoundaryUtils.createCharacterArray( AVAILABLE_VALUES, Integer.valueOf( value ) + 1 );
		case TOTAL_DIGITS :
			return BoundaryUtils.createCharacterArray( AVAILABLE_VALUES, Integer.valueOf( value ) + 1 );
		case FRACTION_DIGITS :
			return BoundaryUtils.createCharacterArray( AVAILABLE_VALUES, 1 ) + DECIMAL_POINT
					+ BoundaryUtils.createCharacterArray( AVAILABLE_VALUES, Integer.valueOf( value ) + 1 );
		default :
			return null;
		}
	}
}
