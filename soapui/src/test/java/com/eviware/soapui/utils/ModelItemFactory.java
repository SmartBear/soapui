package com.eviware.soapui.utils;

import com.eviware.soapui.config.OperationConfig;
import com.eviware.soapui.config.RestMethodConfig;
import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.RestResourceConfig;
import com.eviware.soapui.config.RestServiceConfig;
import com.eviware.soapui.config.TestCaseConfig;
import com.eviware.soapui.config.TestSuiteConfig;
import com.eviware.soapui.config.WsdlInterfaceConfig;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.workspace.WorkspaceFactory;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Class containing factory methods for commonly used model items, for use in automatic tests.
 */
public class ModelItemFactory
{
	public static RestRequest makeRestRequest() throws SoapUIException
	{
			return new RestRequest( makeRestMethod(), RestRequestConfig.Factory.newInstance(), false);
	}

	public static RestRequest makeRestRequest(RestResource restResource) throws SoapUIException
	{
		return new RestRequest( makeRestMethod(restResource), RestRequestConfig.Factory.newInstance(), false);
	}

	private static RestMethod makeRestMethod( RestResource restResource )
	{
		return new RestMethod( restResource, RestMethodConfig.Factory.newInstance());
	}

	public static RestMethod makeRestMethod() throws SoapUIException
	{
		return new RestMethod( makeRestResource(), RestMethodConfig.Factory.newInstance());
	}

	public static RestResource makeRestResource() throws SoapUIException
	{
		return new RestResource( makeRestService(), RestResourceConfig.Factory.newInstance());
	}

	public static RestService makeRestService() throws SoapUIException
	{
		return new RestService(makeWsdlProject(), RestServiceConfig.Factory.newInstance());
	}

	public static WsdlProject makeWsdlProject() throws SoapUIException
	{
		return new WsdlProject( (WorkspaceImpl )WorkspaceFactory.getInstance().openWorkspace( "testWorkSpace" , new StringToStringMap()));
	}

	public static WsdlTestCase makeTestCase() throws SoapUIException
	{
		return new WsdlTestCase( new WsdlTestSuite( makeWsdlProject(), TestSuiteConfig.Factory.newInstance() ), TestCaseConfig.Factory.newInstance(), false );
	}

	public static WsdlOperation makeWsdlOperation() throws SoapUIException
	{
		return new WsdlOperation( makeWsdlInterface(), OperationConfig.Factory.newInstance() );
	}

	private static WsdlInterface makeWsdlInterface() throws SoapUIException
	{
		return new WsdlInterface( makeWsdlProject(), WsdlInterfaceConfig.Factory.newInstance() );
	}
}
