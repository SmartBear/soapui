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

package com.eviware.soapui.impl.wsdl.actions.mockresponse;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.actions.support.AbstractAddToTestCaseAction;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.SchemaComplianceAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.NotSoapFaultAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.soap.SoapResponseAssertion;
import com.eviware.soapui.impl.wsdl.teststeps.registry.WsdlTestRequestStepFactory;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

/**
 * Adds a WsdlRequest to a WsdlTestCase as a WsdlTestRequestStep
 * 
 * @author Ole.Matzura
 */

public class CreateTestRequestForMockResponseAction extends AbstractAddToTestCaseAction<WsdlMockResponse>
{
	public static final String SOAPUI_ACTION_ID = "CreateTestRequestForMockResponseAction";

	private static final String STEP_NAME = "Name";
	private static final String ADD_SOAP_FAULT_ASSERTION = "Add Not SOAP Fault Assertion";
	private static final String ADD_SOAP_RESPONSE_ASSERTION = "Add SOAP Response Assertion";
	private static final String ADD_SCHEMA_ASSERTION = "Add Schema Assertion";

	private XFormDialog dialog;
	private StringToStringMap dialogValues = new StringToStringMap();

	public CreateTestRequestForMockResponseAction()
	{
		super( "Create TestRequest", "Creates a TestRequest for this MockResponse in a TestCase" );
	}

	public void perform( WsdlMockResponse mockResponse, Object param )
	{
		WsdlProject project = ( WsdlProject )ModelSupport.getModelItemProject( mockResponse );

		WsdlTestCase testCase = getTargetTestCase( project );
		if( testCase != null )
			addRequest( testCase, mockResponse, -1 );
	}

	public WsdlTestRequestStep addRequest( WsdlTestCase testCase, WsdlMockResponse mockResponse, int position )
	{
		if( dialog == null )
			buildDialog();

		dialogValues.put( STEP_NAME, mockResponse.getMockOperation().getName() + " - " + mockResponse.getName() );

		boolean enabled = mockResponse.getMockOperation().getOperation().isBidirectional();
		dialog.getFormField( ADD_SCHEMA_ASSERTION ).setEnabled( enabled );
		dialog.getFormField( ADD_SOAP_FAULT_ASSERTION ).setEnabled( enabled );
		dialog.getFormField( ADD_SOAP_RESPONSE_ASSERTION ).setEnabled( enabled );

		dialogValues = dialog.show( dialogValues );
		if( dialog.getReturnValue() != XFormDialog.OK_OPTION )
			return null;
		;

		String name = dialogValues.get( STEP_NAME );

		WsdlTestRequestStep testStep = ( WsdlTestRequestStep )testCase.insertTestStep( WsdlTestRequestStepFactory
				.createConfig( mockResponse.getMockOperation().getOperation(), name ), position );

		if( testStep == null )
			return null;

		if( enabled )
		{
			if( dialogValues.getBoolean( ADD_SOAP_RESPONSE_ASSERTION ) )
				testStep.getTestRequest().addAssertion( SoapResponseAssertion.ID );

			if( dialogValues.getBoolean( ADD_SCHEMA_ASSERTION ) )
				testStep.getTestRequest().addAssertion( SchemaComplianceAssertion.ID );

			if( dialogValues.getBoolean( ADD_SOAP_FAULT_ASSERTION ) )
				testStep.getTestRequest().addAssertion( NotSoapFaultAssertion.LABEL );
		}

		testStep.getTestRequest().setEndpoint( mockResponse.getMockOperation().getMockService().getLocalEndpoint() );

		UISupport.selectAndShow( testStep );

		return testStep;
	}

	private void buildDialog()
	{
		XFormDialogBuilder builder = XFormFactory.createDialogBuilder( "Create TestRequest" );
		XForm mainForm = builder.createForm( "Basic" );

		mainForm.addTextField( STEP_NAME, "Name of TestRequest Step", XForm.FieldType.URL ).setWidth( 30 );

		mainForm.addCheckBox( ADD_SOAP_RESPONSE_ASSERTION, "(adds validation that response is a SOAP message)" );
		mainForm.addCheckBox( ADD_SCHEMA_ASSERTION, "(adds validation that response complies with its schema)" );
		mainForm.addCheckBox( ADD_SOAP_FAULT_ASSERTION, "(adds validation that response is not a SOAP Fault)" );

		dialog = builder.buildDialog( builder.buildOkCancelActions(), "Specify options for creating the TestRequest",
				UISupport.OPTIONS_ICON );

		dialogValues.put( ADD_SOAP_RESPONSE_ASSERTION, Boolean.TRUE.toString() );
	}
}
