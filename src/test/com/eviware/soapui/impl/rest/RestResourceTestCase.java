/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import junit.framework.TestCase;

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
		
		RestResource subResource = resource.addNewChildResource("Child", "{test}/test" );
		assertEquals( "/{id}/test/{test}/test", subResource.getFullPath() );
	}
}
