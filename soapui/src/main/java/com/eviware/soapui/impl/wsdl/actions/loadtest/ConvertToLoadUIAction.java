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

package com.eviware.soapui.impl.wsdl.actions.loadtest;

import java.io.IOException;
import java.util.HashMap;

import com.eviware.soapui.impl.wsdl.actions.project.StartLoadUI;
import com.eviware.soapui.impl.wsdl.loadtest.WsdlLoadTest;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
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

public class ConvertToLoadUIAction extends AbstractSoapUIAction<WsdlLoadTest>
{

	private XFormDialog dialog;
	public static final String SOAPUI_ACTION_ID = "ConvertToLoadUIAction";

	public ConvertToLoadUIAction()
	{
		super( "Convert to loadUI TestCase", "Convert this LoadTest to a loadUI TestCase" );
	}

	public void perform( WsdlLoadTest loadTest, Object param )
	{
		if( IntegrationUtils.forceSaveProject( loadTest.getTestCase().getTestSuite().getProject() ) )
		{
			if( !StartLoadUI.testCajoConnection() )
			{
				if( UISupport.confirm( StartLoadUI.LOADUI_LAUNCH_QUESTION, StartLoadUI.LOADUI_LAUNCH_TITLE ) )
				{
					StartLoadUI.launchLoadUI();
				}
				return;
			}

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
			if( dialog.show() )
			{
				String targetProjectString = dialog.getValue( Form.PROJECT );
				String targetTestCaseName = !dialog.getValue( Form.TESTCASE ).equals(
						IntegrationUtils.CREATE_ON_PROJECT_LEVEL ) ? dialog.getValue( Form.TESTCASE ) : null;
				String targetRunnerName = dialog.getValue( Form.SOAPUIRUNNER );
				String openedProjectName = IntegrationUtils.getOpenedProjectName();
				if( !StringUtils.isNullOrEmpty( openedProjectName ) && !targetProjectString.equals( openedProjectName )
						&& IntegrationUtils.checkOpenedLoadUIProjectForClose() )
				{
					return;
				}
				exportToLoadUI( loadTest, targetProjectString, targetTestCaseName, targetRunnerName );
			}
		}
	}

	protected void exportToLoadUI( WsdlLoadTest loadTest, String targetProjectString, String targetTestCaseName,
			String targetRunnerName )
	{

		HashMap<String, Object> createdRunnerSettings = null;
		try
		{
			createdRunnerSettings = IntegrationUtils.exportLoadTestToLoadUI( loadTest, targetProjectString,
					targetTestCaseName, targetRunnerName );
		}
		catch( IOException e )
		{
			UISupport.showInfoMessage( "Error while opening selected loadUI project" );
			return;
		}
		// if( createdRunnerSettings != null )
		// {
		// String creationInfo = "SoapUIRunner created under project: '"
		// + createdRunnerSettings.get( ContextMapping.LOADUI_PROJECT_NAME ) +
		// "'";
		// if( !targetTestCaseName.equals(
		// IntegrationUtils.CREATE_ON_PROJECT_LEVEL ) )
		// {
		// creationInfo += ", TestCase: '" + createdRunnerSettings.get(
		// ContextMapping.LOADUI_TEST_CASE_NAME ) + "'";
		// }
		// UISupport.showInfoMessage( creationInfo );
		// }
	}

	@AForm( description = "Specify Items in loadUI for Converting LoadTest", name = "Convert to loadUI", helpUrl = HelpUrls.CLONETESTCASE_HELP_URL, icon = UISupport.CONVERT_TO_LOADUI_ICON_PATH )
	protected interface Form
	{
		@AField( name = "Target Project", description = "The target Project in loadUI", type = AFieldType.ENUMERATION )
		public final static String PROJECT = "Target Project";

		@AField( name = "Target TestCase", description = "The name of the target TestCase in loadUI", type = AFieldType.ENUMERATION )
		public final static String TESTCASE = "Target TestCase";

		@AField( name = "Target SoapUIRunner", description = "The target SoapUIRunner in loadUI", type = AFieldType.ENUMERATION )
		public final static String SOAPUIRUNNER = "Target SoapUIRunner";

	}

}
