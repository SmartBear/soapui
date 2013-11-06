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

package com.eviware.soapui.impl.wsdl.actions.iface.tools.support;

import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.eviware.soapui.support.types.StringToStringMap;

public class ArgumentBuilderTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( ArgumentBuilderTestCase.class );
	}

	@Test
	public void testUnix() throws Exception
	{
		ArgumentBuilder builder = new ArgumentBuilder( new StringToStringMap() );
		builder.startScript( "tcpmon", null, ".sh" );

		assertEquals( "sh", builder.getArgs().get( 0 ) );
		assertEquals( "-c", builder.getArgs().get( 1 ) );

		assertEquals( "./tcpmon.sh", builder.getArgs().get( 2 ) );

		builder.addArgs( new String[] { "test" } );
		assertEquals( "./tcpmon.sh test", builder.getArgs().get( 2 ) );

		builder.addArgs( new String[] { "te st" } );
		assertEquals( "./tcpmon.sh test te%20st", builder.getArgs().get( 2 ) );
	}
}
