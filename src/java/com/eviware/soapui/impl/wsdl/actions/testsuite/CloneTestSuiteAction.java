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

package com.eviware.soapui.impl.wsdl.actions.testsuite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

/**
 * Clones a WsdlTestSuite
 * 
 * @author Ole.Matzura
 */

public class CloneTestSuiteAction extends AbstractSoapUIAction<WsdlTestSuite>
{
	private XFormDialog dialog;

	public CloneTestSuiteAction()
	{
		super( "Clone TestSuite", "Clones this TestSuite" );
	}

	public void perform( WsdlTestSuite testSuite, Object param )
	{
		if( dialog == null )
			dialog = ADialogBuilder.buildDialog( Form.class );

		dialog.getFormField( Form.CLONE_DESCRIPTION ).addFormFieldListener( new XFormFieldListener()
		{

			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				if( dialog.getBooleanValue( Form.CLONE_DESCRIPTION ) )
				{
					dialog.getFormField( Form.DESCRIPTION ).setEnabled( false );
				}
				else
				{
					dialog.getFormField( Form.DESCRIPTION ).setEnabled( true );
				}

			}
		} );
		dialog.setValue( Form.NAME, "Copy of " + testSuite.getName() );
		dialog.setBooleanValue( Form.MOVE, false );
		dialog.setBooleanValue( Form.CLONE_DESCRIPTION, true );
		dialog.getFormField( Form.DESCRIPTION ).setEnabled( false );
		dialog.setValue( Form.DESCRIPTION, testSuite.getDescription() );
		WorkspaceImpl workspace = testSuite.getProject().getWorkspace();
		dialog.setOptions( Form.PROJECT, ModelSupport.getNames( workspace.getOpenProjectList(),
				new String[] { "<Create New>" } ) );

		dialog.setValue( Form.PROJECT, testSuite.getProject().getName() );

		if( dialog.show() )
		{
			String targetProjectName = dialog.getValue( Form.PROJECT );
			String name = dialog.getValue( Form.NAME );

			WsdlProject project = ( WsdlProject )testSuite.getProject();

			// within same project?
			boolean move = dialog.getBooleanValue( Form.MOVE );
			boolean cloneDescription = dialog.getBooleanValue( Form.CLONE_DESCRIPTION );
			String description = testSuite.getDescription();
			if( !cloneDescription )
			{
				description = dialog.getValue( Form.DESCRIPTION );
			}
			if( targetProjectName.equals( testSuite.getProject().getName() ) )
			{
				cloneTestSuiteWithinProject( testSuite, name, project, description );
			}
			else
			{
				cloneToAnotherProject( testSuite, targetProjectName, name, move, description );
			}

			if( move )
			{
				testSuite.getProject().removeTestSuite( testSuite );
			}
		}
	}

	public static WsdlTestSuite cloneToAnotherProject( WsdlTestSuite testSuite, String targetProjectName, String name,
			boolean move, String description )
	{
		WorkspaceImpl workspace = testSuite.getProject().getWorkspace();
		WsdlProject targetProject = ( WsdlProject )workspace.getProjectByName( targetProjectName );
		if( targetProject == null )
		{
			targetProjectName = UISupport.prompt( "Enter name for new Project", "Clone TestSuite", "" );
			if( targetProjectName == null )
				return null;

			try
			{
				targetProject = workspace.createProject( targetProjectName, null );
			}
			catch( SoapUIException e )
			{
				UISupport.showErrorMessage( e );
			}

			if( targetProject == null )
				return null;
		}

		Set<Interface> requiredInterfaces = getRequiredInterfaces( testSuite, targetProject );

		if( requiredInterfaces.size() > 0 )
		{
			String msg = "Target project [" + targetProjectName + "] is missing required Interfaces;\r\n\r\n";
			for( Interface iface : requiredInterfaces )
			{
				msg += iface.getName() + " [" + iface.getTechnicalId() + "]\r\n";
			}
			msg += "\r\nThese will be cloned to the targetProject as well";

			if( !UISupport.confirm( msg, "Clone TestSuite" ) )
				return null;

			for( Interface iface : requiredInterfaces )
			{
				targetProject.importInterface( ( AbstractInterface<?> )iface, true, true );
			}
		}

		testSuite = targetProject.importTestSuite( testSuite, name, -1, !move, description );
		UISupport.select( testSuite );

		return testSuite;
	}

	public static boolean cloneTestSuiteWithinProject( WsdlTestSuite testSuite, String name, WsdlProject project,
			String description )
	{
		WsdlTestSuite newTestSuite = project.importTestSuite( testSuite, name, -1, true, description );
		UISupport.select( newTestSuite );
		return true;
	}

	public static Set<Interface> getRequiredInterfaces( WsdlTestSuite testSuite, WsdlProject targetProject )
	{
		Set<Interface> requiredInterfaces = new HashSet<Interface>();

		for( int i = 0; i < testSuite.getTestCaseCount(); i++ )
		{
			WsdlTestCase testCase = testSuite.getTestCaseAt( i );

			for( int y = 0; y < testCase.getTestStepCount(); y++ )
			{
				WsdlTestStep testStep = testCase.getTestStepAt( y );
				requiredInterfaces.addAll( testStep.getRequiredInterfaces() );
			}
		}

		if( requiredInterfaces.size() > 0 && targetProject.getInterfaceCount() > 0 )
		{
			Map<String, Interface> bindings = new HashMap<String, Interface>();
			for( Interface iface : requiredInterfaces )
			{
				bindings.put( iface.getTechnicalId(), iface );
			}

			for( Interface iface : targetProject.getInterfaceList() )
			{
				bindings.remove( iface.getTechnicalId() );
			}

			requiredInterfaces.retainAll( bindings.values() );
		}
		return requiredInterfaces;
	}

	@AForm( description = "Specify target Project and name of cloned TestSuite", name = "Clone TestSuite", helpUrl = HelpUrls.CLONETESTSUITE_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	protected interface Form
	{
		@AField( name = "TestSuite Name", description = "The name of the cloned TestSuite", type = AFieldType.STRING )
		public final static String NAME = "TestSuite Name";

		@AField( name = "Target Project", description = "The target Project for the cloned TestSuite", type = AFieldType.ENUMERATION )
		public final static String PROJECT = "Target Project";

		@AField( name = "Move instead", description = "Moves the selected TestSuite instead of copying", type = AFieldType.BOOLEAN )
		public final static String MOVE = "Move instead";

		@AField( name = "Clone description", description = "Clones the description of selected TestSuite", type = AFieldType.BOOLEAN )
		public final static String CLONE_DESCRIPTION = "Clone description";

		@AField( name = "Description", description = "Description of new TestSuite", type = AFieldType.STRINGAREA )
		public final static String DESCRIPTION = "Description";
	}
}
