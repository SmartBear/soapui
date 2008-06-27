/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.teststeps.registry;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.CredentialsConfig;
import com.eviware.soapui.config.RequestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.config.WsdlRequestConfig;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.x.form.XForm;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormDialogBuilder;
import com.eviware.x.form.XFormFactory;

/**
 * Factory for WsdlTestRequestSteps
 * 
 * @author Ole.Matzura
 */

public class RestRequestStepFactory extends WsdlTestStepFactory
{
	public static final String REQUEST_TYPE = "restrequest";
	public static final String STEP_NAME = "Name";
	private XFormDialog dialog;
	private StringToStringMap dialogValues = new StringToStringMap();

	public RestRequestStepFactory()
	{
		super( REQUEST_TYPE, "Test Request", "Submits a request and validates its response", "/request.gif" );
	}

	public WsdlTestStep buildTestStep(WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest)
	{
		return new WsdlTestRequestStep( testCase, config, forLoadTest );
	}

	public static TestStepConfig createConfig(WsdlRequest request, String stepName)
	{
		RequestStepConfig requestStepConfig = RequestStepConfig.Factory.newInstance();
		
      requestStepConfig.setInterface( request.getOperation().getInterface().getName() );
      requestStepConfig.setOperation( request.getOperation().getName() );

      WsdlRequestConfig testRequestConfig = requestStepConfig.addNewRequest();
      
      testRequestConfig.setName( stepName );
      testRequestConfig.setEncoding( request.getEncoding() );
      testRequestConfig.setEndpoint( request.getEndpoint() );
      testRequestConfig.addNewRequest().setStringValue( request.getRequestContent() );
      testRequestConfig.setOutgoingWss( request.getOutgoingWss() );
      testRequestConfig.setIncomingWss( request.getIncomingWss() );

      if( (CredentialsConfig) request.getConfig().getCredentials() != null )
      {
      	testRequestConfig.setCredentials( (CredentialsConfig) request.getConfig().getCredentials().copy() );
      }

      testRequestConfig.setWssPasswordType( request.getConfig().getWssPasswordType() );
      //testRequestConfig.setSettings( request.getConfig().getSettings() );
      
      TestStepConfig testStep = TestStepConfig.Factory.newInstance();
      testStep.setType( REQUEST_TYPE );
      testStep.setConfig( requestStepConfig );

		return testStep;
	}

	public static TestStepConfig createConfig( WsdlOperation operation, String stepName )
	{
		RequestStepConfig requestStepConfig = RequestStepConfig.Factory.newInstance();
		
      requestStepConfig.setInterface( operation.getInterface().getName() );
      requestStepConfig.setOperation( operation.getName() );

      WsdlRequestConfig testRequestConfig = requestStepConfig.addNewRequest();
      
      testRequestConfig.setName( stepName );
      testRequestConfig.setEncoding( "UTF-8" );
      String[] endpoints = operation.getInterface().getEndpoints();
      if( endpoints.length > 0 )
      	testRequestConfig.setEndpoint( endpoints[0] );
      
      String requestContent = operation.createRequest( 
      			SoapUI.getSettings().getBoolean( WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS ));
      testRequestConfig.addNewRequest().setStringValue( requestContent );
      
      TestStepConfig testStep = TestStepConfig.Factory.newInstance();
      testStep.setType( REQUEST_TYPE );
      testStep.setConfig( requestStepConfig );

		return testStep;
	}
	
	public TestStepConfig createNewTestStep(WsdlTestCase testCase, String name)
	{
		// build list of available interfaces / operations
		Project project = testCase.getTestSuite().getProject();
		List<String> options = new ArrayList<String>();
		List<Operation> operations = new ArrayList<Operation>();

		for( int c = 0; c < project.getInterfaceCount(); c++ )
		{
			Interface iface = project.getInterfaceAt( c );
			for( int i = 0; i < iface.getOperationCount(); i++ )
			{
				options.add( iface.getName() + " -> " + iface.getOperationAt( i ).getName() );
				operations.add( iface.getOperationAt( i ));
			}
		}
		
		Object op = UISupport.prompt( "Select operation to invoke for request", "New TestRequest", options.toArray() );
		if( op != null )
		{
			int ix = options.indexOf( op );
			if( ix != -1 )
			{
				WsdlOperation operation = (WsdlOperation) operations.get( ix );
				
				if( dialog == null )
					buildDialog();
				
				dialogValues.put( STEP_NAME, name );
				dialogValues = dialog.show( dialogValues );
				if( dialog.getReturnValue() != XFormDialog.OK_OPTION )
					return null;

				return createNewTestStep(operation, dialogValues);
			}
		}
		
		return null;
	}

	public TestStepConfig createNewTestStep(WsdlOperation operation, StringToStringMap values )
	{
		String name;
		name = values.get( STEP_NAME );
		
		RequestStepConfig requestStepConfig = RequestStepConfig.Factory.newInstance();
		
		requestStepConfig.setInterface( operation.getInterface().getName() );
		requestStepConfig.setOperation( operation.getName() );

		WsdlRequestConfig testRequestConfig = requestStepConfig.addNewRequest();
		
		testRequestConfig.setName( name );
		testRequestConfig.setEncoding( "UTF-8" );
		String[] endpoints = operation.getInterface().getEndpoints();
		if( endpoints.length > 0 )
			testRequestConfig.setEndpoint( endpoints[0] );
		
		TestStepConfig testStep = TestStepConfig.Factory.newInstance();
		testStep.setType( REQUEST_TYPE );
		testStep.setConfig( requestStepConfig );
		testStep.setName( name );

		return testStep;
	}

	public boolean canCreate()
	{
		return true;
	}
	
	private void buildDialog()
	{
		XFormDialogBuilder builder = XFormFactory.createDialogBuilder("Add Request to TestCase");
		XForm mainForm = builder.createForm( "Basic" );
		
		mainForm.addTextField( STEP_NAME, "Name of TestStep", XForm.FieldType.URL ).setWidth( 30 );

		dialog = builder.buildDialog( builder.buildOkCancelActions(), 
				"Specify options for adding a new request to a TestCase", UISupport.OPTIONS_ICON);		
	}
}
