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
import java.util.List;

import com.eviware.soapui.config.RestMethodConfig;
import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.config.RestRequestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.RestTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStep;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.types.TupleList;

/**
 * Factory for WsdlTestRequestSteps
 * 
 * @author Ole.Matzura
 */

public class RestRequestStepFactory extends WsdlTestStepFactory
{
	public static final String RESTREQUEST_TYPE = "restrequest";
	public static final String STEP_NAME = "Name";

	// private XFormDialog dialog;
	// private StringToStringMap dialogValues = new StringToStringMap();

	public RestRequestStepFactory()
	{
		super( RESTREQUEST_TYPE, "REST Test Request", "Submits a REST-style Request and validates its response",
				"/rest_request.gif" );
	}

	public WsdlTestStep buildTestStep( WsdlTestCase testCase, TestStepConfig config, boolean forLoadTest )
	{
		return new RestTestRequestStep( testCase, config, forLoadTest );
	}

	public static TestStepConfig createConfig( RestRequest request, String stepName )
	{
		RestRequestStepConfig requestStepConfig = RestRequestStepConfig.Factory.newInstance();

		requestStepConfig.setService( request.getOperation().getInterface().getName() );
		requestStepConfig.setResourcePath( request.getOperation().getFullPath() );
		requestStepConfig.addNewRestRequest().set( request.getConfig().copy() );

		TestStepConfig testStep = TestStepConfig.Factory.newInstance();
		testStep.setType( RESTREQUEST_TYPE );
		testStep.setConfig( requestStepConfig );
		testStep.setName( stepName );

		return testStep;
	}

	public TestStepConfig createNewTestStep( WsdlTestCase testCase, String name )
	{
		// build list of available interfaces / restResources
		Project project = testCase.getTestSuite().getProject();
		List<String> options = new ArrayList<String>();
		TupleList<RestResource, RestRequest> restResources = new TupleList<RestResource, RestRequest>();

		for( int c = 0; c < project.getInterfaceCount(); c++ )
		{
			Interface iface = project.getInterfaceAt( c );
			if( iface instanceof RestService )
			{
				List<RestResource> resources = ( ( RestService )iface ).getAllResources();

				for( RestResource resource : resources )
				{
					options.add( iface.getName() + " -> " + resource.getPath() );
					restResources.add( resource, null );

					for( RestRequest request : resource.getRequests().values() )
					{
						restResources.add( resource, request );
						options.add( iface.getName() + " -> " + resource.getPath() + " -> " + request.getName() );
					}
				}
			}
		}

		if( restResources.size() == 0 )
		{
			UISupport.showErrorMessage( "Missing REST Resources in project" );
			return null;
		}

		Object op = UISupport.prompt( "Select Resource to invoke for request", "New RestRequest", options.toArray() );
		if( op != null )
		{
			int ix = options.indexOf( op );
			if( ix != -1 )
			{
				TupleList<RestResource, RestRequest>.Tuple tuple = restResources.get( ix );

				// if( dialog == null )
				// buildDialog();
				//
				// dialogValues.put( STEP_NAME, name );
				// dialogValues = dialog.show( dialogValues );
				// if( dialog.getReturnValue() != XFormDialog.OK_OPTION )
				// return null;

				return tuple.getValue2() == null ? createNewTestStep( tuple.getValue1(), name ) : createConfig( tuple
						.getValue2(), name );
			}
		}

		return null;
	}

	public TestStepConfig createNewTestStep( RestResource resource, String name )
	{
		RestRequestStepConfig requestStepConfig = RestRequestStepConfig.Factory.newInstance();
		RestMethodConfig testRequestConfig = requestStepConfig.addNewRestRequest();

		testRequestConfig.setName( name );
		testRequestConfig.setEncoding( "UTF-8" );

		if( resource != null )
		{
			requestStepConfig.setService( resource.getInterface().getName() );
			requestStepConfig.setResourcePath( resource.getFullPath() );

			String[] endpoints = resource.getInterface().getEndpoints();
			if( endpoints.length > 0 )
				testRequestConfig.setEndpoint( endpoints[0] );

			testRequestConfig.addNewRequest();
			RestParametersConfig parametersConfig = testRequestConfig.addNewParameters();

			for( XmlBeansRestParamsTestPropertyHolder.RestParamProperty property : resource.getDefaultParams() )
			{
				parametersConfig.addNewParameter().set( property.getConfig() );
			}
		}

		TestStepConfig testStepConfig = TestStepConfig.Factory.newInstance();
		testStepConfig.setType( RESTREQUEST_TYPE );
		testStepConfig.setConfig( requestStepConfig );
		testStepConfig.setName( name );

		return testStepConfig;
	}

	public boolean canCreate()
	{
		return true;
	}

	// private void buildDialog()
	// {
	// XFormDialogBuilder builder = XFormFactory.createDialogBuilder(
	// "Add REST Request to TestCase" );
	// XForm mainForm = builder.createForm( "Basic" );
	//
	// mainForm.addTextField( STEP_NAME, "Name of TestStep", XForm.FieldType.URL
	// ).setWidth( 30 );
	//
	// dialog = builder.buildDialog( builder.buildOkCancelActions(),
	// "Specify options for adding a new REST Request to a TestCase",
	// UISupport.OPTIONS_ICON );
	// }
}
