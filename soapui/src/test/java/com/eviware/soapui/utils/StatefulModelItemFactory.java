package com.eviware.soapui.utils;

import com.eviware.soapui.config.RestMethodConfig;
import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.TestCaseConfig;
import com.eviware.soapui.config.TestSuiteConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.support.SoapUIException;

import java.util.UUID;

/**
 * @author manne
 */
public class StatefulModelItemFactory
{
	private WsdlProject project;

	public StatefulModelItemFactory()
	{
		try
		{
			project =  ModelItemFactory.makeWsdlProject();
		}
		catch( SoapUIException e )
		{
			throw new RuntimeException( "Unexpected", e );
		}
	}

	public RestRequest makeRestRequest() throws SoapUIException
	{
			return new RestRequest( makeRestMethod(), RestRequestConfig.Factory.newInstance(), false);
	}

	public RestRequest makeRestRequest(RestResource restResource) throws SoapUIException
	{
		return new RestRequest( makeRestMethod(restResource), RestRequestConfig.Factory.newInstance(), false);
	}

	private RestMethod makeRestMethod( RestResource restResource )
	{
		return new RestMethod( restResource, RestMethodConfig.Factory.newInstance());
	}

	public RestMethod makeRestMethod() throws SoapUIException
	{
		RestMethodConfig methodConfig = RestMethodConfig.Factory.newInstance();
		methodConfig.setName( "Get" );
		methodConfig.setMethod( "GET" );
		final RestResource restResource = makeRestResource();
		RestMethod restMethod = new RestMethod( restResource, methodConfig )
		{
			@Override
			public RestRequestInterface.HttpMethod getMethod()
			{
				return RestRequestInterface.HttpMethod.GET;
			}

			@Override
			public RestResource getOperation()
			{
				return restResource;
			}
		};
		restResource.getConfig().setMethodArray( new RestMethodConfig[]{ restMethod.getConfig()} );
		return restMethod;
	}

	public RestResource makeRestResource() throws SoapUIException
	{
		String serviceName = "Interface_" + UUID.randomUUID().toString();
		RestService service = (RestService)project.addNewInterface( serviceName, RestServiceFactory.REST_TYPE );
		service.setName( serviceName );
		RestResource restResource = service.addNewResource( "root", "/" );
		return restResource;
	}



	public WsdlTestCase makeTestCase() throws SoapUIException
	{
		return new WsdlTestCase( new WsdlTestSuite( project, TestSuiteConfig.Factory.newInstance() ), TestCaseConfig.Factory.newInstance(), false );
	}
}
