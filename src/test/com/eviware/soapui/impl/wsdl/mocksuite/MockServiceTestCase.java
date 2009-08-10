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

package com.eviware.soapui.impl.wsdl.mocksuite;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.TestCaseWithJetty;

public class MockServiceTestCase extends TestCaseWithJetty
{
	public void testMockOperation() throws Exception
	{
		WsdlProject project = new WsdlProject();
		WsdlInterface iface = WsdlInterfaceFactory.importWsdl( project, "http://localhost:8082/test8/TestService.wsdl", true )[0];
		
		WsdlMockService mockService = ( WsdlMockService ) project.addNewMockService( "MockService 1" );
		
		mockService.setPort( 9081 );
		mockService.setPath( "/testmock" );
		
		WsdlOperation operation = ( WsdlOperation ) iface.getOperationAt( 0 );
		WsdlMockOperation mockOperation = ( WsdlMockOperation ) mockService.addNewMockOperation( operation );
		WsdlMockResponse mockResponse = mockOperation.addNewMockResponse( "Test Response", true );
		mockResponse.setResponseContent( "Tjohoo!" );
		
		mockService.start();
		
		iface.addEndpoint( "/testmock" );
		WsdlRequest request = ( WsdlRequest ) operation.getRequestAt(  0 );
		
		request.setEndpoint( "http://localhost:9081/testmock" );
		Response response = request.submit( new WsdlSubmitContext(null), false ).getResponse();
		
		assertEquals( response.getContentAsString(), mockResponse.getResponseContent() );
	}
}
