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
