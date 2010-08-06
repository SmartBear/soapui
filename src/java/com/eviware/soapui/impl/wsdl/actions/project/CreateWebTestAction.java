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

import com.eviware.soapui.config.HttpRequestConfig;
import com.eviware.soapui.config.TestStepConfig;
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
import com.sun.java.xml.ns.j2Ee.HttpMethodType;

public class CreateWebTestAction extends AbstractSoapUIAction<WsdlProject>
{

	public static final String SOAPUI_ACTION_ID = "AddNewWebTestAction";
	private WsdlProject project;
	public static final MessageSupport messages = MessageSupport.getMessages( CreateWebTestAction.class );
	private static final String CREATE_NEW_OPTION = "<Create New>";
	private XFormDialog dialog;

	public CreateWebTestAction()
	{
		super( "New Web Test", "New Web Test" );
	}

	public void perform( WsdlProject target, Object param )
	{
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
		dialog.setValue( Form.WEBTESTNAME, "Web Test" );
		dialog.setValue( Form.ENDPOINT, "" );
		dialog.setValue( Form.STARTRECORDING, Boolean.toString( true ) );
		if( dialog.show() )
		{
			String targetTestSuiteName = dialog.getValue( Form.TESTSUITE );
			String targetTestCaseName = dialog.getValue( Form.TESTCASE );
			String name = dialog.getValue( Form.WEBTESTNAME );
			WsdlTestSuite targetTestSuite = null;
			WsdlTestCase targetTestCase = null;

			targetTestSuite = project.getTestSuiteByName( targetTestSuiteName );
			if( targetTestSuite == null )
			{
				targetTestSuiteName = "WebTest TestSuite";
				while( project.getTestSuiteByName( targetTestSuiteName ) != null )
				{
					targetTestSuiteName = UISupport.prompt(
							"TestSuite name must be unique, please specify new name for TestSuite\n" + "["
									+ targetTestSuiteName + "]", "Change TestSuite name", targetTestSuiteName );

					if( targetTestSuiteName == null )
						return;
				}

				targetTestSuite = project.addNewTestSuite( targetTestSuiteName );
			}
			targetTestCase = targetTestSuite.getTestCaseByName( targetTestCaseName );
			if( targetTestCase == null )
			{
				targetTestCaseName = "WebTest TestCase";
				while( targetTestSuite.getTestCaseByName( targetTestCaseName ) != null )
				{
					targetTestCaseName = UISupport
							.prompt( "TestCase name must be unique, please specify new name for TestCase\n" + "["
									+ targetTestCaseName + "] in TestSuite [" + targetTestSuiteName + "]",
									"Change TestCase name", targetTestCaseName );
					if( targetTestCaseName == null )
						return;
				}
				targetTestCase = targetTestSuite.addNewTestCase( targetTestCaseName );

			}
			while( name == null || targetTestCase.getTestStepByName( name ) != null )
			{
				name = UISupport.prompt( "TestStep name must be unique, please specify new name for step\n" + "[" + name
						+ "] in TestCase [" + targetTestSuiteName + "->" + targetTestCaseName + "]", "Change TestStep name",
						name );

				if( name == null )
					return;
			}
			createWebTest( targetTestCase, dialog.getValue( Form.ENDPOINT ), name, dialog
					.getBooleanValue( Form.STARTRECORDING ) );

		}
	}

	public void createWebTest( WsdlTestCase targetTestCase, String endpoint, String name, boolean startRecording )
	{
		HttpRequestConfig httpRequest = HttpRequestConfig.Factory.newInstance();
		httpRequest.setMethod( HttpMethodType.GET.toString() );

		httpRequest.setEndpoint( endpoint );

		TestStepConfig testStepConfig = TestStepConfig.Factory.newInstance();
		testStepConfig.setType( HttpRequestStepFactory.HTTPREQUEST_TYPE );
		testStepConfig.setConfig( httpRequest );
		testStepConfig.setName( name );
		WsdlTestStep testStep = targetTestCase.addTestStep( testStepConfig );

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
		if( startRecording )
		{
			cc.setRecordHttpTrafic( true );
		}
		desktopPanel.getResponseEditor().selectView( 2 );
	}

	@AForm( description = "Specify target TestSuite/TestCase for the WebTest", name = "Add WebTest", helpUrl = HelpUrls.CLONETESTSUITE_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		@AField( name = "Web Test Name", description = "The WebTestname", type = AFieldType.STRING )
		public final static String WEBTESTNAME = "Web Test Name";

		@AField( name = "Target TestSuite", description = "The target TestSuite to add WebTest to", type = AFieldType.ENUMERATION )
		public final static String TESTSUITE = "Target TestSuite";

		@AField( name = "Target TestCase", description = "The target TestCase to add WebTest to", type = AFieldType.ENUMERATION )
		public final static String TESTCASE = "Target TestCase";

		@AField( description = "Endpoint", type = AField.AFieldType.STRING )
		public final static String ENDPOINT = messages.get( "Endpoint" );

		@AField( description = "Start Recording immediately", type = AFieldType.BOOLEAN, enabled = true )
		public final static String STARTRECORDING = messages.get( "Start Recording immediately" );

	}
}
