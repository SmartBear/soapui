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

package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.SaveStatus;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

import java.io.IOException;

/**
 * Removes a WsdlProject from the workspace
 *
 * @author Ole.Matzura
 */

public class RemoveProjectAction extends AbstractSoapUIAction<WsdlProject>
{
	public static final String SOAPUI_ACTION_ID = "RemoveProjectAction";

	public RemoveProjectAction()
	{
		super( "Remove", "Removes this project from the workspace" );
	}

	public void perform( WsdlProject project, Object param )
	{
		if( hasRunningTests( project ) )
		{
			UISupport.showErrorMessage( "Cannot remove Interface due to running tests" );
			return;
		}

		Boolean saveProject = Boolean.FALSE;

		if( project.isOpen() )
		{
			saveProject = UISupport.confirmOrCancel( "Save project [" + project.getName() + "] before removing?",
					"Remove Project" );
			if( saveProject == null )
				return;
		}
		else
		{
			if( !UISupport.confirm( "Remove project [" + project.getName() + "] from workspace", "Remove Project" ) )
				return;
		}

		if( saveProject )
		{
			try
			{
				SaveStatus status = project.save();
				if( status == SaveStatus.CANCELLED || status == SaveStatus.FAILED )
				{
					return;
				}
			}
			catch( IOException e1 )
			{
				UISupport.showErrorMessage( e1 );
			}
		}
		project.getWorkspace().removeProject( project );

	}

	private boolean hasRunningTests( WsdlProject project )
	{
		for( int c = 0; c < project.getTestSuiteCount(); c++ )
		{
			TestSuite testSuite = project.getTestSuiteAt( c );
			for( int i = 0; i < testSuite.getTestCaseCount(); i++ )
			{
				if( SoapUI.getTestMonitor().hasRunningTest( testSuite.getTestCaseAt( i ) ) )
				{
					return true;
				}
			}
		}

		for( MockService mockService : project.getMockServiceList() )
		{
			if( SoapUI.getTestMonitor().hasRunningMock( mockService ) )
			{
				return true;
			}
		}

		return false;
	}
}
