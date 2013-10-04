package com.eviware.soapui.utils;

import com.eviware.soapui.config.*;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.rest.*;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.workspace.WorkspaceFactory;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.types.StringToStringMap;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: manne
 * Date: 9/10/13
 * Time: 8:45 AM
 * To change this template use File | Settings | File Templates.
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
		return new RestMethod( makeRestResource(), RestMethodConfig.Factory.newInstance());
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
