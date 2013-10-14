package com.eviware.soapui.impl.rest.panels.request;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the ParameterFinder class
 */
public class ParameterFinderTest
{

	@Test
	public void findsSingleQueryParameter() throws Exception
	{
		ParameterFinder finder = new ParameterFinder( "?name=Johan" );
		assertThat(finder.findParameterAt( 2 ), is("name"));
	}

	@Test
	public void locatesSecondQueryParameter() throws Exception
	{
		ParameterFinder finder = new ParameterFinder( "?name=Johan&reallyLongOne=value" );
		assertThat(finder.findParameterAt( 15 ), is("reallyLongOne"));
	}

	@Test
	public void findsSingleMatrixParameter() throws Exception
	{
		ParameterFinder finder = new ParameterFinder( ";name=Johan?query1=value1" );
		assertThat(finder.findParameterAt( 2 ), is("name"));

	}

	@Test
	public void locatesSecondMatrixParameter() throws Exception
	{
		ParameterFinder finder = new ParameterFinder( ";name=Johan;reallyLongOne=value?query1=value1" );
		assertThat(finder.findParameterAt( 15 ), is("reallyLongOne"));
	}

	@Test
	public void findsParameterWhenEqualsSignClicked() throws Exception
	{
		String parametersString = "?name=Johan&reallyLongOne=value";
		ParameterFinder finder = new ParameterFinder( parametersString );
		assertThat(finder.findParameterAt( parametersString.lastIndexOf( '=' ) ), is("reallyLongOne"));
	}

	@Test
	public void findsNextParameterWhenAmpersandClicked() throws Exception
	{
		String parametersString = "?name=Johan&reallyLongOne=value";
		ParameterFinder finder = new ParameterFinder( parametersString );
		assertThat(finder.findParameterAt( parametersString.indexOf( '&' ) ), is("reallyLongOne"));
	}

	@Test
	public void findsNextParameterWhenSemicolonClicked() throws Exception
	{
		String parametersString = ";name=Johan;reallyLongOne=value?query1=value1";
		ParameterFinder finder = new ParameterFinder( parametersString );
		assertThat(finder.findParameterAt( parametersString.indexOf( ";really" ) ), is("reallyLongOne"));
	}

	@Test
	public void findsParameterWhenValueClicked() throws Exception
	{
		String parametersString = "?name=Johan&reallyLongOne=value";
		ParameterFinder finder = new ParameterFinder( parametersString );
		assertThat(finder.findParameterAt( parametersString.indexOf( "Johan" ) ), is("name"));

	}

	@Test
	public void handlesClickOnTrailingAmpersand() throws Exception
	{
		String parametersString = "?name=Johan&reallyLongOne=value&";
		ParameterFinder finder = new ParameterFinder( parametersString );
		assertThat(finder.findParameterAt( parametersString.lastIndexOf( "&" ) ), is(""));
	}

	@Test
	public void returnsFirstParameterWhenLeadingCharIsClicked() throws Exception
	{
		String parametersString = "?name=Johan&reallyLongOne=value&";
		ParameterFinder finder = new ParameterFinder( parametersString );
		assertThat(finder.findParameterAt( 0 ), is("name"));
	}

	@Test
	public void handlesClickOnLeadingEqualsSign() throws Exception
	{
		String parametersString = "?=name=Johan&reallyLongOne=value&";
		ParameterFinder finder = new ParameterFinder( parametersString );
		assertThat(finder.findParameterAt( parametersString.indexOf( "=" ) ), is(""));
	}
}
