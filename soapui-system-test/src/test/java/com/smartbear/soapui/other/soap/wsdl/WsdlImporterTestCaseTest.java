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

package com.smartbear.soapui.other.soap.wsdl;

import com.eviware.soapui.impl.wsdl.*;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.model.iface.Submit;
import com.smartbear.soapui.utils.IntegrationTest;
import com.smartbear.soapui.utils.jetty.JettyTestCaseBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

@Category( IntegrationTest.class )
public class WsdlImporterTestCaseTest extends JettyTestCaseBase
{

	//TODO: Should be split up into several smaller methods
	@Test
	public void testOneWayOperationImport() throws Exception
	{
		replaceInFile( "wsdls/testonewayop/TestService.wsdl","8082","" + getPort());

		WsdlProject project = new WsdlProject();
		WsdlInterface[] wsdls = WsdlImporter.importWsdl( project, "http://localhost:" + getPort() + "/wsdls/testonewayop/TestService.wsdl" );

		assertThat( wsdls.length, is( 1 ) );

		WsdlInterface iface = wsdls[0];

		assertThat( iface, is(notNullValue()) );
		assertThat(iface.getOperationCount(), is(2) );

		WsdlOperation operation = iface.getOperationAt( 0 );

		assertThat( operation, is( notNullValue() ) );
		assertThat( operation.getName(), is( "GetDefaultPageData" ) );

		Definition definition = WsdlUtils.readDefinition( "http://localhost:" + getPort() + "/wsdls/testonewayop/TestService.wsdl" );

		BindingOperation bindingOperation = operation.findBindingOperation( definition );
		assertThat( bindingOperation, is( notNullValue() ) );
		assertThat( operation.getBindingOperationName(), is(bindingOperation.getName()) );

		assertThat( operation.getOutputName(), is( nullValue() ) );

		WsdlRequest request = operation.addNewRequest( "TestRequest" );
		assertThat( request, is(notNullValue()) );

		String requestXml = operation.createRequest( true );
		assertThat( requestXml, is(notNullValue()) );

		request.setRequestContent( requestXml );

		Submit submit = request.submit( new WsdlSubmitContext( null ), false );

		assertThat( submit.getResponse().getContentAsString(), containsString( "Error 404 NOT_FOUND" ));
	}
}
