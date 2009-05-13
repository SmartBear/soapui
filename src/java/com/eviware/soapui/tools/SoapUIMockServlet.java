/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.tools;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.SoapUICore;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunner;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;

/**
 * @author ole
 */
public class SoapUIMockServlet extends HttpServlet
{
	private WsdlMockRunner mockRunner;
	private WsdlMockService mockService;
	private WsdlProject project;
	private static Logger logger = Logger.getLogger( SoapUIMockServlet.class.getName() );

	@Override
	public void init() throws ServletException
	{
		super.init();
		try
		{
			logger.info( "Initializing soapUI Core" );
			SoapUI.setSoapUICore( createSoapUICore( getInitParameter( "settingsFile" ),
					getInitParameter( "settingsPassword" ) ) );

			logger.info( "Loading project" );
			project = new WsdlProject( getInitParameter( "projectFile" ), getInitParameter( "projectPassword" ) );

			logger.info( "Starting MockService" );
			mockService = project.getMockServiceByName( getInitParameter( "mockService" ) );
			mockRunner = mockService.start();
		}
		catch( Exception ex )
		{
			logger.log( Level.SEVERE, null, ex );
		}
	}

	@Override
	protected void service( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
			IOException
	{
		try
		{
			mockRunner.dispatchRequest( request, response );
		}
		catch( DispatchException ex )
		{
			logger.log( Level.SEVERE, null, ex );
		}
	}

	/**
	 * Returns a short description of the servlet.
	 */
	public String getServletInfo()
	{
		return mockService.getName();
	}

	// </editor-fold>

	protected SoapUICore createSoapUICore( String settingsFile, String soapUISettingsPassword )
	{
		return new DefaultSoapUICore( null, settingsFile, soapUISettingsPassword );
	}
}
