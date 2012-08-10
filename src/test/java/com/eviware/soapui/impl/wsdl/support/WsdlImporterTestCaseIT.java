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

package com.eviware.soapui.impl.wsdl.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.WsdlSubmitContext;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.support.TestCaseWithJetty;

// TODO Integration test. Move to it folder.
public class WsdlImporterTestCaseIT extends TestCaseWithJetty
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( WsdlImporterTestCaseIT.class );
	}

	@Test
	public void testOneWayOperationImport() throws Exception
	{
		WsdlProject project = new WsdlProject();
		WsdlInterface[] wsdls = WsdlImporter.importWsdl( project, "http://localhost:8082/testonewayop/TestService.wsdl" );

		assertEquals( 1, wsdls.length );

		WsdlInterface iface = wsdls[0];

		assertNotNull( iface );
		assertEquals( 2, iface.getOperationCount() );

		WsdlOperation operation = ( WsdlOperation )iface.getOperationAt( 0 );

		assertNotNull( operation );
		assertEquals( "GetDefaultPageData", operation.getName() );

		Definition definition = WsdlUtils.readDefinition( "http://localhost:8082/testonewayop/TestService.wsdl" );

		BindingOperation bindingOperation = operation.findBindingOperation( definition );
		assertNotNull( bindingOperation );
		assertEquals( bindingOperation.getName(), operation.getBindingOperationName() );

		assertNull( operation.getOutputName() );

		WsdlRequest request = operation.addNewRequest( "TestRequest" );
		assertNotNull( request );

		String requestXml = operation.createRequest( true );
		assertNotNull( requestXml );

		request.setRequestContent( requestXml );

		Submit submit = request.submit( new WsdlSubmitContext( null ), false );

		assertTrue( submit.getResponse().getContentAsString().indexOf( "Error 404 NOT_FOUND" ) > 0 );
	}
}
