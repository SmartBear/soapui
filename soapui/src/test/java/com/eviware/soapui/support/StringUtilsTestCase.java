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

package com.eviware.soapui.support;

import junit.framework.JUnit4TestAdapter;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class StringUtilsTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( StringUtilsTestCase.class );
	}

	@Test
	public void testUnquote() throws Exception
	{
		assertEquals( "test", StringUtils.unquote( "\"test\"" ) );
		assertNull( StringUtils.unquote( null ) );
		assertEquals( "", StringUtils.unquote( "" ) );
		assertEquals( "\"test", StringUtils.unquote( "\"test" ) );
		assertEquals( "test\"", StringUtils.unquote( "test\"" ) );
		assertEquals( "test", StringUtils.unquote( "test" ) );
	}

	@Test
	public void testQuote() throws Exception
	{
		assertNull( StringUtils.quote( null ) );
		assertEquals( "\"\"", StringUtils.quote( "" ) );
		assertEquals( "\"test\"", StringUtils.quote( "test" ) );
		assertEquals( "\"\"test\"", StringUtils.quote( "\"test" ) );
		assertEquals( "\"test\"\"", StringUtils.quote( "test\"" ) );
		assertEquals( "\"\"\"", StringUtils.quote( "\"" ) );
	}

	@Test
	public void testCreateXmlName() throws Exception
	{
		assertEquals( "helloThere", StringUtils.createXmlName( "hello there" ) );
		assertEquals( "helloThere", StringUtils.createXmlName( "hello ?? there" ) );
		assertEquals( "hello_there", StringUtils.createXmlName( "hello_there" ) );
		assertEquals( "helloThere", StringUtils.createXmlName( "hello:there" ) );
		assertEquals( "tb_table.column", StringUtils.createXmlName( "tb_table.column" ) );
	}

	@Test
	public void createsXmlNameForStringStartingWithDigit() throws Exception
	{
		 assertThat(StringUtils.createXmlName( "15" ), is ("_15"));
		 assertThat(StringUtils.createXmlName( "1pt" ), is ("_1pt"));
	}

}
