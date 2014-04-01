/*
 * Copyright 2004-2014 SmartBear Software
 *
 * Licensed under the EUPL, Version 1.1 or - as soon as they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
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
