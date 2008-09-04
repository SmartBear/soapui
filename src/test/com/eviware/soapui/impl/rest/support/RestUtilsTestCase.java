package com.eviware.soapui.impl.rest.support;

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import junit.framework.TestCase;

import java.io.File;

public class RestUtilsTestCase extends TestCase
{
	public void testExtractTemplateParams() throws Exception
	{
		String path = "/{id}/test/{test}/test";
		
		String[] params = RestUtils.extractTemplateParams( path );
		assertEquals( params.length, 2 );
		assertEquals( "id", params[0] );
		assertEquals( "test", params[1] );
	}
	
	public void testImportWadl() throws Exception
	{
		WsdlProject project = new WsdlProject();
		RestService service = (RestService) project.addNewInterface("Test", RestServiceFactory.REST_TYPE );

      new WadlImporter( service ).initFromWadl(
				new File( "src" + File.separatorChar + "test-resources" + File.separatorChar + "wadl" + File.separatorChar + "YahooSearch.wadl"
            ).toURI().toURL().toString() );
		
	   assertEquals( 1, service.getOperationCount() );
	   assertEquals("/NewsSearchService/V1/", service.getBasePath());
		
	   RestResource resource = service.getOperationAt( 0 );
	   
	   assertEquals( 1, resource.getPropertyCount());
	   assertEquals("appid", resource.getPropertyAt(0).getName());
	   assertNotNull(resource.getProperty("appid"));
	   assertEquals( 1, resource.getRequestCount() );
	   
	   RestRequest request = resource.getRequestAt(0);
	   assertEquals( AbstractHttpRequest.RequestMethod.GET, request.getMethod() );
	   assertEquals( 9, request.getPropertyCount() );
	}
}
