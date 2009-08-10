/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */


package com.eviware.soapui.impl.wsdl.support.http;

import junit.framework.TestCase;

public class ProxyUtilsTestCase extends TestCase
{
	public void testExcludes()
	{
		assertFalse( ProxyUtils.excludes( new String[] { ""} , "www.test.com", 8080 ));
		assertTrue( ProxyUtils.excludes( new String[] { "test.com"} , "www.test.com", 8080 ));
		assertFalse( ProxyUtils.excludes( new String[] { "test2.com"} , "www.test.com", 8080));
		assertTrue( ProxyUtils.excludes( new String[] { "test.com:8080"} , "www.test.com", 8080 ));
		assertFalse( ProxyUtils.excludes( new String[] { "test2.com:8080"} , "www.test.com", 8080 ));
		assertFalse( ProxyUtils.excludes( new String[] { "test.com:8081"} , "www.test.com", 8080 ));
		assertTrue( ProxyUtils.excludes( new String[] { "test.com:8080","test.com:8081"} , "www.test.com", 8080 ));
		assertTrue( ProxyUtils.excludes( new String[] { "test.com:8080","test.com"} , "www.test.com", 8080 ));
	}
}
