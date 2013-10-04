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
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

public class PathUtilsTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( PathUtilsTestCase.class );
	}

	@Test
	public void shouldRelativize() throws Exception
	{
		assertEquals( "c:\\test\\file.txt", PathUtils.relativize( "c:\\test\\file.txt", "d:\\" ) );
		assertEquals( "c:/test/file.txt", PathUtils.relativize( "c:/test/file.txt", "d:\\" ) );
		assertEquals( "c:\\test\\file.txt", PathUtils.relativize( "c:\\test\\file.txt", "d:/" ) );
	}
}
