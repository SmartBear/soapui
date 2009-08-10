/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.support;

import junit.framework.TestCase;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;

import com.eviware.soapui.SoapUI;

public class TestCaseWithJetty extends TestCase
{
	private static Server server;
	
	protected void setUp() throws Exception
	{
		if( server != null )
		{	
			return;
		}
		
		server = new Server( 8082 );
		ResourceHandler resource_handler = new ResourceHandler();
		resource_handler.setResourceBase( ".\\src\\test-resources" );

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
	
	public void testDummy() throws Exception
	{
		assertTrue( 1 == 1 );
	}
}
