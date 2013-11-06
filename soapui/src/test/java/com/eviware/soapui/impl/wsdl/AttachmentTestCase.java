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

package com.eviware.soapui.impl.wsdl;

import static org.junit.Assert.assertEquals;

import java.io.File;

import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

import com.eviware.soapui.impl.WsdlInterfaceFactory;

public class AttachmentTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( AttachmentTestCase.class );
	}

	@Test
	public void shouldHaveAttachments() throws Exception
	{

		String wsdlUrl = AttachmentTestCase.class.getResource( "/attachment-test.wsdl" ).toString();
		WsdlProject project = new WsdlProject();
		WsdlInterface iface = WsdlInterfaceFactory.importWsdl( project, wsdlUrl, false )[0];

		WsdlOperation operation = iface.getOperationByName( "SendClaim" );
		WsdlRequest request = operation.addNewRequest( "Test" );

		request.setRequestContent( operation.createRequest( true ) );

		System.out.println( request.getRequestContent() );

		HttpAttachmentPart[] definedAttachmentParts = request.getDefinedAttachmentParts();

		assertEquals( definedAttachmentParts.length, 4 );
		assertEquals( definedAttachmentParts[0].getName(), "ClaimPhoto" );
	}
}
