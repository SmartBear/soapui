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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ToolsTestCase
{
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
