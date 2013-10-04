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
package com.eviware.soapui.impl.wsdl.teststeps.assertions;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.eviware.soapui.config.TestAssertionConfig;
import com.eviware.soapui.impl.wsdl.teststeps.assertions.basic.XQueryContainsAssertion;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.Assertable;
import com.eviware.soapui.model.testsuite.AssertionException;

/*
 * This test covers bug reported in SOAPUI-3935
 */
public class XQueryContainsTestCase
{
	@Mock
	private Assertable assertable;
	@Mock
	private SubmitContext context;

	private String response;
	private XQueryContainsAssertion assertion;

	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( XQueryContainsTestCase.class );
	}

	@Before
	public void setUp() throws Exception
	{
		response = readResource( "/xqueryassertion/response.xml" );
		assertion = new XQueryContainsAssertion( TestAssertionConfig.Factory.newInstance(), assertable );
	}

	private String readResource( String string ) throws Exception
	{
		BufferedReader reader = new BufferedReader( new InputStreamReader( getClass().getResourceAsStream( string ) ) );
		StringBuffer result = new StringBuffer();

		String line = reader.readLine();
		while( line != null )
		{
			result.append( line );
			line = reader.readLine();
		}

		return result.toString();
	}

	@Test( expected = AssertionException.class )
	public void negativeRouteTest() throws AssertionException
	{
		assertion.setPath( "count(DirectionsResponse/route) > 10" );
		assertion.setExpectedContent( "true" );
		assertion.assertContent( response, context, XQueryContainsAssertion.ID );
	}

	@Test
	public void positiveRouteTest() throws AssertionException
	{
		assertion.setPath( "count(DirectionsResponse/route) > 10" );
		assertion.setExpectedContent( "false" );
		String result = assertion.assertContent( response, context, XQueryContainsAssertion.ID );
		assertEquals( "Not matched expected!", "XQuery Match matches content for [count(DirectionsResponse/route) > 10]",
				result );
	}

	@Test( expected = AssertionException.class )
	public void negativeLatitudeTest() throws AssertionException
	{
		assertion.setPath( "/DirectionsResponse/route[1]/leg[1]/step[1]/start_location[1]/lat[1]" );
		assertion.setExpectedContent( "<lat>-35.9286900</lat>" );
		assertion.assertContent( response, context, XQueryContainsAssertion.ID );
	}
	
	@Test
	public void positiveLatitudeTest() throws AssertionException
	{
		assertion.setPath( "/DirectionsResponse/route[1]/leg[1]/step[1]/start_location[1]/lat[1]" );
		assertion.setExpectedContent( "<lat>-34.9286900</lat>" );
		String result = assertion.assertContent( response, context, XQueryContainsAssertion.ID );
		assertEquals( "Not matched expected!", "XQuery Match matches content for [/DirectionsResponse/route[1]/leg[1]/step[1]/start_location[1]/lat[1]]",
				result );
	}

}
