/*
 *  SoapUI, copyright (C) 2004-2014 smartbear.com 
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.monitor;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author joel.jonsson
 */
public class ContentTypesTest
{
	@Test
	public void emptyStringGivesWildcardContentType()
	{
		assertThat( ContentTypes.of( "" ).toString(), is( "" ) );
	}

	@Test
	public void separateTwoContentTypesWithCommaAndSpace()
	{
		assertThat( ContentTypes.of( "text/plain,application/*" ).toString(), is( "text/plain, application/*" ) );
	}

	@Test
	public void noContentTypeDoesMatchRequestWithContentType()
	{
		assertThat( ContentTypes.of( "" ).matches( "text/plain" ), is( false ) );
	}

	@Test
	public void fullWildCardMatchesRequestWithContentType()
	{
		assertThat( ContentTypes.of( "*/*" ).matches( "text/plain" ), is( true ) );
	}

	@Test
	public void fullWildCardMatchesRequestWithContentTypeAndParameters()
	{
		assertThat( ContentTypes.of( "*/*" ).matches( "text/plain; charset=utf-8" ), is( true ) );
	}

	@Test
	public void wildCardPrimaryTypeMatchesRequestWithContentType()
	{
		assertThat( ContentTypes.of( "*/plain" ).matches( "text/plain" ), is( true ) );
	}

	@Test
	public void wildCardPrimaryTypeDoesNotMatchRequestWithOtherSubtype()
	{
		assertThat( ContentTypes.of( "*/xml" ).matches( "text/plain" ), is( false ) );
	}

	@Test
	public void wildCardSubTypeMatchesRequestWithContentType()
	{
		assertThat( ContentTypes.of( "text/*" ).matches( "text/plain" ), is( true ) );
	}

	@Test
	public void wildCardSubTypeDoesNotMatchRequestWithOtherPrimaryType()
	{
		assertThat( ContentTypes.of( "application/*" ).matches( "text/plain" ), is( false ) );
	}

	@Test
	public void equalContentTypesMatches()
	{
		assertThat( ContentTypes.of( "text/plain" ).matches( "text/plain" ), is( true ) );
	}

	@Test
	public void equalContentTypesMatchesEvenWithParameters()
	{
		assertThat( ContentTypes.of( "text/plain" ).matches( "text/plain; charset=utf-8" ), is( true ) );
	}

	@Test
	public void invalidContentTypeIsSilentlyIgnoredAndDoesNotMatch()
	{
		assertThat( ContentTypes.of( "hejhopp" ).matches( "hejhopp/tjoho" ), is( false ) );
	}

	@Test
	public void invalidRetrievedContentTypeIsSilentlyIgnoredAndDoesNotMatch()
	{
		assertThat( ContentTypes.of( "*/*" ).matches( "hejhopp" ), is( false ) );
	}

	@Test
	public void separateMatchingForSeveralContentTypes()
	{
		ContentTypes contentTypes = ContentTypes.of( "text/plain, application/*" );
		assertThat( contentTypes.matches( "text/plain" ), is( true ) );
		assertThat( contentTypes.matches( "application/xml" ), is( true ) );
		assertThat( contentTypes.matches( "text/html" ), is( false ) );
	}
}
