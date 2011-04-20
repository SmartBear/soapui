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

package com.eviware.soapui.impl.wsdl;

import junit.framework.TestCase;

import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.support.Tools;

public class ResolveContextTest extends TestCase
{
	public void testRelativizePath()
	{
		testFilePath( "test.txt", "c:\\dir\\test.txt", "c:\\dir" );
		testFilePath( "dir2\\test.txt", "c:\\dir\\dir2\\test.txt", "c:\\dir" );
		testFilePath( "..\\test.txt", "c:\\dir\\dir2\\test.txt", "c:\\dir\\dir2\\dir3" );
		testFilePath( "dir\\test.txt", "c:\\dir\\test.txt", "c:\\" );
		testFilePath( "..\\test.txt", "c:\\dir\\test.txt", "c:\\dir\\anotherDir" );
		testFilePath( "..\\dir2\\test.txt", "c:\\dir\\dir2\\test.txt", "c:\\dir\\anotherDir" );

		testUrl( "test.txt", "http://www.test.com/dir/test.txt", "http://www.test.com/dir" );
		testUrl( "dir2/test.txt", "http://www.test.com/dir/dir2/test.txt", "http://www.test.com/dir" );
		testUrl( "../test.txt?test", "http://www.test.com/dir/dir2/test.txt?test", "http://www.test.com/dir/dir2/dir3" );
	}

	private void testFilePath( String relativePath, String absolutePath, String rootPath )
	{
		assertEquals( relativePath, PathUtils.relativize( absolutePath, rootPath ) );

		if( !rootPath.endsWith( "\\" ) )
			rootPath += "\\";

		assertEquals( absolutePath, Tools.joinRelativeUrl( rootPath, relativePath ) );
	}

	private void testUrl( String relativePath, String absolutePath, String rootPath )
	{
		assertEquals( relativePath, PathUtils.relativize( absolutePath, rootPath ) );

		if( !rootPath.endsWith( "/" ) )
			rootPath += "/";

		assertEquals( absolutePath, Tools.joinRelativeUrl( rootPath, relativePath ) );
	}
}
