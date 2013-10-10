package com.eviware.soapui.impl.rest.panels.request;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: manne
 * Date: 10/9/13
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
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


}
