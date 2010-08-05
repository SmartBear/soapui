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
package com.eviware.soapui.impl.wsdl.actions.project;

import com.eviware.soapui.impl.rest.panels.request.views.html.HttpHtmlResponseView;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.panels.teststeps.HttpTestRequestDesktopPanel;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.impl.wsdl.teststeps.HttpTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.registry.HttpRequestStepFactory;
import com.eviware.soapui.model.iface.Request.SubmitException;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.model.testsuite.TestSuite;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

public class AddNewWebTestAction extends AbstractSoapUIAction<WsdlProject>
{

	public static final String SOAPUI_ACTION_ID = "AddNewWebTestAction";
	private WsdlProject project;
	public static final MessageSupport messages = MessageSupport.getMessages( AddNewWebTestAction.class );
	private static final String CREATE_NEW_OPTION = "<Create New>";
	private XFormDialog dialog;

	public AddNewWebTestAction()
	{
		super( "Add WebTest", "Add new WebTest for recording HTTP trafic" );
	}

	public void perform( WsdlProject target, Object param )
	{
		if( !StartLoadUI.testCajoConnection() )
		{
			if( UISupport.confirm( StartLoadUI.LOADUI_LAUNCH_QUESTION, StartLoadUI.LOADUI_LAUNCH_TITLE ) )
			{
				StartLoadUI.launchLoadUI();
			}
			return;
		}
		this.project = target;
		if( dialog == null )
		{
			dialog = ADialogBuilder.buildDialog( Form.class );

			dialog.getFormField( Form.TESTSUITE ).addFormFieldListener( new XFormFieldListener()
			{

				@Override
				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
				{
					if( newValue.equals( CREATE_NEW_OPTION ) )
					{
						dialog.setOptions( Form.TESTCASE, new String[] { CREATE_NEW_OPTION } );
					}
					else
					{
						TestSuite testSuite = project.getTestSuiteByName( newValue );
						dialog.setOptions( Form.TESTCASE, testSuite == null ? new String[] { CREATE_NEW_OPTION }
								: ModelSupport.getNames( testSuite.getTestCaseList(), new String[] { CREATE_NEW_OPTION } ) );
					}
				}
			} );
		}

		dialog.setOptions( Form.TESTSUITE, ModelSupport.getNames( new String[] { CREATE_NEW_OPTION }, project
				.getTestSuiteList() ) );
		dialog.setValue( Form.TESTSUITE, CREATE_NEW_OPTION );

		dialog.setOptions( Form.TESTCASE, new String[] { CREATE_NEW_OPTION } );
		dialog.setValue( Form.TESTCASE, CREATE_NEW_OPTION );
		if( dialog.show() )
		{
			String targetTestSuiteName = dialog.getValue( Form.TESTSUITE );
			String targetTestCaseName = dialog.getValue( Form.TESTCASE );
			WsdlTestSuite targetTestSuite = null;
			WsdlTestCase targetTestCase = null;

			targetTestSuite = project.getTestSuiteByName( targetTestSuiteName );
			if( targetTestSuite == null )
			{
				targetTestSuiteName = UISupport.prompt( "Specify name for new TestSuite", "Clone TestStep",
						"WebTest TestSuite" );
				if( targetTestSuiteName == null )
					return;

				targetTestSuite = project.addNewTestSuite( targetTestSuiteName );
			}
			targetTestCase = targetTestSuite.getTestCaseByName( targetTestCaseName );
			if( targetTestCase == null )
			{
				targetTestCaseName = UISupport.prompt( "Specify name for new TestCase", "Clone TestStep",
						"WebTest TestCase" );
				if( targetTestCaseName == null )
					return;

				targetTestCase = targetTestSuite.addNewTestCase( targetTestCaseName );

			}
			createWebTest( targetTestCase );

		}
	}

	public void createWebTest( WsdlTestCase targetTestCase )
	{
		WsdlTestStep testStep = targetTestCase.addTestStep( HttpRequestStepFactory.HTTPREQUEST_TYPE, "WebTest" );
		HttpTestRequestDesktopPanel desktopPanel = ( HttpTestRequestDesktopPanel )UISupport.selectAndShow( testStep );
		try
		{
			( ( HttpTestRequestStep )testStep ).getTestRequest().submit( new WsdlTestRunContext( testStep ), true );
		}
		catch( SubmitException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpHtmlResponseView cc = ( HttpHtmlResponseView )desktopPanel.getResponseEditor().getViews().get( 2 );
		cc.setRecordHttpTrafic( true );
		desktopPanel.getResponseEditor().selectView( 2 );
	}

	@AForm( description = "Specify target TestSuite/TestCase for the WebTest", name = "Add WebTest", helpUrl = HelpUrls.CLONETESTSUITE_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		@AField( name = "Target TestSuite", description = "The target TestSuite to add WebTest to", type = AFieldType.ENUMERATION )
		public final static String TESTSUITE = "Target TestSuite";

		@AField( name = "Target TestCase", description = "The target TestCase to add WebTest to", type = AFieldType.ENUMERATION )
		public final static String TESTCASE = "Target TestCase";

	}

}
