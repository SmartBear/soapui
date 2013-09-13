package com.eviware.soapui.utils;

import com.eviware.soapui.config.RestMethodConfig;
import com.eviware.soapui.config.RestResourceConfig;
import com.eviware.soapui.config.RestServiceConfig;
import com.eviware.soapui.impl.WorkspaceImpl;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.wsdl.WsdlProject;
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
}
