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

package com.eviware.soapui.impl.wsdl;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.model.workspace.WorkspaceFactory;

public class WorkspaceTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( WorkspaceTestCase.class );
	}

	@Test
	public void testWorkspaceImpl() throws Exception
	{
		Workspace workspace = WorkspaceFactory.getInstance().openWorkspace(
				System.getProperty( "user.home", "." ) + File.separatorChar + SoapUI.DEFAULT_WORKSPACE_FILE, null );

		for( int c = 0; c < workspace.getProjectCount(); c++ )
		{
			assertNotNull( workspace.getProjectAt( c ).getName() );
		}
	}
}
