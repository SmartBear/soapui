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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

public class ToolsTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( ToolsTestCase.class );
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
}
