/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com 
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.teststeps;

import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.RestRequestStepConfig;
import com.eviware.soapui.config.TestStepConfig;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.impl.wsdl.teststeps.registry.RestRequestStepFactory;
import com.eviware.soapui.support.SoapUIException;
import org.apache.xmlbeans.XmlException;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Joel
 */
public class RestTestRequestStepTest
{

	public static final String INTERFACE_NAME = "My Interface";
	public static final String RESOURCE_NAME = "My Resource";
	public static final String METHOD_NAME = "My Method";
	public static final String PATH = "/";

	@Test
	public void resourceIsFoundEvenThoughMultipleInterfacesWithDuplicateNameExists() throws RestRequestStepFactory.ItemDeletedException, XmlException, IOException, SoapUIException
	{
		WsdlTestCase testCase = Mockito.mock( WsdlTestCase.class );
		WsdlProject project = new WsdlProject();
		RestService restService1 = ( RestService )project
				.addNewInterface( INTERFACE_NAME, RestServiceFactory.REST_TYPE );
		RestService restService2 = ( RestService )project
				.addNewInterface( INTERFACE_NAME, RestServiceFactory.REST_TYPE );
		RestResource restResource = restService2.addNewResource( RESOURCE_NAME, PATH );
		restResource.addNewMethod( METHOD_NAME );
		Mockito.when( testCase.getParent() ).thenReturn( project );

		TestStepConfig config = TestStepConfig.Factory.newInstance();
		RestRequestStepConfig configConfig = ( RestRequestStepConfig )config.addNewConfig().changeType( RestRequestStepConfig.type );
		configConfig.setService( INTERFACE_NAME );
		configConfig.setResourcePath( PATH );
		configConfig.setMethodName( METHOD_NAME );
		configConfig.setRestRequest( RestRequestConfig.Factory.newInstance() );

		RestTestRequestStep step = new RestTestRequestStep( testCase, config, false );
		RestResource foundResource = step.getResource();
		assertThat( foundResource, is( sameInstance( restResource ) ) );
	}
}
