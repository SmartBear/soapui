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

package com.eviware.soapui.impl.wsdl.teststeps.registry;

import java.util.ArrayList;

import com.eviware.soapui.config.AsyncResponseStepConfig;
import com.eviware.soapui.config.MockResponseConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.CompressedStringSupport;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlAsyncResponseTestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.util.ModelItemNames;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormField;
import com.eviware.x.form.XFormFieldListener;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AForm;
import com.eviware.x.form.support.AField.AFieldType;

public class AsyncResponseStepFactory extends WsdlTestStepFactory
{
	public static final String ASYNC_RESPONSE_TYPE = "asyncresponse";

	private static XFormDialog form;
	private static WsdlProject project;

	@AForm( name = "New Asynchronous Response Step", description = "Set options for the Asynchronous Response step" )
	private class CreateForm
	{
		@AField( name = "Name", description = "The name of the Asynchronous Response step", type = AFieldType.STRING )
		public static final String NAME = "Name";

		@AField( name = "Operation", description = "Specifies the operation to mock", type = AFieldType.ENUMERATION )
		public static final String OPERATION = "Operation";

		@AField( name = "Interface", description = "Specifies the interface containing the operation to be mocked", type = AFieldType.ENUMERATION )
		public static final String INTERFACE = "Interface";

		@AField( name = "Create Response", description = "Specifies if a mock response is to be created from the scema", type = AFieldType.BOOLEAN )
		public static final String CREATE_RESPONSE = "Create Response";

		@AField( name = "Port", description = "Specifies the port to listen on", type = AFieldType.STRING )
		public static final String PORT = "Port";

		@AField( name = "Path", description = "Specifies the path to listen on", type = AFieldType.STRING )
		public static final String PATH = "Path";

		@AField( name = "Request Query", description = "Specifies the XPath query to use when extracting information from the incoming request", type = AFieldType.STRING )
		public static final String REQUEST_QUERY = "Request Query";

		@AField( name = "Matching Value", description = "Specifies the value/property that must match the result of the request query", type = AFieldType.STRING )
		public static final String MATCHING_VALUE = "Matching Value";
	}

	public AsyncResponseStepFactory()
	{
		super( ASYNC_RESPONSE_TYPE, "Asynchronous Response",
				"Responds to an asynchronous call based on XPath expressions", "/asyncResponseStep.gif" );
	}

	public WsdlTestStep buildTestStep( WsdlTestCase testcase, TestStepConfig config, boolean flag )
	{
		return new WsdlAsyncResponseTestStep( testcase, config, flag );
	}

	public TestStepConfig createNewTestStep( WsdlTestCase testCase, String name )
	{
		createForm();

		return createConfig( testCase.getTestSuite().getProject(), name );
	}

	private static void createForm()
	{
		if( form == null )
		{
			form = ADialogBuilder.buildDialog( AsyncResponseStepFactory.CreateForm.class );
			form.getFormField( CreateForm.INTERFACE ).addFormFieldListener( new XFormFieldListener()
			{
				public void valueChanged( XFormField sourceField, String newValue, String oldValue )
				{
					WsdlInterface wsdlinterface = ( WsdlInterface )project.getInterfaceByName( newValue );
					form.setOptions( CreateForm.OPERATION, ( new ModelItemNames( wsdlinterface.getOperationList() ) )
							.getNames() );
				}

			} );

			form.setBooleanValue( CreateForm.CREATE_RESPONSE, true );
			form.setValue( CreateForm.PATH, "/" );
			form.setValue( CreateForm.REQUEST_QUERY, "" );
			form.setValue( CreateForm.MATCHING_VALUE, "" );
		}
	}

	private static TestStepConfig createConfig( WsdlProject wsdlProject, String name )
	{
		project = wsdlProject;

		ArrayList<WsdlInterface> arraylist = new ArrayList<WsdlInterface>();
		for( int i = 0; i < project.getInterfaceCount(); i++ )
		{
			AbstractInterface<?> iface = project.getInterfaceAt( i );
			if( iface.getInterfaceType().equals( WsdlInterfaceFactory.WSDL_TYPE ) && iface.getOperationCount() > 0 )
			{
				arraylist.add( ( WsdlInterface )iface );
			}
		}

		if( arraylist.isEmpty() )
		{
			UISupport.showErrorMessage( "Missing Interfaces/Operations to mock" );
			return null;
		}

		form.setValue( CreateForm.NAME, name );
		form.setOptions( CreateForm.INTERFACE, ( new ModelItemNames<WsdlInterface>( arraylist ) ).getNames() );
		form
				.setOptions( CreateForm.OPERATION, ( new ModelItemNames( arraylist.get( 0 ).getOperationList() ) )
						.getNames() );

		if( !form.show() )
		{
			return null;
		}

		TestStepConfig testStepConfig;
		( testStepConfig = TestStepConfig.Factory.newInstance() ).setType( ASYNC_RESPONSE_TYPE );
		testStepConfig.setName( form.getValue( CreateForm.NAME ) );

		AsyncResponseStepConfig config = AsyncResponseStepConfig.Factory.newInstance();
		config.setInterface( form.getValue( CreateForm.INTERFACE ) );
		config.setOperation( form.getValue( CreateForm.OPERATION ) );
		config.setPort( form.getIntValue( CreateForm.PORT, 8080 ) );
		config.setPath( form.getValue( CreateForm.PATH ) );
		config.setRequestQuery( form.getValue( CreateForm.REQUEST_QUERY ) );
		config.setMatchingValue( form.getValue( CreateForm.MATCHING_VALUE ) );
		config.addNewResponse();
		config.getResponse().addNewResponseContent();

		if( form.getBooleanValue( CreateForm.CREATE_RESPONSE ) )
		{
			WsdlInterface iface = ( WsdlInterface )project.getInterfaceByName( config.getInterface() );
			String content = iface.getOperationByName( config.getOperation() ).createResponse(
					project.getSettings().getBoolean( WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS ) );
			CompressedStringSupport.setString( config.getResponse().getResponseContent(), content );
		}

		testStepConfig.addNewConfig().set( config );

		return testStepConfig;
	}

	public static TestStepConfig createConfig( WsdlOperation operation, boolean flag )
	{
		return createConfig( operation, null, flag );
	}

	public static TestStepConfig createConfig( WsdlRequest request, boolean flag )
	{
		return createConfig( request.getOperation(), request, flag );
	}

	public static TestStepConfig createConfig( WsdlOperation operation, WsdlRequest request, boolean flag )
	{
		if( flag )
		{
			createForm();

			form.setValue( CreateForm.INTERFACE, operation.getInterface().getName() );
			form.setValue( CreateForm.OPERATION, operation.getName() );
			form.setBooleanValue( CreateForm.CREATE_RESPONSE, request.getResponse() == null );

			return createConfig( operation.getInterface().getProject(), ( new StringBuilder() ).append( request.getName() )
					.append( " Response" ).toString() );
		}

		TestStepConfig testStepConfig = com.eviware.soapui.config.TestStepConfig.Factory.newInstance();
		testStepConfig.setType( ASYNC_RESPONSE_TYPE );
		testStepConfig.setName( "Asynchronous Response" );

		AsyncResponseStepConfig config = AsyncResponseStepConfig.Factory.newInstance();
		config.setInterface( operation.getInterface().getName() );
		config.setOperation( operation.getName() );

		MockResponseConfig mockResponseConfig = config.addNewResponse();
		mockResponseConfig.addNewResponseContent();
		if( request != null && request.getResponse() != null )
		{
			CompressedStringSupport.setString( mockResponseConfig.getResponseContent(), request.getResponse()
					.getContentAsString() );
		}

		testStepConfig.addNewConfig().set( config );

		return testStepConfig;
	}

	public static TestStepConfig createNewTestStep( WsdlMockResponse response )
	{
		WsdlOperation wsdloperation;
		if( ( wsdloperation = response.getMockOperation().getOperation() ) == null )
		{
			UISupport.showErrorMessage( "Missing operation for this mock response" );
			return null;
		}
		else
		{
			createForm();

			form.setValue( CreateForm.INTERFACE, wsdloperation.getInterface().getName() );
			form.setValue( CreateForm.OPERATION, wsdloperation.getName() );
			form.setBooleanValue( CreateForm.CREATE_RESPONSE, false );
			form.setIntValue( CreateForm.PORT, response.getMockOperation().getMockService().getPort() );
			form.setValue( CreateForm.PATH, response.getMockOperation().getMockService().getPath() );
			// form.setValue(CreateForm.REQUEST_QUERY,
			// response.getMockOperation().getMockService().getRequestQuery());
			// form.setValue(CreateForm.MATCHING_VALUE,
			// response.getMockOperation().getMockService().getMatchingValue());

			return createConfig( wsdloperation.getInterface().getProject(), ( new StringBuilder() ).append(
					response.getMockOperation().getName() ).append( " - " ).append( response.getName() ).toString() );
		}
	}

	public boolean canCreate()
	{
		return true;
	}
}