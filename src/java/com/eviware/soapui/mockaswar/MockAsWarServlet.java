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

package com.eviware.soapui.mockaswar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.monitor.MockEngine;
import com.eviware.soapui.support.StringUtils;

/**
 * Servlet implementation class SoapUIMockServlet
 */
public class MockAsWarServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger( MockAsWarServlet.class.getName() );
	private WsdlProject project;

	public void init() throws ServletException
	{
		super.init();
		try
		{
			if( StringUtils.hasContent( getInitParameter( "listeners" ) ) )
			{
				logger.info( "Init listeners" );
				System.setProperty( "soapui.ext.listeners", getServletContext()
						.getRealPath( getInitParameter( "listeners" ) ) );
			}
			else
			{
				logger.info( "Listeners not set!" );
			}
			if( StringUtils.hasContent( getInitParameter( "actions" ) ) )
			{
				logger.info( "Init actions" );
				System.setProperty( "soapui.ext.actions", getServletContext().getRealPath( getInitParameter( "actions" ) ) );
			}
			else
			{
				logger.info( "Actions not set!" );
			}
			if( StringUtils.hasContent( getInitParameter( "soapuiSettings" ) ) )
			{
				logger.info( "Init settings" );

				SoapUI
						.setSoapUICore( new MockServletSoapUICore( getServletContext(), getInitParameter( "soapuiSettings" ) ) );
			}
			else
			{
				logger.info( "Settings not set!" );
				SoapUI.setSoapUICore( new MockServletSoapUICore( getServletContext() ) );
			}

			logger.info( "Loading project" );
			project = new WsdlProject( getServletContext().getRealPath( getInitParameter( "projectFile" ) ) );

			logger.info( "Starting MockService(s)" );

			for( MockService mockService : project.getMockServiceList() )
			{
				logger.info( "Starting mockService [" + mockService.getName() + "]" );
				mockService.start();
			}
		}
		catch( Exception ex )
		{
			logger.log( Level.SEVERE, null, ex );
		}
	}

	public void destroy()
	{
		super.destroy();
		getMockServletCore().stop();
	}

	protected MockServletSoapUICore getMockServletCore()
	{
		return ( MockServletSoapUICore )SoapUI.getSoapUICore();
	}

	protected void service( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
			IOException
	{
		try
		{
			getMockServletCore().dispatchRequest( request, response );
		}
		catch( DispatchException ex )
		{
			logger.log( Level.SEVERE, null, ex );
		}
	}

	private class MockServletSoapUICore extends DefaultSoapUICore implements MockEngine
	{
		private List<MockRunner> mockRunners = new ArrayList<MockRunner>();

		public MockServletSoapUICore( ServletContext servletContext, String soapUISettings )
		{
			super( servletContext.getRealPath( "/" ), servletContext.getRealPath( soapUISettings ) );
		}

		public void dispatchRequest( HttpServletRequest request, HttpServletResponse response ) throws DispatchException
		{
			for( MockRunner mockRunner : getMockRunners() )
			{
				if( request.getPathInfo().equals( mockRunner.getMockService().getPath() ) )
				{
					mockRunner.dispatchRequest( request, response );
				}
			}
		}

		public void stop()
		{
			for( MockRunner mockRunner : getMockRunners() )
			{
				mockRunner.stop();
			}
		}

		public MockServletSoapUICore( ServletContext servletContext )
		{
			super( servletContext.getRealPath( "/" ), null );
		}

		@Override
		protected MockEngine buildMockEngine()
		{
			return this;
		}

		public MockRunner[] getMockRunners()
		{
			return mockRunners.toArray( new MockRunner[mockRunners.size()] );
		}

		public boolean hasRunningMock( MockService mockService )
		{
			for( MockRunner runner : mockRunners )
			{
				if( runner.getMockService() == mockService )
					return true;
			}

			return false;
		}

		public void startMockService( MockRunner runner ) throws Exception
		{
			mockRunners.add( runner );
		}

		public void stopMockService( MockRunner runner )
		{
			mockRunners.remove( runner );
		}
	}
}
