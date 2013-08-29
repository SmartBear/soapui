/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;

public class RestUtilsTestCase
{

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( RestUtilsTestCase.class );
	}

	@Test
	public void shouldExtractTemplateParams() throws Exception
	{
		String path = "/{id}/test/{test}/test";

		String[] params = RestUtils.extractTemplateParams( path );
		assertEquals( params.length, 2 );
		assertEquals( "id", params[0] );
		assertEquals( "test", params[1] );
	}

	@Test
	public void shouldImportWadl() throws Exception
	{
		WsdlProject project = new WsdlProject();
		RestService service = ( RestService )project.addNewInterface( "Test", RestServiceFactory.REST_TYPE );

		new WadlImporter( service ).initFromWadl( RestUtilsTestCase.class.getResource(  "/wadl/YahooSearch.wadl" ).toURI().toString());

		assertEquals( 1, service.getOperationCount() );
		assertEquals( "/NewsSearchService/V1/", service.getBasePath() );

		RestResource resource = service.getOperationAt( 0 );

		assertEquals( 1, resource.getPropertyCount() );
		assertEquals( "appid", resource.getPropertyAt( 0 ).getName() );
		assertNotNull( resource.getProperty( "appid" ) );
		assertEquals( 1, resource.getRequestCount() );

		RestRequest request = resource.getRequestAt( 0 );
		assertEquals( RestRequestInterface.RequestMethod.GET, request.getMethod() );
		assertEquals( 9, request.getPropertyCount() );
	}
}
