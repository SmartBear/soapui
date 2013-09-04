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

import com.eviware.soapui.config.RestParametersConfig;
import junit.framework.JUnit4TestAdapter;
import org.junit.After;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * Tests RestURIParserImpl
 *
 * @author Shadid Chowdhury
 */
public class RestURIParserImplTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter(
				RestURIParserImplTestCase.class );
	}

	private RestURIParserImpl restURIParser;

	@After
	public void tearDown()
	{
		restURIParser = null;
	}

	@Test
	public void encodedParamURITest() throws MalformedURLException
	{
		String uri = "http://service.com/api/1.2/json/search/search?title=Kill%20me";
		String expectedEndpoint = "http://service.com";
		String expectedPath = "/api/1.2/json/search/search";
		String expectedResourceName = "Search";
		String expectedQuery = "title=Kill me";

		restURIParser = new RestURIParserImpl( uri );

		assertURIParsedCorrectly( expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser );
	}

	@Test
	public void decodedParamURITest() throws MalformedURLException
	{
		String uri = "http://service.com/api/1.2/json/search/search?title=Kill me";
		String expectedEndpoint = "http://service.com";
		String expectedPath = "/api/1.2/json/search/search";
		String expectedResourceName = "Search";
		String expectedQuery = "title=Kill me";

		restURIParser = new RestURIParserImpl( uri );

		assertURIParsedCorrectly( expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser );
	}

	@Test
	public void noParameterTest() throws MalformedURLException
	{
		String uri = "http://service.com/rest/";
		String expectedEndpoint = "http://service.com";
		String expectedPath = "/rest";
		String expectedResourceName = "Rest";
		String expectedQuery = "";

		restURIParser = new RestURIParserImpl( uri );

		assertURIParsedCorrectly( expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser );
	}

	@Test
	public void noEndpointTest() throws MalformedURLException
	{
		String uri = "/abc?book=15;column=12";
		String expectedEndpoint = "";
		String expectedPath = "/abc";
		String expectedResourceName = "Abc";
		String expectedQuery = "book=15;column=12";

		restURIParser = new RestURIParserImpl( uri );

		assertURIParsedCorrectly( expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser );
	}

	@Test
	public void noEndpointNorPrefixSlashTest() throws MalformedURLException
	{
		String uri = "1.2/json.search/search?title=Kill%20me";
		String expectedEndpoint = "";
		String expectedPath = "/1.2/json.search/search";
		String expectedResourceName = "Search";
		String expectedQuery = "title=Kill me";

		restURIParser = new RestURIParserImpl( uri );

		assertURIParsedCorrectly( expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser );
	}

	@Test
	public void parametereizedURITest() throws MalformedURLException
	{
		String uri = "/conversation/date/{date}/time/{time}/?userId=1234";
		String expectedEndpoint = "";
		String expectedPath = "/conversation/date/{date}/time/{time}";
		String expectedResourceName = "Time";
		String expectedQuery = "userId=1234";

		restURIParser = new RestURIParserImpl( uri );

		assertURIParsedCorrectly( expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser );
	}

	@Test
	public void parametereizedURIWithEndpointTest() throws MalformedURLException
	{
		String uri = "/conversation/{date}";
		String expectedEndpoint = "";
		String expectedPath = "/conversation/{date}";
		String expectedResourceName = "Date";
		String expectedQuery = "";

		restURIParser = new RestURIParserImpl( uri );

		assertURIParsedCorrectly( expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser );
	}

	@Test
	public void parametereizedFullURITest() throws MalformedURLException
	{
		String uri = "http://servo.com/conversation/date/{date}/time/{time}/?userId=1234";
		String expectedEndpoint = "http://servo.com";
		String expectedPath = "/conversation/date/{date}/time/{time}";
		String expectedResourceName = "Time";
		String expectedQuery = "userId=1234";

		restURIParser = new RestURIParserImpl( uri );

		assertURIParsedCorrectly( expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser );
	}

	@Test
	public void numbersInResourcePathTest() throws MalformedURLException
	{
		String uri = "http://bokus.se/books/ISBN-5012359";
		String expectedEndpoint = "http://bokus.se";
		String expectedPath = "/books/ISBN-5012359";
		String expectedResourceName = "ISBN-5012359";
		String expectedQuery = "";

		restURIParser = new RestURIParserImpl( uri );

		assertURIParsedCorrectly( expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser );
	}

	@Test
	public void httpPrefixAddedWhenOmittedTest() throws MalformedURLException
	{
		String uri = "soapui.com";
		String expectedEndpoint = "http://soapui.com";
		String expectedPath = "";
		String expectedResourceName = "";
		String expectedQuery = "";

		restURIParser = new RestURIParserImpl( uri );

		assertURIParsedCorrectly( expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser );
	}

	@Test
	public void uriWithLargeDomainTest() throws MalformedURLException
	{
		String uri = "soapui.local";
		String expectedEndpoint = "http://soapui.local";
		String expectedPath = "";
		String expectedResourceName = "";
		String expectedQuery = "";

		restURIParser = new RestURIParserImpl( uri );

		assertURIParsedCorrectly( expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser );
	}

	@Test
	public void numericResourceTest() throws MalformedURLException
	{
		String uri = "1.57/api/get?id=1234";
		String expectedEndpoint = "";
		String expectedPath = "/1.57/api/get";
		String expectedResourceName = "Get";
		String expectedQuery = "id=1234";

		restURIParser = new RestURIParserImpl( uri );

		assertURIParsedCorrectly( expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser );
	}

	@Test
	public void domainWithHyphenAndPortTest() throws MalformedURLException
	{
		String uri = "http://consys-qa-m09.websys.aol.com:8090/subscribers/subscriber";
		String expectedEndpoint = "http://consys-qa-m09.websys.aol.com:8090";
		String expectedPath = "/subscribers/subscriber";
		String expectedResourceName = "Subscriber";
		String expectedQuery = "";

		restURIParser = new RestURIParserImpl( uri );

		assertURIParsedCorrectly( expectedEndpoint, expectedPath, expectedResourceName, expectedQuery, restURIParser );
	}

	private void assertURIParsedCorrectly( String expectedEndpoint,
														String expectedPath,
														String expectedResourceName,
														String expectedQuery, RestURIParserImpl restURIParser ) throws MalformedURLException
	{
		assertEquals( expectedEndpoint, restURIParser.getEndpoint() );
		assertEquals( expectedPath, restURIParser.getResourcePath() );
		assertEquals( expectedResourceName, restURIParser.getResourceName() );
		assertEquals( expectedQuery, restURIParser.getQuery() );
	}
}
