/*
 * soapUI, copyright (C) 2004-2013 smartbear.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 *
 */
package com.eviware.soapui.impl.rest.support;

import junit.framework.JUnit4TestAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * Tests RestURIParserImpl
 * Author: Shadid Chowdhury
 */
public class RestURIParserImplTestCase
{


	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter(
				RestURIParserImplTestCase.class );
	}

	RestURIParserImpl restURIParser;

	@Before
	public void setUp() throws URISyntaxException
	{
	}

	@After
	public void tearDown()
	{
		restURIParser = null;
	}

	@Test
	public void encodedParamURITest() throws Exception
	{
		String uri = "http://service.com/api/1.2/json/search/search?title=Kill%20me";
		String expectedEndpoint = "http://service.com";
		String expectedPath = "/api/1.2/json/search/search";
		String expectedParams = "title=Kill me";

		restURIParser = new RestURIParserImpl( uri );

		assertEquals( expectedEndpoint, restURIParser.getEndpoint() );
		assertEquals( expectedPath, restURIParser.getPath() );
		assertEquals( expectedParams, restURIParser.getParams() );
	}


}
