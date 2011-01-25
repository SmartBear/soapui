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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.eviware.soapui.SoapUI;

public class DateBoundary extends AbstractBoundary
{

	private static final int OFFSET = 10;
	public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	public SimpleDateFormat simpleDateFormat = new SimpleDateFormat( DATE_FORMAT );

	public DateBoundary( String maxExclusive, String maxInclusive, String minExclusive, String minInclusive )
	{
		super( null, null, null, null, null, maxExclusive, maxInclusive, minExclusive, minInclusive );
	}

	@Override
	public String outOfBoundary( int restrictionAttribute )
	{
		switch( restrictionAttribute )
		{
		case MAX_EXCLISIVE :
			return createDate( maxExclusive, ( int )( Math.random() * OFFSET ) );
		case MIN_EXCLISIVE :
			return createDate( minExclusive, -( int )( Math.random() * OFFSET ) );
		case MAX_INCLISIVE :
			return createDate( maxInclusive, ( int )( Math.random() * OFFSET + 1 ) );
		case MIN_INCLISIVE :
			return createDate( minInclusive, -( int )( Math.random() * OFFSET - 1 ) );
		default :
			return null;
		}
	}

	private String createDate( String restrictionDate, int days )
	{
		try
		{
			Date date = simpleDateFormat.parse( restrictionDate );
			Calendar calendar = Calendar.getInstance();
			calendar.setTime( date );
			calendar.add( Calendar.DAY_OF_YEAR, days );
			return simpleDateFormat.format( calendar.getTime() );
		}
		catch( ParseException e )
		{
			SoapUI.logError( e, "date : '" + restrictionDate + "' is not in proper format: " + DATE_FORMAT );
		}
		return null;
	}
}
