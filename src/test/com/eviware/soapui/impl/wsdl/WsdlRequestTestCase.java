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


package com.eviware.soapui.impl.wsdl;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.support.TestCaseWithJetty;

public class WsdlRequestTestCase extends TestCaseWithJetty
{
	public void testRequest() throws Exception
	{
		//	 create new project
		WsdlProject project = new WsdlProject();

		// import amazon wsdl
		WsdlInterface iface = WsdlInterfaceFactory.importWsdl( project, "http://127.0.0.1:8082/test1/TestService.wsdl", true )[0];

		// get "Help" operation
		WsdlOperation operation = (WsdlOperation) iface.getOperationByName( "GetPage" );

		// create a new empty request for that operation
		WsdlRequest request = operation.addNewRequest( "My request" );

		// generate the request content from the schema
		request.setRequestContent( operation.createRequest( true ) );

		// submit the request
		WsdlSubmit submit = (WsdlSubmit) request.submit( new WsdlSubmitContext( request ), false );

		// wait for the response
		Response response = submit.getResponse();

		//	print the response
//		String content = response.getContentAsString();
//		System.out.println( content );
//		assertNotNull( content );
	}
	/*
	public void testMemory() throws Exception
	{
		try
		{
//			create new project
			for( int c = 0; c < 100; c++ )
			{
				String url = "http://localhost:8082/soapui-tests/test1/TestService.wsdl";
//				WsdlContext context = new WsdlContext( url, SoapVersion.Soap11, null, null );
//				context.load();
				
				WsdlProject project = new WsdlProject();
//				WsdlInterface iface = WsdlImporter.getInstance().importWsdl( project, url )[0];
//				project.removeInterface( iface );
				
				// import amazon wsdl
			   project.importWsdl( url, false );
		//	   project.release();
		//	   project.removeInterface( iface );
		//	   project.release();
				
				//		 get "Help" operation
//			WsdlOperation operation = (WsdlOperation) iface.getOperationByName( "GetPage" );
				
				//		 create a new empty request for that operation
//			WsdlRequest request = operation.addNewRequest( "My request" );	
				
				System.out.println( "run " + c );
			}
			
			assertTrue( true );
		} 
		catch (RuntimeException e)
		{
			UISupport.logError( e );
			assertTrue( false );
		}
	}*/
}
