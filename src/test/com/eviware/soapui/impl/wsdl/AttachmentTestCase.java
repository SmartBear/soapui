/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
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
		String wsdlUrl = new File( "src/test-resources/attachment-test.wsdl" ).toURI().toURL().toString();
		WsdlProject project = new WsdlProject();
		WsdlInterface iface = WsdlInterfaceFactory.importWsdl( project, wsdlUrl, false )[0];

		WsdlOperation operation = ( WsdlOperation )iface.getOperationByName( "SendClaim" );
		WsdlRequest request = operation.addNewRequest( "Test" );

		request.setRequestContent( operation.createRequest( true ) );

		System.out.println( request.getRequestContent() );

		HttpAttachmentPart[] definedAttachmentParts = request.getDefinedAttachmentParts();

		assertEquals( definedAttachmentParts.length, 4 );
		assertEquals( definedAttachmentParts[0].getName(), "ClaimPhoto" );

		/*
		 * XmlCursor cursor = xmlObject.newCursor(); //xmlObject.changeType(
		 * docType ).newCursor(); while( !cursor.isEnddoc() ) { if(
		 * cursor.isContainer() ) { String attributeText =
		 * cursor.getAttributeText( new QName(
		 * "http://www.w3.org/2004/11/xmlmime", "contentType")); if( attributeText
		 * != null ) System.out.println( "contentType: " + attributeText);
		 * 
		 * SchemaType schemaType = cursor.getObject().schemaType(); if( schemaType
		 * != null && schemaType.getName().equals( new
		 * QName("http://ws-i.org/profiles/basic/1.1/xsd","swaRef")) ) {
		 * System.out.println( cursor.getTextValue() ); } }
		 * 
		 * cursor.toNextToken();
		 * 
		 * }
		 */
	}
}
