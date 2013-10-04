package com.eviware.soapui.utils;

import com.eviware.soapui.config.*;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlTestSuite;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.workspace.WorkspaceFactory;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Created with IntelliJ IDEA.
 * User: manne
 * Date: 9/10/13
 * Time: 8:45 AM
 * To change this template use File | Settings | File Templates.
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
		return new RestResource(new RestService(makeWsdlProject(), RestServiceConfig.Factory.newInstance()), RestResourceConfig.Factory.newInstance());
	}

	public static WsdlProject makeWsdlProject() throws SoapUIException
	{
		return new WsdlProject( (WorkspaceImpl )WorkspaceFactory.getInstance().openWorkspace( "testWorkSpace" , new StringToStringMap()));
	}

	public static WsdlTestCase makeTestCase() throws SoapUIException
	{
		return new WsdlTestCase( new WsdlTestSuite( makeWsdlProject(), TestSuiteConfig.Factory.newInstance() ), TestCaseConfig.Factory.newInstance(), false );
	}
}
