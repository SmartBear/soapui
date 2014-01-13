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

package com.eviware.soapui.support;

import com.eviware.soapui.SoapUI;
import junit.framework.JUnit4TestAdapter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class TestCaseWithJetty
{
	public static junit.framework.Test suite()
	{
		return new JUnit4TestAdapter( TestCaseWithJetty.class );
	}

	private static Server server;

	@BeforeClass
	public static void setUp() throws Exception
	{
		if( server != null )
		{
			return;
		}

		server = new Server( 8082 );
		ResourceHandler resource_handler = new ResourceHandler();
		resource_handler.setResourceBase( new File( TestCaseWithJetty.class.getResource( "/" ).toURI() ).getCanonicalPath() );

		HandlerList handlers = new HandlerList();
		handlers.setHandlers( new Handler[] { resource_handler, new DefaultHandler() } );
		server.setHandler( handlers );

		try
		{
			server.start();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	@AfterClass
	public static void stop() throws Exception
	{
		try
		{
			server.stop();
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}

	}

	@Test
	public void testDummy() throws Exception
	{
		assertTrue( true );
	}
}
