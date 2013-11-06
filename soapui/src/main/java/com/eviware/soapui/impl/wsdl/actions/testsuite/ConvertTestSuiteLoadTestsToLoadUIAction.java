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

package com.eviware.soapui.impl.wsdl.actions.testsuite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.actions.project.StartLoadUI;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.integration.loadui.IntegrationUtils;
import com.eviware.soapui.model.testsuite.LoadTest;
import com.eviware.soapui.model.testsuite.TestCase;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.XFormMultiSelectList;

public class ConvertTestSuiteLoadTestsToLoadUIAction extends AbstractSoapUIAction<WsdlTestSuite>
{
	public static final String SOAPUI_ACTION_ID = "ConvertTestSuiteLoadTestsToLoadUIAction";

	public ConvertTestSuiteLoadTestsToLoadUIAction()
	{
		super( "Convert LoadTests to loadUI TestCases", "Select Containing LoadTest to Convert to loadUI TestCases" );
	}

	@Override
	public void perform( WsdlTestSuite testSuite, Object param )
	{

		if( IntegrationUtils.forceSaveProject( testSuite.getProject() ) )
		{

			if( !StartLoadUI.testCajoConnection() )
			{
				if( UISupport.confirm( StartLoadUI.LOADUI_LAUNCH_QUESTION, StartLoadUI.LOADUI_LAUNCH_TITLE ) )
				{
					StartLoadUI.launchLoadUI();
				}
				return;
			}

			XFormDialog dialog = ADialogBuilder.buildDialog( TestSuiteForm.class );

			dialog.setOptions( TestSuiteForm.LOADUIPROJECT, IntegrationUtils.getAvailableProjects() );
			if( !StringUtils.isNullOrEmpty( IntegrationUtils.getOpenedProjectName() ) )
			{
				dialog.setValue( TestSuiteForm.LOADUIPROJECT, IntegrationUtils.getOpenedProjectName() );
			}
			else
			{
				dialog.setValue( TestSuiteForm.LOADUIPROJECT, IntegrationUtils.CREATE_NEW_OPTION );
			}
			List<String> testSuiteLoadTests = new ArrayList<String>();
			for( TestCase testCase : testSuite.getTestCaseList() )
			{
				for( LoadTest loadTest : testCase.getLoadTestList() )
				{
					testSuiteLoadTests.add( testCase.getName() + " - " + loadTest.getName() );
				}
			}
			String[] names = new String[testSuiteLoadTests.size()];
			for( int c = 0; c < names.length; c++ )
			{
				names[c] = testSuiteLoadTests.get( c );
			}
			dialog.setOptions( TestSuiteForm.LOADTESTS, names );
			if( dialog.show() )
			{
				if( dialog.getReturnValue() == XFormDialog.OK_OPTION )
				{
					String loadUIProject = dialog.getValue( TestSuiteForm.LOADUIPROJECT );
					String openedProjectName = IntegrationUtils.getOpenedProjectName();
					if( !StringUtils.isNullOrEmpty( openedProjectName ) && !loadUIProject.equals( openedProjectName )
							&& IntegrationUtils.checkOpenedLoadUIProjectForClose() )
					{
						return;
					}
					String[] soapuiLoadTests = StringUtils.toStringArray( ( ( XFormMultiSelectList )dialog
							.getFormField( TestSuiteForm.LOADTESTS ) ).getSelectedOptions() );
					if( soapuiLoadTests.length == 0 )
					{
						UISupport.showErrorMessage( "No LoadTests selected." );
						return;
					}
					try
					{
						IntegrationUtils.exportMultipleLoadTestToLoadUI( testSuite, soapuiLoadTests, loadUIProject );
					}
					catch( IOException e )
					{
						UISupport.showInfoMessage( "Error while opening selected loadUI project" );
						return;
					}
				}
			}
		}
	}

	@AForm( description = "Specify target loadUI Project and select LoadTests to convert", name = "Convert multiple LoadTests to loadUI", helpUrl = HelpUrls.CLONETESTSUITE_HELP_URL, icon = UISupport.CONVERT_TO_LOADUI_ICON_PATH )
	public interface TestSuiteForm
	{
		@AField( name = "Target Project", description = "The target loadUI Project to add to", type = AFieldType.ENUMERATION )
		public final static String LOADUIPROJECT = "Target Project";

		@AField( name = "Source LoadTests", description = "The LoadTests to convert", type = AFieldType.MULTILIST )
		public final static String LOADTESTS = "Source LoadTests";

	}

}
