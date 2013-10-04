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

package com.eviware.soapui.impl.wsdl;

import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;

import com.eviware.soapui.config.WsdlInterfaceConfig;

public class WsdlInterfaceTestCase
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( WsdlInterfaceTestCase.class );
	}

	private WsdlProject project;
	private WsdlInterfaceConfig interfaceConfig;
	private WsdlInterface iface;

	@Before
	public void setUp() throws Exception
	{
		project = new WsdlProject();
		interfaceConfig = WsdlInterfaceConfig.Factory.newInstance();
		iface = new WsdlInterface( project, interfaceConfig );

		assertEquals( 0, iface.getEndpoints().length );
	}

	@Test
	public void testAddEndpoints() throws Exception
	{
		iface.addEndpoint( "testEndpoint" );
		assertEquals( 1, iface.getEndpoints().length );
		assertEquals( "testEndpoint", iface.getEndpoints()[0] );

		iface.addEndpoint( "testEndpoint" );
		assertEquals( 1, iface.getEndpoints().length );
		assertEquals( "testEndpoint", iface.getEndpoints()[0] );

		iface.addEndpoint( "testEndpoint2" );
		assertEquals( 2, iface.getEndpoints().length );
		assertEquals( "testEndpoint", iface.getEndpoints()[0] );
		assertEquals( "testEndpoint2", iface.getEndpoints()[1] );
	}

	@Test
	public void testRemoveEndpoints() throws Exception
	{
		iface.addEndpoint( "testEndpoint" );
		iface.addEndpoint( "testEndpoint2" );

		iface.removeEndpoint( "testEndpoint" );
		assertEquals( 1, iface.getEndpoints().length );

		iface.removeEndpoint( "testEndpoint2" );
		assertEquals( 0, iface.getEndpoints().length );
	}
}
