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
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.actions.support.AbstractAddToTestCaseAction;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SchemaComplianceAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.NotSoapFaultAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.SoapResponseAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestRequestStepFactory;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.ui.desktop.SoapUIDesktop;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;
import com.eviware.x.form.XFormField;

/**
 * Adds a WsdlRequest to a WsdlTestCase as a WsdlTestRequestStep
 * 
 * @author Ole.Matzura
 */

public class AddRequestToTestCaseAction extends AbstractAddToTestCaseAction<WsdlRequest>
{
	public static final String SOAPUI_ACTION_ID = "AddRequestToTestCaseAction";

	private static final String STEP_NAME = "Name";
	private static final String ADD_SOAP_FAULT_ASSERTION = "Add Not SOAP Fault Assertion";
	private static final String ADD_SOAP_RESPONSE_ASSERTION = "Add SOAP Response Assertion";
	private static final String ADD_SCHEMA_ASSERTION = "Add Schema Assertion";
	private static final String CLOSE_REQUEST = "Close Request Window";
	private static final String SHOW_TESTCASE = "Shows TestCase Editor";
	private static final String COPY_ATTACHMENTS = "Copy Attachments";
	private static final String COPY_HTTPHEADERS = "Copy HTTP Headers";

	private XFormDialog dialog;
	private StringToStringMap dialogValues = new StringToStringMap();
	private XFormField closeRequestCheckBox;

	public AddRequestToTestCaseAction()
	{
		super( "Add to TestCase", "Adds this request to a TestCase" );
	}

	public void perform( WsdlRequest request, Object param )
	{
		WsdlProject project = request.getOperation().getInterface().getProject();

		WsdlTestCase testCase = getTargetTestCase( project );
		if( testCase != null )
			addRequest( testCase, request, -1 );
	}

	public WsdlTestRequestStep addRequest( WsdlTestCase testCase, WsdlRequest request, int position )
	{
		if( dialog == null )
			buildDialog();

		dialogValues.put( STEP_NAME, request.getOperation().getName() + " - " + request.getName() );
		dialogValues.put( CLOSE_REQUEST, "true" );
		dialogValues.put( SHOW_TESTCASE, "true" );
		dialog.getFormField( COPY_ATTACHMENTS ).setEnabled( request.getAttachmentCount() > 0 );
		dialog.setBooleanValue( COPY_ATTACHMENTS, true );

		dialog.getFormField( COPY_HTTPHEADERS ).setEnabled( request.getRequestHeaders().size() > 0 );
		dialog.setBooleanValue( COPY_HTTPHEADERS, false );

		SoapUIDesktop desktop = SoapUI.getDesktop();
		closeRequestCheckBox.setEnabled( desktop != null && desktop.hasDesktopPanel( request ) );

		boolean bidirectional = request.getOperation().isBidirectional();
		dialog.getFormField( ADD_SCHEMA_ASSERTION ).setEnabled( bidirectional );
		dialog.getFormField( ADD_SOAP_FAULT_ASSERTION ).setEnabled( bidirectional );
		dialog.getFormField( ADD_SOAP_RESPONSE_ASSERTION ).setEnabled( bidirectional );

		dialogValues = dialog.show( dialogValues );
		if( dialog.getReturnValue() != XFormDialog.OK_OPTION )
			return null;

		String name = dialogValues.get( STEP_NAME );

		WsdlTestRequestStep testStep = ( WsdlTestRequestStep )testCase.insertTestStep( WsdlTestRequestStepFactory
				.createConfig( request, name ), position );

		if( testStep == null )
			return null;

		if( dialogValues.getBoolean( COPY_ATTACHMENTS ) )
			request.copyAttachmentsTo( testStep.getTestRequest() );

		if( dialogValues.getBoolean( COPY_HTTPHEADERS ) )
			testStep.getTestRequest().setRequestHeaders( request.getRequestHeaders() );

		if( bidirectional )
		{
			if( dialogValues.getBoolean( ADD_SOAP_RESPONSE_ASSERTION ) )
				testStep.getTestRequest().addAssertion( SoapResponseAssertion.ID );

			if( dialogValues.getBoolean( ADD_SCHEMA_ASSERTION ) )
				testStep.getTestRequest().addAssertion( SchemaComplianceAssertion.ID );

			if( dialogValues.getBoolean( ADD_SOAP_FAULT_ASSERTION ) )
				testStep.getTestRequest().addAssertion( NotSoapFaultAssertion.LABEL );
		}

		UISupport.selectAndShow( testStep );

		if( dialogValues.getBoolean( CLOSE_REQUEST ) && desktop != null )
		{
			desktop.closeDesktopPanel( request );
		}

		if( dialogValues.getBoolean( SHOW_TESTCASE ) )
		{
			UISupport.selectAndShow( testCase );
		}

		return testStep;
	}

	private void buildDialog()
	{
		XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "Add Request to TestCase" );
		XForm mainForm = builder.createForm( "Basic" );

		mainForm.addTextField( STEP_NAME, "Name of TestStep", XForm.FieldType.URL ).setWidth( 30 );

		mainForm.addCheckBox( ADD_SOAP_RESPONSE_ASSERTION, "(adds validation that response is a SOAP message)" );
		mainForm.addCheckBox( ADD_SCHEMA_ASSERTION, "(adds validation that response complies with its schema)" );
		mainForm.addCheckBox( ADD_SOAP_FAULT_ASSERTION, "(adds validation that response is not a SOAP Fault)" );
		closeRequestCheckBox = mainForm.addCheckBox( CLOSE_REQUEST, "(closes the current window for this request)" );
		mainForm.addCheckBox( SHOW_TESTCASE, "(opens the TestCase editor for the target TestCase)" );
		mainForm.addCheckBox( COPY_ATTACHMENTS, "(copies the requests attachments to the TestRequest)" );
		mainForm.addCheckBox( COPY_HTTPHEADERS, "(copies the requests HTTP-Headers to the TestRequest)" );

		dialog = builder.buildDialog( builder.buildOkCancelActions(),
				"Specify options for adding the request to a TestCase", UISupport.OPTIONS_ICON );

		dialogValues.put( ADD_SOAP_RESPONSE_ASSERTION, Boolean.TRUE.toString() );
	}
}
