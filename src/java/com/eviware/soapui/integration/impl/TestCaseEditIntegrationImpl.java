/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.integration.impl;

import java.util.List;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.integration.TestCaseEditIntegration;
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
	}

	public void printLog( String log )
	{
		SoapUI.log( log );
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

			showTestCase( projectFilePath, workspace, testCase, testSuite );
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

	private void showTestCase( String projectFile, Workspace workspace, TestCase testCase, TestSuite testSuite )
			throws SoapUIException
	{
		if( testCase != null )
		{
			UISupport.selectAndShow( testCase );
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
