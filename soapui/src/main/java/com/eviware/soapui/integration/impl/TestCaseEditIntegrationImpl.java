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

package com.eviware.soapui.integration.impl;

import java.awt.Frame;
import java.io.File;
import java.util.List;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.integration.TestCaseEditIntegration;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;

public class TestCaseEditIntegrationImpl implements TestCaseEditIntegration
{

	public void editTestCase( String project, String testSuite, String testCase )
	{
		// TODO Auto-generated method stub
	}

	public void test()
	{
		CajoClient.getInstance().setLoadUIPath();
	}

	public String getSoapUIPath()
	{
		String os = System.getProperty( "os.name" );
		if( os == null )
			return null;

		String ext;
		if( os.indexOf( "Windows" ) >= 0 )
			ext = "bat";
		else if( os.indexOf( "Mac OS X" ) >= 0 )
			ext = "command";
		else
			ext = "sh";

		String pro = "";
		Frame mainFrame = UISupport.getMainFrame();
		if( mainFrame != null && mainFrame.getTitle().toLowerCase().indexOf( "pro" ) > -1 )
		{
			pro = "-pro";
		}

		String path = System.getProperty( "soapui.home" );
		if( path == null )
			return null;

		File pathFile = new File( path );
		path = pathFile.getAbsolutePath();
		path += File.separator + "bin" + File.separator + "soapui" + pro + "." + ext;

		File f = new File( path );
		if( f.exists() )
			return f.getAbsolutePath();
		else
			return null;
	}

	public void printLog( String log )
	{
		SoapUI.log( log );
	}

	public void openProject( String[] parameters )
	{
		if( parameters != null && parameters.length == 1 )
		{
			String projectFilePath = parameters[0];

			try
			{
				Workspace workspace = SoapUI.getWorkspace();
				Project project = findProject( projectFilePath, workspace );

				project = openProject( projectFilePath, workspace, project );

				showModelItem( project );
				bringToFront();
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}
	}

	public void openTestCase( String[] parameters )
	{
		if( !isValid( parameters ) )
			return;

		String projectFilePath = parameters[0];
		String testSuiteName = parameters[1];
		String testCaseName = parameters[2];

		try
		{
			Workspace workspace = SoapUI.getWorkspace();
			Project project = findProject( projectFilePath, workspace );

			project = openProject( projectFilePath, workspace, project );

			TestSuite testSuite = project.getTestSuiteByName( testSuiteName );
			TestCase testCase = testSuite.getTestCaseByName( testCaseName );

			showModelItem( testCase );
			bringToFront();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	public void bringToFront()
	{
		UISupport.getMainFrame().setVisible( true );
		UISupport.getMainFrame().setAlwaysOnTop( true );
		UISupport.getMainFrame().setAlwaysOnTop( false );
	}

	private Project openProject( String projectFilePath, Workspace workspace, Project project ) throws SoapUIException
	{
		if( project != null )
		{
			if( !project.isOpen() )
				project = workspace.openProject( project );
		}
		else
		{
			project = workspace.importProject( projectFilePath );
		}
		if( project == null )
		{
			throw new SoapUIException( "Cannot open project on path: " + projectFilePath );
		}
		return project;
	}

	private void showModelItem( ModelItem modelItem ) throws SoapUIException
	{
		if( modelItem != null )
		{
			UISupport.selectAndShow( modelItem );
		}
	}

	private Project findProject( String projectFile, Workspace workspace )
	{
		Project project = null;
		List<? extends Project> projectList = workspace.getProjectList();
		for( Project proj : projectList )
		{
			if( proj.getPath().equalsIgnoreCase( projectFile ) )
			{
				project = workspace.getProjectByName( proj.getName() );
				break;
			}
		}
		return project;
	}

	private boolean isValid( String[] parameters )
	{
		if( parameters != null && parameters.length == 3 )
		{
			for( String parameter : parameters )
			{
				if( StringUtils.isNullOrEmpty( parameter ) )
					return false;
			}
			return true;
		}
		else
			return false;
	}

}
