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

package com.eviware.soapui.impl.wsdl.actions.testcase;

import java.io.IOException;
import java.util.HashMap;

import com.eviware.soapui.impl.wsdl.actions.project.StartLoadUI;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.integration.loadui.IntegrationUtils;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

public class RunTestCaseWithLoadUIAction extends AbstractSoapUIAction<WsdlTestCase>
{
	private static final String EMPTY_OPTION = "-";
	private XFormDialog dialog;
	public static final String SOAPUI_ACTION_ID = "RunTestCaseWithLoadUIAction";

	public RunTestCaseWithLoadUIAction()
	{
		super( "Run with loadUI", "Run this TestCase with loadUI" );
	}

	public void perform( WsdlTestCase testCase, Object param )
	{
		if( IntegrationUtils.forceSaveProject( testCase.getTestSuite().getProject() ) )
		{

			if( !StartLoadUI.testCajoConnection() )
			{
				if( UISupport.confirm( StartLoadUI.LOADUI_LAUNCH_QUESTION, StartLoadUI.LOADUI_LAUNCH_TITLE ) )
				{
					StartLoadUI.launchLoadUI();
				}
				return;
			}

			final String soapUITestCase = testCase.getName();
			final String soapUITestSuite = testCase.getTestSuite().getName();
			final String soapUIProjectPath = testCase.getTestSuite().getProject().getPath();
			String generatorType = EMPTY_OPTION;
			String analisysType = EMPTY_OPTION;
			if( dialog == null )
				dialog = ADialogBuilder.buildDialog( Form.class );

			dialog.getFormField( Form.PROJECT ).addFormFieldListener( new XFormFieldListener()
			{

				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
				{
					dialog.setOptions( Form.TESTCASE, IntegrationUtils.getAvailableTestCases( newValue ) );
					if( dialog.getValue( Form.TESTCASE ).equals( IntegrationUtils.CREATE_ON_PROJECT_LEVEL ) )
					{
						dialog.setOptions( Form.SOAPUIRUNNER,
								IntegrationUtils.getAvailableRunners( newValue, IntegrationUtils.CREATE_ON_PROJECT_LEVEL ) );
					}
				}
			} );
			dialog.getFormField( Form.TESTCASE ).addFormFieldListener( new XFormFieldListener()
			{

				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
				{
					if( newValue.equals( IntegrationUtils.CREATE_NEW_OPTION ) )
					{
						dialog.setOptions( Form.SOAPUIRUNNER, new String[] { IntegrationUtils.CREATE_NEW_OPTION } );
					}
					else
					{
						dialog.setOptions( Form.SOAPUIRUNNER,
								IntegrationUtils.getAvailableRunners( dialog.getValue( Form.PROJECT ), newValue ) );
					}
				}
			} );

			dialog.setOptions( Form.PROJECT, IntegrationUtils.getAvailableProjects() );
			if( !StringUtils.isNullOrEmpty( IntegrationUtils.getOpenedProjectName() ) )
			{
				dialog.setValue( Form.PROJECT, IntegrationUtils.getOpenedProjectName() );
			}
			else
			{
				dialog.setValue( Form.PROJECT, IntegrationUtils.CREATE_NEW_OPTION );
			}
			dialog.setOptions( Form.TESTCASE, IntegrationUtils.getAvailableTestCases( dialog.getValue( Form.PROJECT ) ) );
			if( !dialog.getValue( Form.PROJECT ).equals( IntegrationUtils.getOpenedProjectName() ) )
			{
				dialog.setValue( Form.TESTCASE, IntegrationUtils.CREATE_ON_PROJECT_LEVEL );
			}

			dialog.setOptions( Form.SOAPUIRUNNER,
					IntegrationUtils.getAvailableRunners( dialog.getValue( Form.PROJECT ), dialog.getValue( Form.TESTCASE ) ) );
			dialog.setValue( Form.SOAPUIRUNNER, IntegrationUtils.CREATE_NEW_OPTION );

			dialog.setOptions( Form.GENERATOR, new String[] { EMPTY_OPTION, "Fixed Rate", "Variance", "Random", "Ramp",
					"Virtual Users", "Fixed Load" } );
			dialog.setValue( Form.GENERATOR, EMPTY_OPTION );

			dialog.setOptions( Form.STATISTICS, new String[] { EMPTY_OPTION, "Statistics" } );
			dialog.setValue( Form.STATISTICS, EMPTY_OPTION );

			if( dialog.show() )
			{
				String targetProjectString = dialog.getValue( Form.PROJECT );
				String targetTestCaseName = !dialog.getValue( Form.TESTCASE ).equals(
						IntegrationUtils.CREATE_ON_PROJECT_LEVEL ) ? dialog.getValue( Form.TESTCASE ) : null;
				String targetSoapUIRunnerName = dialog.getValue( Form.SOAPUIRUNNER );
				generatorType = dialog.getValue( Form.GENERATOR );
				analisysType = dialog.getValue( Form.STATISTICS );
				if( dialog.getReturnValue() == XFormDialog.OK_OPTION )
				{
					String openedProjectName = IntegrationUtils.getOpenedProjectName();
					if( !StringUtils.isNullOrEmpty( openedProjectName ) && !targetProjectString.equals( openedProjectName )
							&& IntegrationUtils.checkOpenedLoadUIProjectForClose() )
					{
						return;
					}

					HashMap<String, String> createdRunnerSettings = null;
					try
					{
						createdRunnerSettings = IntegrationUtils.createSoapUIRunner( soapUIProjectPath, soapUITestSuite,
								soapUITestCase, targetProjectString, targetTestCaseName, targetSoapUIRunnerName, generatorType,
								analisysType );
					}
					catch( IOException e )
					{
						UISupport.showInfoMessage( "Error while opening selected loadUI project" );
						return;
					}
					// next code was for getting back info on items created on loadUI
					// side - removed for now
					// if( createdRunnerSettings != null )
					// {
					// String creationInfo =
					// "SoapUI Runner created/updated under project: '"
					// + createdRunnerSettings.get(
					// ContextMapping.LOADUI_PROJECT_NAME )
					// + "'";
					// if( targetTestCaseName != null && !targetTestCaseName.equals(
					// IntegrationUtils.CREATE_ON_PROJECT_LEVEL ) )
					// {
					// creationInfo += ", TestCase: '" + createdRunnerSettings.get(
					// ContextMapping.LOADUI_TEST_CASE_NAME )
					// + "'";
					// }
					// creationInfo += "Transfer focus to loadUI?";
					// if( UISupport.confirm( creationInfo,
					// IntegrationUtils.LOADU_INFO_DIALOG_TITLE ) )
					// {
					// IntegrationUtils.bringLoadUIToFront();
					// }
					// }
				}
			}
		}
	}

	@AForm( description = "Specify Items in loadUI for Running TestCase", name = "Run With loadUI", helpUrl = HelpUrls.CLONETESTCASE_HELP_URL, icon = UISupport.LOADUI_ICON_PATH )
	protected interface Form
	{
		@AField( name = "Target Project", description = "The target Project in loadUI", type = AFieldType.ENUMERATION )
		public final static String PROJECT = "Target Project";

		@AField( name = "Target TestCase", description = "The name of the target TestCase in loadUI", type = AFieldType.ENUMERATION )
		public final static String TESTCASE = "Target TestCase";

		@AField( name = "Target SoapUI Runner", description = "The target SoapUI Runner in loadUI", type = AFieldType.ENUMERATION )
		public final static String SOAPUIRUNNER = "Target SoapUI Runner";

		@AField( name = "Default Generator", description = "Choose generator type in loadUI", type = AFieldType.ENUMERATION )
		public final static String GENERATOR = "Default Generator";

		@AField( name = "Default Statistics", description = "Choose Statistics in loadUI", type = AFieldType.ENUMERATION )
		public final static String STATISTICS = "Default Statistics";

	}

}
