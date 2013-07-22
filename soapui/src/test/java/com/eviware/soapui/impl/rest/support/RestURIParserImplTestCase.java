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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * Tests RestURIParserImpl
 * Author: Shadid Chowdhury
 */
@RunWith( JUnit4.class )
public class RestURIParserImplTestCase
{
	@Rule
	public ExpectedException exception = ExpectedException.none();

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter(
				RestURIParserImplTestCase.class );
	}

	@Test
	public void encodedParamURITest() throws URISyntaxException
	{
		String uri = "http://service.com/api/1.2/json/search/search?title=Kill%20me";
		String expectedEndpoint = "http://service.com";
		String expectedPath = "/api/1.2/json/search/search";
		String expectedResourceName = "Search";
		String expectedQuery = "title=Kill me";

		assertURIParsedCorrectly( uri, expectedEndpoint, expectedPath, expectedResourceName, expectedQuery );
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

		assertURIParsedCorrectly( uri, expectedEndpoint, expectedPath, expectedResourceName, expectedQuery );
	}

	@Test
	public void noParameterTest() throws URISyntaxException
	{
		String uri = "http://service.com/rest/";
		String expectedEndpoint = "http://service.com";
		String expectedPath = "/rest";
		String expectedResourceName = "Rest";
		String expectedQuery = "";

		assertURIParsedCorrectly( uri, expectedEndpoint, expectedPath, expectedResourceName, expectedQuery );
	}

	@Test
	public void noEndpointTest() throws URISyntaxException
	{
		String uri = "/abc?book=15;column=12";
		String expectedEndpoint = "";
		String expectedPath = "/abc";
		String expectedResourceName = "Abc";
		String expectedQuery = "book=15;column=12";

		assertURIParsedCorrectly( uri, expectedEndpoint, expectedPath, expectedResourceName, expectedQuery );
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

		assertURIParsedCorrectly( uri, expectedEndpoint, expectedPath, expectedResourceName, expectedQuery );
	}

	@Test
	public void parametereizedURITest() throws URISyntaxException
	{
		String uri = "/conversation/date/{date}/time/{time}/?userId=1234";
		String expectedEndpoint = "";
		String expectedPath = "/conversation/date/{date}/time/{time}";
		String expectedResourceName = "Time";
		String expectedQuery = "userId=1234";

		assertURIParsedCorrectly( uri, expectedEndpoint, expectedPath, expectedResourceName, expectedQuery );
	}

	@Test
	public void numbersInResourcePathTest() throws URISyntaxException
	{
		String uri = "http://bokus.se/books/ISBN-5012359";
		String expectedEndpoint = "http://bokus.se";
		String expectedPath = "/books/ISBN-5012359";
		String expectedResourceName = "ISBN-5012359";
		String expectedQuery = "";

		assertURIParsedCorrectly( uri, expectedEndpoint, expectedPath, expectedResourceName, expectedQuery );
	}

	@Test
	public void httpPrefixAddedWhenOmittedTest() throws URISyntaxException
	{
		String uri = "spotify.com";
		String expectedEndpoint = "http://spotify.com";
		String expectedPath = "";
		String expectedResourceName = "";
		String expectedQuery = "";

		assertURIParsedCorrectly( uri, expectedEndpoint, expectedPath, expectedResourceName, expectedQuery );
	}

	@Test
	public void invalidSchemeIsRejectedTest() throws URISyntaxException
	{
		String uri = "ftp://spotify.com/api/?userId=1234";

		exception.expect( InvalidURISchemeException.class );
		RestURIParserImpl restURIParser = new RestURIParserImpl( uri );
	}

	private static void assertURIParsedCorrectly( String uri,
																 String expectedEndpoint,
																 String expectedPath,
																 String expectedResourceName,
																 String expectedQuery ) throws URISyntaxException
	{
		RestURIParserImpl restURIParser = new RestURIParserImpl( uri );

		assertEquals( expectedEndpoint, restURIParser.getEndpoint() );
		assertEquals( expectedPath, restURIParser.getPath() );
		assertEquals( expectedResourceName, restURIParser.getResourceName() );
		assertEquals( expectedQuery, restURIParser.getQuery() );
	}
}
