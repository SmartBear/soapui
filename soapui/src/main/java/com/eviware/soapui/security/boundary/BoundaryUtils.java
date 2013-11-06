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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.eviware.soapui.SoapUI;

/**
 * @author nebojsa.tasic
 */
public class BoundaryUtils
{

	/**
	 * create string of specified size from random characters specified by
	 * availableValues
	 * 
	 * @param availableValues
	 * @param size
	 * @return
	 */
	public static String createCharacterArray( String availableValues, Integer size )
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

	/**
	 * returns one random character from specified availableValues string
	 * 
	 * @param availableValues
	 * @return character
	 */
	public static String randomCharacter( String availableValues )
	{
		int position = ( int )( Math.random() * availableValues.length() );
		return availableValues.substring( position, position + 1 );
	}

	/**
	 * 
	 * creates date in string representation that is differs from restrictionDate
	 * by daysOffset number of days
	 * 
	 * @param restrictionDate
	 * @param daysOffset
	 * @return date
	 */
	public static String createDate( String restrictionDate, int daysOffset, SimpleDateFormat format )
	{
		try
		{
			Date date = format.parse( restrictionDate );
			Calendar calendar = Calendar.getInstance();
			calendar.setTime( date );
			calendar.add( Calendar.DAY_OF_YEAR, daysOffset );
			return format.format( calendar.getTime() );
		}
		catch( ParseException e )
		{
			SoapUI.logError( e, "date : '" + restrictionDate + "' is not in proper format: " + format.toPattern() );
		}
		return null;
	}

	/**
	 * 
	 * creates time in string representation that is differs from minutesOffset
	 * by minutesOffset number of minutes
	 * 
	 * @param restrictionTime
	 * @param minutesOffset
	 * @return date
	 */
	public static String createTime( String restrictionTime, int minutesOffset, SimpleDateFormat format )
	{
		try
		{
			Date date = format.parse( restrictionTime );
			Calendar calendar = Calendar.getInstance();
			calendar.setTime( date );
			calendar.add( Calendar.MINUTE, minutesOffset );
			return format.format( calendar.getTime() );
		}
		catch( ParseException e )
		{
			SoapUI.logError( e, "time : '" + restrictionTime + "' is not in proper format: " + format.toPattern() );
		}
		return null;
	}
}
