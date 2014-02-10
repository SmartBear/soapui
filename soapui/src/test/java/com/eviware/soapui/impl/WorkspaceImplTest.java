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
import com.eviware.soapui.support.SoapUIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class WorkspaceImplTest
{
	private static final String OUTPUT_FOLDER_PATH = WorkspaceImpl.class.getResource( "/" ).getPath();
	private static final String TEST_WORKSPACE_FILE_PATH = OUTPUT_FOLDER_PATH + "test-workspace.xml";
	private static final String TEST_PROJECT_FILE_PATH = OUTPUT_FOLDER_PATH + "test-project.xml";
	private File workspaceFile;
	private File projectFile;
	private WorkspaceImpl workspace;

	@Before
	public void setUp() throws Exception
	{
		workspaceFile = new File( TEST_WORKSPACE_FILE_PATH );
		workspace = new WorkspaceImpl( workspaceFile.getAbsolutePath(), null );

		projectFile = new File( TEST_PROJECT_FILE_PATH );
		WsdlProject project = workspace.createProject( "Test Project", null );
		project.saveAs( projectFile.getAbsolutePath() );

		workspace.save( false );
	}

	@After
	public void tearDown()
	{
		if( workspaceFile.exists() )
		{
			workspaceFile.delete();
		}
		if( projectFile.exists() )
		{
			projectFile.delete();
		}
	}

	@Test
	public void testProjectRoot() throws Exception
	{
		workspace.setProjectRoot( "${workspaceDir}" );
		workspace.save( false );
		workspace.switchWorkspace( workspaceFile );

		assertThat( workspace.getProjectRoot(), is( "${workspaceDir}" ) );
		assertThat( workspace.getProjectCount(), is( 1 ) );
		assertThat( workspace.getProjectAt( 0 ).getName(), is( "Test Project" ) );
	}

	@Test
	public void doesNotRemoveExternallyModifiedProjects() throws SoapUIException
	{
		projectFile.setLastModified( System.currentTimeMillis() );
		workspace.save( false, true );

		workspace.switchWorkspace( workspaceFile );

		assertThat( workspace.getProjectCount(), is( 1 ) );
	}
}
