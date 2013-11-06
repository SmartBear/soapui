/*
 *  SoapUI, copyright (C) 2004-2011 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.mockaswar;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.list.TreeList;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.DefaultSoapUICore;
import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.DispatchException;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRunner;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.model.mock.MockService;
import com.eviware.soapui.model.project.ProjectFactoryRegistry;
import com.eviware.soapui.monitor.MockEngine;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;
import com.eviware.soapui.support.types.StringToStringsMap;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Servlet implementation class SoapUIMockServlet
 */
@SuppressWarnings( "unchecked" )
public class MockAsWarServlet extends HttpServlet
{

	private static final long serialVersionUID = 1L;
	protected static Logger logger = Logger.getLogger( MockAsWarServlet.class.getName() );
	protected WsdlProject project;
	long maxResults;
	List<MockResult> results = new TreeList();
	private List<LoggingEvent> events = new TreeList();
	boolean enableWebUI;

	public void init() throws ServletException
	{
		super.init();
		try
		{
			String mockServiceEndpoint = initMockServiceParameters();

			logger.info( "Loading project" );

			initProject( getServletContext().getRealPath( getInitParameter( "projectFile" ) ) );

			if( project == null || project.getName() == null )
				initProject( getServletContext().getResource( "/" + getInitParameter( "projectFile" ) ).toString() );

			if( project == null )
				logger.info( "Starting MockService(s)" );

			for( MockService mockService : project.getMockServiceList() )
			{
				logger.info( "Starting mockService [" + mockService.getName() + "]" );
				if( StringUtils.hasContent( mockServiceEndpoint ) )
				{
					( ( WsdlMockService )mockService ).setMockServiceEndpoint( mockServiceEndpoint );
				}

				mockService.start();
			}
		}
		catch( Exception ex )
		{
			logger.log( Level.SEVERE, null, ex );
		}
	}

	protected void initProject( String path ) throws XmlException, IOException, SoapUIException
	{
		project = ( WsdlProject )ProjectFactoryRegistry.getProjectFactory( "wsdl" ).createNew( path );
	}

	protected String initMockServiceParameters()
	{
		if( StringUtils.hasContent( getInitParameter( "listeners" ) ) )
		{
			logger.info( "Init listeners" );
			System.setProperty( "soapui.ext.listeners", getServletContext().getRealPath( getInitParameter( "listeners" ) ) );
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

		if( SoapUI.getSoapUICore() == null )
		{
			if( StringUtils.hasContent( getInitParameter( "soapUISettings" ) ) )
			{
				logger.info( "Init settings" );
				SoapUI.setSoapUICore(
						new MockServletSoapUICore( getServletContext(), getInitParameter( "soapUISettings" ) ), true );
			}
			else
			{
				logger.info( "Settings not set!" );
				SoapUI.setSoapUICore( new MockServletSoapUICore( getServletContext() ), true );
			}
		}
		else
			logger.info( "SoapUI core already exists, reusing existing one" );

		if( StringUtils.hasContent( getInitParameter( "enableWebUI" ) ) )
		{
			if( "true".equals( getInitParameter( "enableWebUI" ) ) )
			{
				logger.info( "WebUI ENABLED" );
				enableWebUI = true;
			}
			else
			{
				logger.info( "WebUI DISABLED" );
				enableWebUI = false;
			}
		}

		try
		{
			maxResults = Integer.parseInt( getInitParameter( "maxResults" ) );
		}
		catch( Throwable t )
		{
			maxResults = 1000;
		}

		SoapUI.ensureGroovyLog().addAppender( new GroovyLogAppender() );

		String mockServiceEndpoint = getInitParameter( "mockServiceEndpoint" );
		return mockServiceEndpoint;
	}

	public void destroy()
	{
		super.destroy();
		getMockServletCore().stop();
	}

	protected MockAsWarCoreInterface getMockServletCore()
	{
		return ( MockAsWarCoreInterface )SoapUI.getSoapUICore();
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

	private void printResult( PrintWriter out, MockResult result )
	{

		out.print( "<h4>Details for MockResult at " + new java.util.Date( result.getTimestamp() ) + " ("
				+ result.getTimeTaken() + "ms)</h4>" );

		out.println( "<hr/><p><b>Request Headers</b>:</p>" );
		out.print( "<table border=\"1\"><tr><td>Header</td><td>Value</td></tr>" );
		StringToStringsMap headers = result.getMockRequest().getRequestHeaders();
		for( String name : headers.getKeys() )
		{
			for( String value : headers.get( name ) )
				out.println( "<tr><td>" + name + "</td><td>" + value + "</td></tr>" );
		}
		out.println( "</table>" );

		out.println( "<hr/><b>Incoming Request</b>:<br/><pre>"
				+ XmlUtils.entitize( result.getMockRequest().getRequestContent() ) + "</pre>" );

		out.println( "<hr/><p><b>Response Headers</b>:</p>" );
		out.print( "<table border\"1\"><tr><td>Header</td><td>Value</td></tr>" );
		headers = result.getResponseHeaders();
		for( String name : headers.getKeys() )
		{
			for( String value : headers.get( name ) )
				out.println( "<tr><td>" + name + "</td><td>" + value + "</td></tr>" );
		}
		out.println( "</table>" );

		out.println( "<hr/><b>Returned Response</b>:<pre>" + XmlUtils.entitize( result.getResponseContent() ) + "</pre>" );
	}

	class MockServletSoapUICore extends DefaultSoapUICore implements MockEngine, MockAsWarCoreInterface
	{
		private final ServletContext servletContext;
		private List<MockRunner> mockRunners = new ArrayList<MockRunner>();

		public MockServletSoapUICore( ServletContext servletContext, String soapUISettings )
		{
			super( servletContext.getRealPath( "/" ), servletContext.getRealPath( soapUISettings ) );
			this.servletContext = servletContext;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.eviware.soapui.mockaswar.MockAsWarCoreInterface#dispatchRequest
		 * (javax.servlet.http.HttpServletRequest,
		 * javax.servlet.http.HttpServletResponse)
		 */
		@Override
		public void dispatchRequest( HttpServletRequest request, HttpServletResponse response ) throws DispatchException,
				IOException
		{
			String pathInfo = request.getPathInfo();
			if( pathInfo == null )
				pathInfo = "";

			for( MockRunner mockRunner : getMockRunners() )
			{
				if( pathInfo.equals( mockRunner.getMockService().getPath() ) )
				{
					MockResult result = mockRunner.dispatchRequest( request, response );

					if( maxResults > 0 )
					{
						synchronized( results )
						{
							while( maxResults > 0 && results.size() > maxResults )
							{
								results.remove( 0 );
							}
							if( result != null )
							{
								results.add( result );
							}
						}
					}
					return;
				}
			}

			if( enableWebUI )
			{
				String realPath = servletContext.getRealPath( pathInfo );
				File file = realPath == null ? null : new File( realPath );
				if( file != null && file.exists() && file.isFile() )
				{
					FileInputStream in = new FileInputStream( file );
					response.setStatus( HttpServletResponse.SC_OK );
					long length = file.length();
					response.setContentLength( ( int )length );
					response.setContentType( ContentTypeHandler.getContentTypeFromFilename( file.getName() ) );
					Tools.readAndWrite( in, length, response.getOutputStream() );
					in.close();
				}
				else if( pathInfo.equals( "/master" ) )
				{
					printMaster( request, response, mockRunners );
				}
				else if( pathInfo.equals( "/detail" ) )
				{
					printDetail( request, response );
				}
				else if( pathInfo.equals( "/log" ) )
				{
					printLog( request, response );
				}
				else
				{
					printFrameset( request, response );
				}
			}
			else
			{
				printDisabledLogFrameset( request, response );
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.eviware.soapui.mockaswar.MockAsWarCoreInterface#stop()
		 */
		@Override
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
			this.servletContext = servletContext;
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
				{
					return true;
				}
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

	public void printMaster( HttpServletRequest request, HttpServletResponse response, List<MockRunner> mockRunners )
			throws IOException
	{
		response.setStatus( HttpServletResponse.SC_OK );
		response.setContentType( "text/html" );

		PrintWriter out = response.getWriter();
		startHtmlPage( out, "MockService Log Table", "15" );

		out.print( "<h3><img src=\"header_logo.jpg\"> SoapUI MockServices Log for project [" + project.getName()
				+ "]</h3>" + "<p style=\"text-align: left\">WSDLs:" );

		for( MockRunner mockRunner : mockRunners )
		{
			String overviewUrl = ( ( WsdlMockRunner )mockRunner ).getOverviewUrl();
			if( overviewUrl.startsWith( "/" ) )
			{
				overviewUrl = overviewUrl.substring( 1 );
			}

			out.print( " [<a target=\"new\" href=\"" + overviewUrl + "\">" + mockRunner.getMockService().getName()
					+ "</a>]" );
		}

		out.print( "</p>" );

		out.print( "<hr/><p><b>Processed Requests</b>: " );
		out.print( "[<a href=\"master\">Refresh</a>] " );
		out.print( "[<a href=\"master?clear\">Clear</a>]</p>" );

		if( "clear".equals( request.getQueryString() ) )
		{
			results.clear();
		}

		out.print( "<table border=\"1\">" );
		out.print( "<tr><td></td><td>Timestamp</td><td>Time Taken</td><td>MockOperation</td><td>MockResponse</td><td>MockService</td></tr>" );

		int cnt = 1;

		for( MockResult result : results )
		{

			if( result != null )
			{
				out.print( "<tr><td>" + ( cnt++ ) + "</td>" );
				out.print( "<td><a target=\"detail\" href=\"detail?" + result.hashCode() + "\">"
						+ new java.util.Date( result.getTimestamp() ) + "</a></td>" );
				out.print( "<td>" + result.getTimeTaken() + "</td>" );
				out.print( "<td>" + result.getMockOperation().getName() + "</td>" );
				if( result.getMockResponse() != null )
					out.print( "<td>" + result.getMockResponse().getName() + "</td>" );
				out.print( "<td>" + result.getMockOperation().getMockService().getName() + "</td></tr>" );
			}
		}

		out.print( "</table>" );

		out.print( "</body></html>" );
		out.flush();

	}

	private void startHtmlPage( PrintWriter out, String title, String refresh )
	{
		out.print( "<html><head>" );
		out.print( "<title>" + title + "</title>" );
		if( refresh != null )
		{
			out.print( "<meta http-equiv=\"refresh\" content=\"" + refresh + "\"/>" );
		}

		out.print( "<link type=\"text/css\" rel=\"stylesheet\" href=\"stylesheet.css\" />" );
		out.print( "</head><body>" );
	}

	public void printDisabledLogFrameset( HttpServletRequest request, HttpServletResponse response ) throws IOException
	{
		response.setStatus( HttpServletResponse.SC_OK );
		response.setContentType( "text/html" );

		PrintWriter out = response.getWriter();
		out.print( "<html><head><title>SoapUI MockServices Log for project [" + project.getName() + "]</title></head>" );
		out.print( "<body>" );
		out.print( "<h3>" );
		out.print( "Log is disabled." );
		out.print( "</h3>" );
		out.print( "</body></html>" );
		out.flush();
	}

	public void printFrameset( HttpServletRequest request, HttpServletResponse response ) throws IOException
	{
		response.setStatus( HttpServletResponse.SC_OK );
		response.setContentType( "text/html" );

		PrintWriter out = response.getWriter();
		out.print( "<html><head><title>SoapUI MockServices Log for project [" + project.getName() + "]</title></head>" );
		out.print( "<frameset rows=\"40%,40%,*\">" );
		out.print( "<frame src=\"master\"/>" );
		out.print( "<frame name=\"detail\" src=\"detail\"/>" );
		out.print( "<frame src=\"log\"/>" );
		out.print( "</frameset>" );
		out.print( "</html>" );
		out.flush();
	}

	public void printDetail( HttpServletRequest request, HttpServletResponse response ) throws IOException
	{
		response.setStatus( HttpServletResponse.SC_OK );
		response.setContentType( "text/html" );

		PrintWriter out = response.getWriter();

		startHtmlPage( out, "MockService Detail", null );

		int id = 0;

		try
		{
			id = Integer.parseInt( request.getQueryString() );
		}
		catch( NumberFormatException e )
		{
		}

		if( id > 0 )
		{
			for( MockResult result : results )
			{
				if( result.hashCode() == id )
				{
					id = 0;
					printResult( out, result );
				}
			}
		}

		if( id > 0 )
		{
			out.print( "<p>Missing specified MockResult</p>" );
		}

		out.print( "</body></html>" );
		out.flush();
	}

	private class GroovyLogAppender extends org.apache.log4j.AppenderSkeleton
	{

		protected void append( LoggingEvent event )
		{
			events.add( event );
		}

		public void close()
		{
		}

		public boolean requiresLayout()
		{
			return false;
		}
	}

	public void printLog( HttpServletRequest request, HttpServletResponse response ) throws IOException
	{
		response.setStatus( HttpServletResponse.SC_OK );
		response.setContentType( "text/html" );

		PrintWriter out = response.getWriter();
		startHtmlPage( out, "MockService Groovy Log", "15" );
		out.print( "<p><b>Groovy Log output</b>: " );
		out.print( "[<a href=\"log\">Refresh</a>] " );
		out.print( "[<a href=\"log?clear\">Clear</a>]</p>" );

		if( "clear".equals( request.getQueryString() ) )
		{
			events.clear();
		}

		out.print( "<table border=\"1\">" );
		out.print( "<tr><td></td><td>Timestamp</td><td>Message</td></tr>" );

		int cnt = 1;

		for( LoggingEvent event : events )
		{

			out.print( "<tr><td>" + ( cnt++ ) + "</td>" );
			out.print( "<td>" + new java.util.Date( event.timeStamp ) + "</td>" );
			out.print( "<td>" + event.getRenderedMessage() + "</td></tr>" );
		}

		out.print( "</table>" );

		out.print( "</body></html>" );
		out.flush();
	}
}
