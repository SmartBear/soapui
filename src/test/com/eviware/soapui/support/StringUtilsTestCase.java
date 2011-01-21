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

package com.eviware.soapui.support;

import junit.framework.TestCase;

public class StringUtilsTestCase extends TestCase
{
	public void testUnquote() throws Exception
	{
		assertEquals( "test", StringUtils.unquote( "\"test\"" ));
		assertNull( StringUtils.unquote( null ) );
		assertEquals( "", StringUtils.unquote( "" ) );
		assertEquals( "\"test", StringUtils.unquote( "\"test" ));
		assertEquals( "test\"", StringUtils.unquote( "test\"" ));
		assertEquals( "test", StringUtils.unquote( "test" ));
	}
	
	public void testQuote() throws Exception
	{
		assertNull( StringUtils.quote( null ));
		assertEquals( "\"\"", StringUtils.quote( "" ) );
		assertEquals( "\"test\"", StringUtils.quote( "test" ) );
		assertEquals( "\"\"test\"", StringUtils.quote( "\"test" ) );
		assertEquals( "\"test\"\"", StringUtils.quote( "test\"" ) );
		assertEquals( "\"\"\"", StringUtils.quote( "\"" ) );
	}
	
	public void testReplaceAll() throws Exception
	{
		assertEquals( "<a>\n\n</a>", "<a>\n<test>--remove--</test>\n</a>".replaceAll( "<(.+)>--remove--</(\\1)>", "" ));
	}
}
