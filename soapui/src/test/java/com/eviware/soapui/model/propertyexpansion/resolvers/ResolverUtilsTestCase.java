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

package com.eviware.soapui.model.propertyexpansion.resolvers;

import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

import org.junit.Test;

public class ResolverUtilsTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( ResolverUtilsTestCase.class );
	}

	@Test
	public void testExtractXPathPropertyValue() throws Exception
	{
		assertEquals( "audi", ResolverUtils.extractXPathPropertyValue( "<test><bil>audi</bil></test>", "//bil" ) );
		assertEquals( "<test><bil>audi</bil><bil>bmw</bil></test>",
				ResolverUtils.extractXPathPropertyValue( "<test><bil>audi</bil><bil>bmw</bil></test>", "//test" ) );
		assertEquals( "audi",
				ResolverUtils.extractXPathPropertyValue( "<test><bil>audi</bil><bil>bmw</bil></test>", "//test/bil[1]" ) );
	}
}
