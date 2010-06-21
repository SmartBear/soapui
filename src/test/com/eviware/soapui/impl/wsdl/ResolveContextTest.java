package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.impl.wsdl.support.PathUtils;
import com.eviware.soapui.support.Tools;

import junit.framework.TestCase;

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
		assertEquals( relativePath, PathUtils.relativize( absolutePath, rootPath ));
		
		if( !rootPath.endsWith( "\\" ))
			rootPath += "\\";
		
		assertEquals( absolutePath, Tools.joinRelativeUrl( rootPath, relativePath ));
	}
	
	private void testUrl( String relativePath, String absolutePath, String rootPath )
	{
		assertEquals( relativePath, PathUtils.relativize( absolutePath, rootPath ));
		
		if( !rootPath.endsWith( "/" ))
			rootPath += "/";
		
		assertEquals( absolutePath, Tools.joinRelativeUrl( rootPath , relativePath ));
	}
}
