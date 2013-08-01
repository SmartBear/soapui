/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import java.io.File;
import java.io.IOException;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

public class WorkspaceImplTestCase extends TestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( WorkspaceImplTestCase.class );
	}

	public File getOuputDirectory() {
	    return new File("target/test-ouput/" + getClass().getName());
	}
	
	@Override
    @Before
	protected void setUp() throws IOException
	{
	    File ouputDirectory = getOuputDirectory();
        FileUtils.deleteDirectory(ouputDirectory);
        FileUtils.forceMkdir(ouputDirectory);
	}

	@Test
	public void testProjectRoot() throws Exception
	{
		File wsFile = new File( getOuputDirectory(), "test-workspace.xml" );
		WorkspaceImpl ws = new WorkspaceImpl( wsFile.getAbsolutePath(), null );

		WsdlProject project = ws.createProject( "Test Project", null );
		project.saveAs( new File( getOuputDirectory(), "test-project.xml" ).getAbsolutePath() );

		ws.save( false );
		ws.switchWorkspace( wsFile );
		assertEquals( 1, ws.getProjectCount() );
		assertEquals( "Test Project", ws.getProjectAt( 0 ).getName() );

		ws.setProjectRoot( "${workspaceDir}" );

		ws.save( false );

		ws.switchWorkspace( wsFile );
		assertEquals( "${workspaceDir}", ws.getProjectRoot() );
		assertEquals( 1, ws.getProjectCount() );
		assertEquals( "Test Project", ws.getProjectAt( 0 ).getName() );
	}
}
