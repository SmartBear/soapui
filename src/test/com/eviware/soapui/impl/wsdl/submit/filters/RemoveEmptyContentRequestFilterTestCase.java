/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.submit.filters;

import junit.framework.TestCase;

public class RemoveEmptyContentRequestFilterTestCase extends TestCase
{
	public void testRemoval() throws Exception
	{
		doRemoval( "<test><testing/></test>", "<test/>" );
		doRemoval( "<test><testing test=\"\"/></test>", "<test/>" );
		
		doRemoval( "<test><testing>   </testing></test>", "<test/>" );
		doRemoval( "<test><testing>  <testar test=\"\"></testar> </testing></test>", 
					"<test><testing>   </testing></test>" );
		
		doRemoval( "<test><testing>\n   <testar test=\"\"></testar>\n </testing></test>", 
		"<test><testing>\n   \n </testing></test>" );
		
		doRemoval( "<test></test>", "<test></test>" );
		
		doRemoval( "<test><testing/><testing/></test>", "<test/>" );
	}
	
	public void doRemoval( String request, String expected ) throws Exception
	{
		assertEquals( expected, RemoveEmptyContentRequestFilter.removeEmptyContent(request, null) );
	}
}
