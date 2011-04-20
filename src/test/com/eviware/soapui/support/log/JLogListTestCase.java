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

package com.eviware.soapui.support.log;

import junit.framework.TestCase;

public class JLogListTestCase extends TestCase
{
	public void testMemory() throws Exception
	{
		JLogList list = new JLogList( "test" );

		// for( long c = 0; c < 100000000; c++ )
		// {
		// list.addLine("testing");
		// Thread.sleep( 2 );
		//
		// if( c % 1000 == 0 )
		// System.out.println( c );
		// }
	}
}
