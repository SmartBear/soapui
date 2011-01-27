/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
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

import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

public class DateBoundaryTest
{
//	DateBoundary dateBoundary;
//
////	@Before
//	public void setUp() throws Exception
//	{
//		String today = DateBoundary.simpleDateFormat.format( Calendar.getInstance().getTime() );
//		dateBoundary = new DateBoundary( today, today, today, today );
//	}
//
////	@Test
//	public void testOutOfBoundaryMinExclusive() throws ParseException
//	{
//		String outOfBoundaryDate = dateBoundary.outOfBoundary( Boundary.MIN_EXCLISIVE );
//		Calendar calendar1 = Calendar.getInstance();
//		Calendar calendar2 = Calendar.getInstance();
//		calendar1.setTime( DateBoundary.simpleDateFormat.parse( outOfBoundaryDate ) );
//		calendar2.setTime( DateBoundary.simpleDateFormat.parse( dateBoundary.minExclusive ) );
//		assertTrue( calendar1.before( calendar2 ) || calendar1.equals( calendar2 ) );
//	}
//
////	@Test
//	public void testOutOfBoundaryMaxExclusive() throws ParseException
//	{
//		String outOfBoundaryDate = dateBoundary.outOfBoundary( Boundary.MAX_EXCLISIVE );
//		Calendar calendar1 = Calendar.getInstance();
//		Calendar calendar2 = Calendar.getInstance();
//		calendar1.setTime( DateBoundary.simpleDateFormat.parse( outOfBoundaryDate ) );
//		calendar2.setTime( DateBoundary.simpleDateFormat.parse( dateBoundary.maxExclusive ) );
//		assertTrue( calendar1.after( calendar2 ) || calendar1.equals( calendar2 ) );
//	}
//
////	@Test
//	public void testOutOfBoundaryMinInclusive() throws ParseException
//	{
//		String outOfBoundaryDate = dateBoundary.outOfBoundary( Boundary.MIN_INCLISIVE );
//		Calendar calendar1 = Calendar.getInstance();
//		Calendar calendar2 = Calendar.getInstance();
//		calendar1.setTime( DateBoundary.simpleDateFormat.parse( outOfBoundaryDate ) );
//		calendar2.setTime( DateBoundary.simpleDateFormat.parse( dateBoundary.minInclusive ) );
//		assertTrue( calendar1.before( calendar2 ) );
//	}
//
////	@Test
//	public void testOutOfBoundaryMaxInclusive() throws ParseException
//	{
//		String outOfBoundaryDate = dateBoundary.outOfBoundary( Boundary.MAX_INCLISIVE );
//		Calendar calendar1 = Calendar.getInstance();
//		Calendar calendar2 = Calendar.getInstance();
//		calendar1.setTime( DateBoundary.simpleDateFormat.parse( outOfBoundaryDate ) );
//		calendar2.setTime( DateBoundary.simpleDateFormat.parse( dateBoundary.maxInclusive ) );
//		assertTrue( calendar1.after( calendar2 ) );
//	}

}
