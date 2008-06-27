package com.eviware.soapui.impl.wsdl.support.wsdl;

import java.io.File;

import com.eviware.soapui.config.DefinitionCacheConfig;

import junit.framework.TestCase;

public class WsdlUtilsTestCase extends TestCase
{
	public void testCacheWsdl() throws Exception
	{
		File file = new File( "src" + File.separator + "test-resources" +
							File.separator + "test6" + File.separator + "TestService.wsdl");
		
		assertTrue( file.exists() );
		
		WsdlLoader loader = new UrlWsdlLoader( file.toURI().toURL().toString() );
		
		DefinitionCacheConfig cachedWsdl = WsdlUtils.cacheWsdl( loader );
		assertEquals( 4, cachedWsdl.sizeOfPartArray() );
		
	}
}
