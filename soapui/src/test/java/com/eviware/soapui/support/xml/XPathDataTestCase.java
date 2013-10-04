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

package com.eviware.soapui.support.xml;

import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

public class XPathDataTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( XPathDataTestCase.class );
	}

	@Test
	public void test1() throws Exception
	{
		XPathData data = new XPathData( "//in/name", false );
		assertEquals( "//in/name", data.getFullPath() );
	}

	@Test
	public void testText() throws Exception
	{
		XPathData data = new XPathData( "//in/name/text()", false );
		assertEquals( "//in/name/text()", data.getFullPath() );
	}

	@Test
	public void testCount() throws Exception
	{
		XPathData data = new XPathData( "count(//in/name)", false );
		assertEquals( "count(//in/name)", data.getFullPath() );
		assertEquals( "count", data.getFunction() );
	}

	@Test
	public void testCountWithNamespace() throws Exception
	{
		String namespace = "declare namespace tes='http://www.example.org/TestService/';\n";
		XPathData data = new XPathData( namespace + "count(//in/name)", false );
		assertEquals( namespace + "count(//in/name)", data.getFullPath() );
		assertEquals( "count", data.getFunction() );
	}

	@Test
	public void testStripXPath() throws Exception
	{
		assertEquals( "//abc", checkStripXPath( "//abc" ) );
		assertEquals( "//abc", checkStripXPath( "//abc[1]" ) );
		assertEquals( "//abc", checkStripXPath( "//abc[a > 3]" ) );
		assertEquals( "//abc", checkStripXPath( "//abc/text()" ) );
		assertEquals( "//abc", checkStripXPath( "count(//abc)" ) );
		assertEquals( "//abc", checkStripXPath( "count( //abc)" ) );
		assertEquals( "//abc", checkStripXPath( "exists(//abc)" ) );
		assertEquals( "//abc", checkStripXPath( "exists( //abc)" ) );

		String ns = "declare namespace ns1='http://abc.com';\n";
		assertEquals( ns + "//abc", checkStripXPath( ns + "//abc[1]" ) );
		assertEquals( ns + "//abc", checkStripXPath( ns + "//abc/text()" ) );
		assertEquals( ns + "//abc", checkStripXPath( ns + "exists(//abc)" ) );
	}

	private String checkStripXPath( String org )
	{
		XPathData xpath = new XPathData( org, true );
		xpath.strip();
		return xpath.getXPath();
	}

	@Test
	public void testReplaceNameInPathOrQuery() throws Exception
	{
		String exp = "//test:test/bil[@name='test ']/@test > 0 and count(//test[bil/text()='test'] = 5";

		assertEquals( "//test:test/bila[@name='test ']/@test > 0 and count(//test[bila/text()='test'] = 5",
				XmlUtils.replaceNameInPathOrQuery( exp, "bil", "bila" ) );
	}
}
