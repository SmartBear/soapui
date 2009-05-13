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

package com.eviware.soapui.impl.wsdl.actions.request;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.CompressedStringConfig;
import com.eviware.soapui.config.MockResponseStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.actions.support.AbstractAddToTestCaseAction;
import com.eviware.soapui.impl.wsdl.support.CompressedStringSupport;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlMockResponseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SchemaComplianceAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlMockResponseStepFactory;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.ui.desktop.SoapUIDesktop;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

public class AddRequestAsMockResponseStepAction extends AbstractAddToTestCaseAction<WsdlRequest>
{
	public static final String SOAPUI_ACTION_ID = "AddRequestAsMockResponseStepAction";
	private XFormDialog dialog;

	public AddRequestAsMockResponseStepAction()
	{
		super( "Add as MockResponse Step", "Creates a MockResponseStep from this Request" );
	}

	public void perform( WsdlRequest request, Object param )
	{
		WsdlTestCase testCase = getTargetTestCase( request.getOperation().getInterface().getProject() );
		if( testCase != null )
			addMockResponse( testCase, request );
	}

	protected boolean addMockResponse( WsdlTestCase testCase, WsdlRequest request )
	{
		String title = getName();
		boolean create = false;

		if( dialog == null )
			dialog = ADialogBuilder.buildDialog( Form.class );

		WsdlOperation operation = request.getOperation();
		dialog.setValue( Form.STEP_NAME, operation.getName() );
		dialog.setBooleanValue( Form.CLOSE_REQUEST, true );
		dialog.setBooleanValue( Form.SHOW_TESTCASE, true );
		dialog.setIntValue( Form.PORT, 8181 );
		dialog.setValue( Form.PATH, "/" + operation.getName() );

		SoapUIDesktop desktop = SoapUI.getDesktop();
		dialog.getFormField( Form.CLOSE_REQUEST ).setEnabled( desktop != null && desktop.hasDesktopPanel( request ) );

		if( !dialog.show() )
			return false;

		TestStepConfig config = WsdlMockResponseStepFactory.createConfig( operation, request, false );
		MockResponseStepConfig mockResponseStepConfig = ( ( MockResponseStepConfig )config.getConfig() );

		config.setName( dialog.getValue( Form.STEP_NAME ) );
		mockResponseStepConfig.setPath( dialog.getValue( Form.PATH ) );
		mockResponseStepConfig.setPort( dialog.getIntValue( Form.PORT, 8181 ) );
		CompressedStringConfig responseContent = mockResponseStepConfig.getResponse().getResponseContent();

		if( request.getResponse() == null && !request.getOperation().isOneWay() )
		{
			create = UISupport.confirm( "Request is missing response, create default mock response instead?", title );
		}

		if( create )
		{
			String response = operation.createResponse( operation.getSettings().getBoolean(
					WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS ) );
			CompressedStringSupport.setString( responseContent, response );
		}
		else if( request.getResponse() != null )
		{
			String response = request.getResponse().getContentAsString();
			CompressedStringSupport.setString( responseContent, response );
		}

		WsdlMockResponseTestStep testStep = ( WsdlMockResponseTestStep )testCase.addTestStep( config );

		if( dialog.getBooleanValue( Form.ADD_SCHEMA_ASSERTION ) )
			testStep.addAssertion( SchemaComplianceAssertion.ID );

		UISupport.selectAndShow( testStep );

		if( dialog.getBooleanValue( Form.CLOSE_REQUEST ) && desktop != null )
		{
			desktop.closeDesktopPanel( request );
		}

		if( dialog.getBooleanValue( Form.SHOW_TESTCASE ) )
		{
			UISupport.selectAndShow( testCase );
		}

		return true;
	}

	@AForm( name = "Add MockResponse to TestCase", description = "Options for adding this requests response to a TestCase", helpUrl = HelpUrls.ADDREQUESTASMOCKRESPONSESTEP_HELP_URL, icon = UISupport.OPTIONS_ICON_PATH )
	private interface Form
	{
		@AField( name = "Name", description = "Unique name of MockResponse Step" )
		public final static String STEP_NAME = "Name";

		@AField( name = "Path", description = "Path to listen on" )
		public final static String PATH = "Path";

		@AField( name = "Port", description = "Port to listen on", type = AFieldType.INT )
		public final static String PORT = "Port";

		@AField( name = "Add Schema Assertion", description = "Adds SchemaCompliance Assertion for request", type = AFieldType.BOOLEAN )
		public final static String ADD_SCHEMA_ASSERTION = "Add Schema Assertion";

		@AField( name = "Close Request Window", description = "Closes the request editor if visible", type = AFieldType.BOOLEAN )
		public final static String CLOSE_REQUEST = "Close Request Window";

		@AField( name = "Shows TestCase Editor", description = "Shows the target steps TestCase editor", type = AFieldType.BOOLEAN )
		public final static String SHOW_TESTCASE = "Shows TestCase Editor";
	}
}
