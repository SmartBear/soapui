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

import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.actions.project.StartLoadUI;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.integration.loadui.IntegrationUtils;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.XFormMultiSelectList;

public class GenerateLoadUITestsAction extends AbstractSoapUIAction<WsdlTestSuite>
{

	public static final String SOAPUI_ACTION_ID = "GenerateLoadUITestsAction";
	private WsdlTestSuite testSuite;

	public GenerateLoadUITestsAction()
	{
		super( "Generate loadUI Tests", "Select contained TestCases to run with loadUI" );
	}

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

			this.testSuite = testSuite;
			final String soapUITestSuite = testSuite.getName();
			final String soapUIProjectPath = testSuite.getProject().getPath();

			XFormDialog dialog = ADialogBuilder.buildDialog( Form.class );

			dialog.setOptions( Form.LOADUIPROJECT, IntegrationUtils.getAvailableProjects() );
			if( !StringUtils.isNullOrEmpty( IntegrationUtils.getOpenedProjectName() ) )
			{
				dialog.setValue( Form.LOADUIPROJECT, IntegrationUtils.getOpenedProjectName() );
			}
			else
			{
				dialog.setValue( Form.LOADUIPROJECT, IntegrationUtils.CREATE_NEW_OPTION );
			}
			dialog.setOptions( Form.TESTCASES, ModelSupport.getNames( testSuite.getTestCaseList() ) );
			dialog.setValue( Form.LEVEL, "Project Level" );

			if( dialog.show() )
			{

				if( dialog.getReturnValue() == XFormDialog.OK_OPTION )
				{
					int levelToAdd = dialog.getValueIndex( Form.LEVEL );
					String loadUIProject = dialog.getValue( Form.LOADUIPROJECT );
					String openedProjectName = IntegrationUtils.getOpenedProjectName();
					if( !StringUtils.isNullOrEmpty( openedProjectName ) && !loadUIProject.equals( openedProjectName )
							&& IntegrationUtils.checkOpenedLoadUIProjectForClose() )
					{
						return;
					}
					String[] soapuiTestCases = StringUtils.toStringArray( ( ( XFormMultiSelectList )dialog
							.getFormField( Form.TESTCASES ) ).getSelectedOptions() );
					if( soapuiTestCases.length == 0 )
					{
						UISupport.showErrorMessage( "No TestCases selected." );
						return;
					}
					try
					{
						IntegrationUtils.generateTestSuiteLoadTests( soapUIProjectPath, soapUITestSuite, soapuiTestCases,
								loadUIProject, levelToAdd );
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

	@AForm( description = "Specify target loadUI Project, select TestCases to add and the loadUI level to add them to", name = "Add multiple TestCases to loadUI", helpUrl = HelpUrls.CLONETESTSUITE_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		@AField( name = "Target Project", description = "The target loadUI Project to add to", type = AFieldType.ENUMERATION )
		public final static String LOADUIPROJECT = "Target Project";

		@AField( name = "Source TestCases", description = "The TestCases to add", type = AFieldType.MULTILIST )
		public final static String TESTCASES = "Source TestCases";

		@AField( name = "Level", description = "Select the level where to add Samplers", type = AFieldType.RADIOGROUP, values = {
				"Project Level", "Single TestCase ", "Separate TestCases" } )
		public final static String LEVEL = "Level";

	}

}
