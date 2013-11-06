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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ToolsTestCase
{
	private String originalOSName;

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( ToolsTestCase.class );
	}

	@Before
	public void setUpSystemProperties()
	{
		//Sets the OS name system property to Mac for testing purposes.
		originalOSName = System.getProperty( "os.name" );
		System.setProperty( "os.name", "Mac OS X" );
	}

	@Test
	public void shouldTokenizeArgs() throws Exception
	{
		assertNull( Tools.tokenizeArgs( "" ) );

		String[] args = Tools.tokenizeArgs( "test ett" );
		assertEquals( args.length, 2 );

		args = Tools.tokenizeArgs( "\"test ett\"" );
		assertEquals( args.length, 1 );
		assertEquals( args[0], "test ett" );

		args = Tools.tokenizeArgs( "\"test\\\" ett\" " );
		assertEquals( args.length, 1 );
		assertEquals( args[0], "test\" ett" );
	}

	@Test
	public void testIsMac()
	{
		assertTrue( Tools.isMac() );
	}

	@After
	public void resetSystemProperties()
	{
		System.setProperty( "os.name", originalOSName );
	}
}
