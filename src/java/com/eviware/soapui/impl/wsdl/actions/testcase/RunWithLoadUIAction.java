/*
 *  soapUI, copyright (C) 2004-2010 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.actions.testcase;

import java.io.IOException;
import java.util.HashMap;

import com.eviware.soapui.impl.wsdl.actions.project.StartLoadUI;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.integration.loadui.ContextMapping;
import com.eviware.soapui.integration.loadui.IntegrationUtils;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

public class RunWithLoadUIAction extends AbstractSoapUIAction<WsdlTestCase>
{
	private XFormDialog dialog;
	public static final String SOAPUI_ACTION_ID = "RunWithLoadUIAction";

	public RunWithLoadUIAction()
	{
		super( "Run with loadUI", "Run this TestCase with loadUI" );
	}

	public void perform( WsdlTestCase testCase, Object param )
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
		if( dialog == null )
			dialog = ADialogBuilder.buildDialog( Form.class );

		dialog.getFormField( Form.PROJECT ).addFormFieldListener( new XFormFieldListener()
		{

			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				dialog.setOptions( Form.TESTCASE, IntegrationUtils.getAvailableTestCases( newValue ) );
				if( dialog.getValue( Form.TESTCASE ).equals( IntegrationUtils.CREATE_ON_PROJECT_LEVEL ) )
				{
					dialog.setOptions( Form.SOAPUISAMPLER, IntegrationUtils.getAvailableSamplers( newValue,
							IntegrationUtils.CREATE_ON_PROJECT_LEVEL ) );
				}
			}
		} );
		dialog.getFormField( Form.TESTCASE ).addFormFieldListener( new XFormFieldListener()
		{

			public void valueChanged( XFormField sourceField, String newValue, String oldValue )
			{
				if( newValue.equals( IntegrationUtils.CREATE_NEW_OPTION ) )
				{
					dialog.setOptions( Form.SOAPUISAMPLER, new String[] { IntegrationUtils.CREATE_NEW_OPTION } );
				}
				else
				{
					dialog.setOptions( Form.SOAPUISAMPLER, IntegrationUtils.getAvailableSamplers( dialog
							.getValue( Form.PROJECT ), newValue ) );
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

		dialog.setOptions( Form.SOAPUISAMPLER, IntegrationUtils.getAvailableSamplers( dialog.getValue( Form.PROJECT ),
				dialog.getValue( Form.TESTCASE ) ) );
		dialog.setValue( Form.SOAPUISAMPLER, IntegrationUtils.CREATE_NEW_OPTION );
		if( dialog.show() )
		{
			String targetProjectString = dialog.getValue( Form.PROJECT );
			String targetTestCaseName = !dialog.getValue( Form.TESTCASE )
					.equals( IntegrationUtils.CREATE_ON_PROJECT_LEVEL ) ? dialog.getValue( Form.TESTCASE ) : null;
			String targetSamplerName = dialog.getValue( Form.SOAPUISAMPLER );
			if( dialog.getReturnValue() == XFormDialog.OK_OPTION )
			{
				String openedProjectName = IntegrationUtils.getOpenedProjectName();
				if( !StringUtils.isNullOrEmpty( openedProjectName ) && !targetProjectString.equals( openedProjectName ) )
					if( UISupport.confirm( "Close currently open [" + IntegrationUtils.getOpenedProjectName()
							+ "] loadUI project", "Close loadUI project" ) )
					{
						IntegrationUtils.closeOpenedLoadUIProject();
					}
					else
					{
						return;
					}

				HashMap<String, String> createdSamplerSettings = null;
				try
				{
					createdSamplerSettings = IntegrationUtils.createSoapUISampler( soapUIProjectPath, soapUITestSuite,
							soapUITestCase, targetProjectString, targetTestCaseName, targetSamplerName );
				}
				catch( IOException e )
				{
					UISupport.showInfoMessage( "Error while opening selected loadUI project" );
					return;
				}
				if( createdSamplerSettings != null )
				{
					String creationInfo = "SoapUISampler created under project: '"
							+ createdSamplerSettings.get( ContextMapping.LOADUI_PROJECT_NAME ) + "'";
					if( targetTestCaseName != null && !targetTestCaseName.equals( IntegrationUtils.CREATE_ON_PROJECT_LEVEL ) )
					{
						creationInfo += ", TestCase: '" + createdSamplerSettings.get( ContextMapping.LOADUI_TEST_CASE_NAME )
								+ "'";
					}
					UISupport.showInfoMessage( creationInfo, IntegrationUtils.LOADU_INFO_DIALOG_TITLE );
				}
			}

		}
	}

	@AForm( description = "Specify Items in LoadUI for Running TestCase", name = "Run With LoadUI", helpUrl = HelpUrls.CLONETESTCASE_HELP_URL, icon = UISupport.LOADUI_ICON_PATH )
	protected interface Form
	{
		@AField( name = "Target Project", description = "The target Project in loadUI", type = AFieldType.ENUMERATION )
		public final static String PROJECT = "Target Project";

		@AField( name = "Target TestCase", description = "The name of the target TestCase in loadUI", type = AFieldType.ENUMERATION )
		public final static String TESTCASE = "Target TestCase";

		@AField( name = "Target SoapUISampler", description = "The target SoapUISampler in loadUI", type = AFieldType.ENUMERATION )
		public final static String SOAPUISAMPLER = "Target SoapUISampler";

	}

}
