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

import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Calendar;

import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;

public class DateBoundaryTest
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( DateBoundaryTest.class );
	}

	DateTimeBoundary dateBoundary;
	String today;

	@Before
	public void setUp() throws Exception
	{
		today = DateTimeBoundary.simpleDateFormat.get().format( Calendar.getInstance().getTime() );
		dateBoundary = new DateTimeBoundary();
	}

	@Test
	public void testOutOfBoundaryMinExclusive() throws ParseException
	{
		String outOfBoundaryDate = dateBoundary.outOfBoundary( Boundary.MIN_EXCLISIVE, today );
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTime( DateTimeBoundary.simpleDateFormat.get().parse( outOfBoundaryDate ) );
		calendar2.setTime( DateTimeBoundary.simpleDateFormat.get().parse( today ) );
		assertTrue( calendar1.before( calendar2 ) || calendar1.equals( calendar2 ) );
	}

	@Test
	public void testOutOfBoundaryMaxExclusive() throws ParseException
	{
		String outOfBoundaryDate = dateBoundary.outOfBoundary( Boundary.MAX_EXCLISIVE, today );
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTime( DateTimeBoundary.simpleDateFormat.get().parse( outOfBoundaryDate ) );
		calendar2.setTime( DateTimeBoundary.simpleDateFormat.get().parse( today ) );
		assertTrue( calendar1.after( calendar2 ) || calendar1.equals( calendar2 ) );
	}

	@Test
	public void testOutOfBoundaryMinInclusive() throws ParseException
	{
		String outOfBoundaryDate = dateBoundary.outOfBoundary( Boundary.MIN_INCLISIVE, today );
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTime( DateTimeBoundary.simpleDateFormat.get().parse( outOfBoundaryDate ) );
		calendar2.setTime( DateTimeBoundary.simpleDateFormat.get().parse( today ) );
		assertTrue( calendar1.before( calendar2 ) );
	}

	@Test
	public void testOutOfBoundaryMaxInclusive() throws ParseException
	{
		String outOfBoundaryDate = dateBoundary.outOfBoundary( Boundary.MAX_INCLISIVE, today );
		Calendar calendar1 = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar1.setTime( DateTimeBoundary.simpleDateFormat.get().parse( outOfBoundaryDate ) );
		calendar2.setTime( DateTimeBoundary.simpleDateFormat.get().parse( today ) );
		assertTrue( calendar1.after( calendar2 ) );
	}

}
