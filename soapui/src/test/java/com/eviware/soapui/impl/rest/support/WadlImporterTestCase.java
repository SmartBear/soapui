/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.support;

import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;

import static com.eviware.soapui.utils.CommonMatchers.anEmptyString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class WadlImporterTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( WadlImporterTestCase.class );
	}

	@Test
	public void testWadlImporter() throws Exception
	{
		WsdlProject project = new WsdlProject();
		RestService service = ( RestService )project.addNewInterface( "REST Service", RestServiceFactory.REST_TYPE );
		WadlImporter importer = new WadlImporter( service );
		importer.initFromWadl( WadlImporter.class.getResource( "/wadl/YahooSearch.wadl" ).toString() );
		assertEquals( service.getName(), "REST Service" );
		assertEquals( 1, service.getResourceList().size() );
		assertEquals( 0, service.getResourceList().get( 0 ).getChildResourceCount() );
		assertEquals( 1, service.getResourceList().get( 0 ).getRestMethodCount() );
	}

	@Test
	public void importsWadl() throws Exception
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

	@Test
	public void removesPropertyExpansions() throws Exception
	{
		WsdlProject project = new WsdlProject();
		RestService service = ( RestService )project.addNewInterface( "Test", RestServiceFactory.REST_TYPE );

		new WadlImporter( service ).initFromWadl( RestUtilsTestCase.class.getResource(
				"/wadl/YahooSearchWithExpansions.wadl" ).toURI().toString());
		RestResource operation = ( RestResource )service.getAllOperations()[0];
		RestMethod restMethod = operation.getRestMethodAt( 0 );
		RestRequest request = restMethod.getRequestAt( 0 );
		assertThat( request.getParams().getProperty( "language" ).getDefaultValue(), is( anEmptyString() ) );
	}
}
