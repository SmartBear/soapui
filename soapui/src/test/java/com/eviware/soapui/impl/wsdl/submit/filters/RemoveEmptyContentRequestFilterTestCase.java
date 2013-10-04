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

package com.eviware.soapui.impl.wsdl.submit.filters;

import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

public class RemoveEmptyContentRequestFilterTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( RemoveEmptyContentRequestFilterTestCase.class );
	}

	@Test
	public void testRemoval() throws Exception
	{
		assertEquals( doRemoval( "<test><testing/></test>" ), "<test/>" );
		assertEquals( doRemoval( "<test><testing test=\"\"/></test>" ), "<test/>" );

		assertEquals( doRemoval( "<test><testing>   </testing></test>" ), "<test/>" );
		assertEquals( doRemoval( "<test><testing>  <testar test=\"\"></testar> </testing></test>" ),
				"<test><testing>   </testing></test>" );

		assertEquals( doRemoval( "<test><testing>\n   <testar test=\"\"></testar>\n </testing></test>" ),
				"<test><testing>\n   \n </testing></test>" );

		assertEquals( doRemoval( "<test></test>" ), "<test></test>" );

		assertEquals( doRemoval( "<test><testing/><testing/></test>" ), "<test/>" );

		assertEquals(
				doRemoval( "<dat1:documentType xmlns:dat1=\"test\"><dat1:listName test=\"\" xsi:nil=\"true\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/></dat1:documentType>" ),
				"<dat1:documentType xmlns:dat1=\"test\"/>" );

	}

	private String doRemoval( String request ) throws Exception
	{
		return RemoveEmptyContentRequestFilter.removeEmptyContent( request, null, true );
	}
}
