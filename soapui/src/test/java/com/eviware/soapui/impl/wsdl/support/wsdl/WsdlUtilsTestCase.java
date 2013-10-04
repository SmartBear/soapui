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

package com.eviware.soapui.impl.wsdl.support.wsdl;

import java.io.File;

import junit.framework.TestCase;

import com.eviware.soapui.config.DefinitionCacheConfig;

public class WsdlUtilsTestCase extends TestCase
{
	public void testCacheWsdl() throws Exception
	{
		File file = new File( WsdlUtilsTestCase.class.getResource(  "/test6/TestService.wsdl" ).toURI());

		assertTrue( file.exists() );

		WsdlLoader loader = new UrlWsdlLoader( file.toURI().toURL().toString() );

		DefinitionCacheConfig cachedWsdl = WsdlUtils.cacheWsdl( loader );
		assertEquals( 4, cachedWsdl.sizeOfPartArray() );

	}
}
