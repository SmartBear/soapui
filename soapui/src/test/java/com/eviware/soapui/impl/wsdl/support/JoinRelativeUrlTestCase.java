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

package com.eviware.soapui.impl.wsdl.support;

import static org.junit.Assert.assertEquals;

import java.io.File;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.eviware.soapui.support.Tools;

public class JoinRelativeUrlTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( JoinRelativeUrlTestCase.class );
	}

	@Test
	public void testJoin() throws Exception
	{
		assertEquals( "http://test:8080/my/root/test.xsd",
				Tools.joinRelativeUrl( "http://test:8080/my/root/test.wsdl", "test.xsd" ) );
		assertEquals( "http://test:8080/my/root/bu/test.xsd",
				Tools.joinRelativeUrl( "http://test:8080/my/root/test.wsdl", "bu/test.xsd" ) );
		assertEquals( "http://test:8080/my/test.xsd",
				Tools.joinRelativeUrl( "http://test:8080/my/root/test.wsdl", "../test.xsd" ) );
		assertEquals( "http://test:8080/my/root/test.xsd",
				Tools.joinRelativeUrl( "http://test:8080/my/root/test.wsdl", "./test.xsd" ) );
		assertEquals( "http://test:8080/bil/test.xsd",
				Tools.joinRelativeUrl( "http://test:8080/my/root/test.wsdl", "../../bil/test.xsd" ) );
		assertEquals( "http://test:8080/bil/test.xsd",
				Tools.joinRelativeUrl( "http://test:8080/my/root/test.wsdl", "././../../bil/test/.././test.xsd" ) );
		assertEquals( "file:c:" + File.separator + "bil" + File.separator + "xsd" + File.separator + "test.xsd", Tools.joinRelativeUrl( "file:c:\\bil\\test.wsdl", "./xsd/test.xsd" ) );
		assertEquals( "file:c:" + File.separator + "bil" + File.separator + "xsd" + File.separator + "test.xsd",
				Tools.joinRelativeUrl( "file:c:\\bil\\test\\test\\test.wsdl", "..\\..\\xsd\\test.xsd" ) );
	}
}
