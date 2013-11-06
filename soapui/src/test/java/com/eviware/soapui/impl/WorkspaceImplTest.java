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
	@Before
	public void setUp() throws Exception
	{
		File file = new File( "test-workspace.xml" );
		if( file.exists() )
			file.delete();

		file = new File( "test-project.xml" );
		if( file.exists() )
			file.delete();
	}

	@Test
	public void testProjectRoot() throws Exception
	{
		File wsFile = new File( "test-workspace.xml" );
		WorkspaceImpl ws = new WorkspaceImpl( wsFile.getAbsolutePath(), null );

		WsdlProject project = ws.createProject( "Test Project", null );
		project.saveAs( new File( "test-project.xml" ).getAbsolutePath() );

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
