package com.eviware.soapui.impl.wsdl.support;

import junit.framework.TestCase;

public class PathUtilsTestCase extends TestCase
{
	public void testRelativize() throws Exception
	{
		assertEquals( "c:\\test\\file.txt", PathUtils.relativize( "c:\\test\\file.txt", "d:\\" ));
		assertEquals( "c:/test/file.txt", PathUtils.relativize( "c:/test/file.txt", "d:\\" ));
		assertEquals( "c:\\test\\file.txt", PathUtils.relativize( "c:\\test\\file.txt", "d:/" ));
	}
}
