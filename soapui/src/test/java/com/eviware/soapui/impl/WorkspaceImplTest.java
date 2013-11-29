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

package com.eviware.soapui.impl;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class WorkspaceImplTest
{
	private static final String OUTPUT_FOLDER_PATH = WorkspaceImpl.class.getResource( "/" ).getPath();
	private static final String TEST_WORKSPACE_FILE_PATH = OUTPUT_FOLDER_PATH + "test-workspace.xml";
	private static final String TEST_PROJECT_FILE_PATH = OUTPUT_FOLDER_PATH + "test-project.xml";

	@Before
	public void setUp() throws Exception
	{
		File file = new File( TEST_WORKSPACE_FILE_PATH );
		if( file.exists() )
			file.delete();

		file = new File( TEST_PROJECT_FILE_PATH );
		if( file.exists() )
			file.delete();
	}

	@Test
	public void testProjectRoot() throws Exception
	{
		File wsFile = new File( TEST_WORKSPACE_FILE_PATH );
		WorkspaceImpl ws = new WorkspaceImpl( wsFile.getAbsolutePath(), null );

		WsdlProject project = ws.createProject( "Test Project", null );
		project.saveAs( new File( TEST_PROJECT_FILE_PATH ).getAbsolutePath() );

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
