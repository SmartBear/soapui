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
 * @author : Shadid Chowdhury
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
	public void encodedParamURITest() throws URISyntaxException
	{
		String uri = "http://service.com/api/1.2/json/search/search?title=Kill%20me";
		String expectedEndpoint = "http://service.com";
		String expectedPath = "/api/1.2/json/search/search";
		String expectedResourceName = "Search";
		String expectedQuery = "title=Kill me";

		restURIParser = new RestURIParserImpl( uri );

		assertEquals( expectedEndpoint, restURIParser.getEndpoint() );
		assertEquals( expectedPath, restURIParser.getPath() );
		assertEquals( expectedResourceName, restURIParser.getResourceName() );
		assertEquals( expectedQuery, restURIParser.getQuery() );
	}

	@Test
	//FIXME: Fix implementation to allow decoded characters in URI. Maybe try encoding them first?
	public void decodedParamURITest() throws URISyntaxException
	{
		String uri = "http://service.com/api/1.2/json/search/search?title=Kill me";
		String expectedEndpoint = "http://service.com";
		String expectedPath = "/api/1.2/json/search/search";
		String expectedResourceName = "Search";
		String expectedQuery = "title=Kill me";

		restURIParser = new RestURIParserImpl( uri );

		assertEquals( expectedEndpoint, restURIParser.getEndpoint() );
		assertEquals( expectedPath, restURIParser.getPath() );
		assertEquals( expectedResourceName, restURIParser.getResourceName() );
		assertEquals( expectedQuery, restURIParser.getQuery() );
	}

	@Test
	public void noParameterTest() throws URISyntaxException
	{
		String uri = "http://service.com/rest/";
		String expectedEndpoint = "http://service.com";
		String expectedPath = "/rest";
		String expectedResourceName = "Rest";
		String expectedQuery = "";

		restURIParser = new RestURIParserImpl( uri );

		assertEquals( expectedEndpoint, restURIParser.getEndpoint() );
		assertEquals( expectedPath, restURIParser.getPath() );
		assertEquals( expectedResourceName, restURIParser.getResourceName() );
		assertEquals( expectedQuery, "");
	}

	@Test
	public void noEndpointTest() throws URISyntaxException
	{
		String uri = "/abc?book=15;column=12";
		String expectedEndpoint = "";
		String expectedPath = "/abc";
		String expectedResourceName = "Abc";
		String expectedQuery = "book=15;column=12";

		restURIParser = new RestURIParserImpl( uri );

		assertEquals( expectedEndpoint, restURIParser.getEndpoint() );
		assertEquals( expectedPath, restURIParser.getPath() );
		assertEquals( expectedResourceName, restURIParser.getResourceName());
		assertEquals( expectedQuery, restURIParser.getQuery() );
	}

	@Test
	//FIXME: Fix implementation to add slash to start of resource path
	public void noEndpointNorPrefixSlashTest() throws URISyntaxException
	{
		String uri = "1.2/json.search/search?title=Kill%20me";
		String expectedEndpoint = "";
		String expectedPath = "/1.2/json.search/search";
		String expectedResourceName = "Search";
		String expectedQuery = "title=Kill me";

		restURIParser = new RestURIParserImpl( uri );

		assertEquals( expectedEndpoint, restURIParser.getEndpoint() );
		assertEquals( expectedPath, restURIParser.getPath() );
		assertEquals( expectedResourceName, restURIParser.getResourceName());
		assertEquals( expectedQuery, restURIParser.getQuery() );
	}

	public void parametereizedURITest() throws URISyntaxException
	{
		String uri = "/conversation/date/{date}/time/{time}/?userId=1234";
		String expectedEndpoint = "";
		String expectedPath = "/conversation/date/{date}/time/{time}";
		String expectedResourceName = "Time";
		String expectedQuery = "userId=1234";

		restURIParser = new RestURIParserImpl( uri );

		assertEquals( expectedEndpoint, restURIParser.getEndpoint() );
		assertEquals( expectedPath, restURIParser.getPath() );
		assertEquals( expectedResourceName, restURIParser.getResourceName());
		assertEquals( expectedQuery, restURIParser.getQuery() );
	}
}
