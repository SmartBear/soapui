package com.eviware.soapui.impl.rest;

import junit.framework.TestCase;

import com.eviware.soapui.impl.wsdl.WsdlProject;

public class RestResourceTestCase extends TestCase
{
	public void testGetTemplateParams() throws Exception
	{
		WsdlProject project = new WsdlProject();
		RestService restService = (RestService) project.addNewInterface("Test", RestServiceFactory.REST_TYPE );
		RestResource resource = restService.addNewResource("Resource", "/test" );
		
		assertEquals( resource.getDefaultParams().length, 0 );
		
		resource.setPath( "/{id}/test" );
		assertEquals( resource.getDefaultParams().length, 1 );
		assertEquals( "id", resource.getDefaultParams()[0].getName() );
		assertEquals( "/{id}/test", resource.getFullPath() );
		
		RestResource subResource = resource.addNewResource("Child", "{test}/test" );
		assertEquals( "/{id}/test/{test}/test", subResource.getFullPath() );
	}
}
