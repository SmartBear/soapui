/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;

import org.xml.sax.InputSource;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.support.soap.SoapMessageBuilder;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsdl.CachedWsdlLoader;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunListener;
import com.eviware.soapui.model.mock.MockRunner;
import com.eviware.soapui.monitor.MockEngine;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.Tools;
import com.eviware.soapui.support.editor.inspectors.attachments.ContentTypeHandler;
import com.eviware.soapui.support.types.StringToStringMap;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * MockRunner that dispatches Http Requests to their designated
 * WsdlMockOperation if possible
 * 
 * @author ole.matzura
 */

public class WsdlMockRunner implements MockRunner
{
	private WsdlMockService mockService;
	private final List<WsdlMockResult> mockResults = Collections.synchronizedList( new LinkedList<WsdlMockResult>() );
	private long maxResults = 100;
	private int removed = 0;
	private final WsdlMockRunContext mockContext;
	private final Map<String, StringToStringMap> wsdlCache = new HashMap<String, StringToStringMap>();
	private boolean running;
	private boolean logEnabled = true;

	public WsdlMockRunner( WsdlMockService mockService, WsdlTestRunContext context ) throws Exception
	{
		this.mockService = mockService;

		Set<WsdlInterface> interfaces = new HashSet<WsdlInterface>();

		for( int i = 0; i < mockService.getMockOperationCount(); i++ )
		{
			WsdlOperation operation = mockService.getMockOperationAt( i ).getOperation();
			if( operation != null )
				interfaces.add( operation.getInterface() );
		}

		for( WsdlInterface iface : interfaces )
			iface.getWsdlContext().loadIfNecessary();

		mockContext = new WsdlMockRunContext( mockService, context );

		mockService.runStartScript( mockContext, this );

		SoapUI.getMockEngine().startMockService( this );
		running = true;
		
		MockRunListener[] mockRunListeners = mockService.getMockRunListeners();

		for( MockRunListener listener : mockRunListeners )
		{
			listener.onMockRunnerStart( this );
		}

		initWsdlCache();
	}

	private void initWsdlCache()
	{
		for( Interface iface : mockService.getMockedInterfaces() )
		{
			if( !iface.getInterfaceType().equals(WsdlInterfaceFactory.WSDL_TYPE))
				continue;
				
			try
			{
				CachedWsdlLoader loader = null;

				
				if( ((WsdlInterface)iface).isCached() )
				{
					loader = ( CachedWsdlLoader ) ((WsdlInterface)iface).createWsdlLoader();
				}
				else
				{
					loader = new CachedWsdlLoader( (WsdlInterface)iface );
				}

				String wsdlPrefix = getInterfacePrefix( iface ).substring( 1 );
				StringToStringMap parts = loader.createFilesForExport( wsdlPrefix + "&part=" );

				for( String key : parts.keySet() )
				{
					if( key.toLowerCase().endsWith( ".wsdl" ) )
					{
						InputSource inputSource = new InputSource( new StringReader( parts.get( key ) ) );
						String content = WsdlUtils.replacePortEndpoint( (WsdlInterface) iface, inputSource,  getLocalMockServiceEndpoint() );
						
						if( content != null )
							parts.put( key, content );
					}
				}

				wsdlCache.put( iface.getName(), parts );

				MockEngine.log.info( "Mounted WSDL for interface [" + iface.getName() + "] at [" + wsdlPrefix + "]" );
			}
			catch( Exception e )
			{
				SoapUI.logError( e );
			}
		}
	}

	public String getLocalMockServiceEndpoint()
	{
		String host = mockService.getHost();
		if( StringUtils.isNullOrEmpty( host ))
			host = "127.0.0.1";
		
		return "http://" + host + ":" + mockService.getPort()	+ mockService.getPath();
	}

	public String getInterfacePrefix( Interface iface )
	{
		String wsdlPrefix = getOverviewUrl() + "&interface=" + iface.getName();
		return wsdlPrefix;
	}

	public WsdlMockRunContext getMockContext()
	{
		return mockContext;
	}

	public synchronized void addMockResult( WsdlMockResult mockResult )
	{
		if( maxResults > 0 && logEnabled )
			mockResults.add( mockResult );
		
		while( mockResults.size() > maxResults )
		{
			mockResults.remove( 0 );
			removed++;
		}
	}
	
	public boolean isRunning()
	{
		return running;
	}

	public void stop()
	{
		if( !isRunning() )
			return;
		
		SoapUI.getMockEngine().stopMockService( this );

		MockRunListener[] mockRunListeners = mockService.getMockRunListeners();

		for( MockRunListener listener : mockRunListeners )
		{
			listener.onMockRunnerStop( this );
		}

		try
		{
			mockService.runStopScript( mockContext, this );
			running = false;
		}
		catch( Exception e )
		{
			SoapUI.logError( e );
		}
	}

	public WsdlMockService getMockService()
	{
		return mockService;
	}

	public long getMaxResults()
	{
		return maxResults;
	}

	public synchronized void setMaxResults( long l )
	{
		this.maxResults = l;

		while( mockResults.size() > l )
		{
			mockResults.remove( 0 );
			removed++;
		}
	}

	@SuppressWarnings( "unchecked" )
	public WsdlMockResult dispatchMockRequest( HttpServletRequest request, HttpServletResponse response )
				throws DispatchException
	{
		WsdlMockResult result = null;
		MockRunListener[] mockRunListeners = mockService.getMockRunListeners();

		try
		{
			for( MockRunListener listener : mockRunListeners )
			{
				listener.onMockRequest( this, request, response );
			}

			long timestamp = System.currentTimeMillis();
			WsdlMockRequest mockRequest = new WsdlMockRequest( request, response, mockContext );

			SoapVersion soapVersion = mockRequest.getSoapVersion();
			if( soapVersion == null )
				throw new DispatchException( "Unrecognized SOAP Version" );

			String soapAction = mockRequest.getSoapAction();

			WsdlOperation operation = SoapUtils.findOperationForRequest( soapVersion, soapAction, mockRequest
						.getRequestXmlObject(), mockService.getMockedOperations(), 
						mockService.isRequireSoapVersion(), mockService.isRequireSoapAction() );

			if( operation != null )
			{
				WsdlMockOperation mockOperation = mockService.getMockOperation( operation );
				if( mockOperation != null )
				{
					long startTime = System.nanoTime();
					try
					{
						result = mockOperation.dispatchRequest( mockRequest, response );
					}
					catch( DispatchException e )
					{
						result = new WsdlMockResult( mockRequest, response );
						
						String fault = SoapMessageBuilder.buildFault( "Server", e.getMessage(), mockRequest.getSoapVersion() );
						result.setResponseContent( fault );
						result.setMockOperation( mockOperation );
						
						response.getWriter().write(fault);
					}
					
					if( request instanceof org.mortbay.jetty.Request)
					    ( ( org.mortbay.jetty.Request ) request ).setHandled( true );
					
					result.setTimeTaken( ( System.nanoTime() - startTime ) / 1000000 );
					result.setTimestamp( timestamp );
					addMockResult( result );
					return result;
				}
				else
				{
					throw new DispatchException( "Failed to find matching operation for request" );
				}
			}

			throw new DispatchException( "Missing operation for soapAction [" + soapAction + "] and body element ["
						+ XmlUtils.getQName( mockRequest.getContentElement().getDomNode() ) + "] with SOAP Version ["
						+ mockRequest.getSoapVersion() + "]" );
		}
		catch( Exception e )
		{
			if( e instanceof DispatchException )
				throw ( DispatchException ) e;

			throw new DispatchException( e );
		}
		finally
		{
			if( result != null )
			{
				for( MockRunListener listener : mockRunListeners )
				{
					listener.onMockResult( result );
				}
			}
		}
	}

	public MockResult getMockResultAt( int index )
	{
		return index <= removed ? null : mockResults.get( index - removed );
	}

	public int getMockResultCount()
	{
		return mockResults.size() + removed;
	}

	public synchronized void clearResults()
	{
		mockResults.clear();
	}

	public void release()
	{
		clearResults();
		mockService = null;
		mockContext.clear();
	}

	public void dispatchGetRequest( HttpServletRequest request, HttpServletResponse response ) throws DispatchException
	{
		try
		{
			if( request.getQueryString() != null && request.getQueryString().startsWith( "WSDL" ) )
			{
				dispatchWsdlRequest( request, response );
			}
			else
			{
				String docroot = getMockService().getDocroot();
				if( StringUtils.hasContent( docroot ))
				{
					try
					{
						File file = new File( docroot + request.getPathInfo().replace('/', File.separatorChar ));
						FileInputStream in = new FileInputStream( file );
						response.setStatus( HttpServletResponse.SC_OK );
						long length = file.length();
						response.setContentLength((int) length);
						response.setContentType( ContentTypeHandler.getContentTypeFromFilename( file.getName() ));
						Tools.readAndWrite( in, length, response.getOutputStream());
					} 
					catch (Exception e)
					{
						throw new DispatchException( e );
					}
				}
			}
		}
		catch( Exception e )
		{
			throw new DispatchException( e );
		}
		finally
		{
			try
			{
				response.flushBuffer();
			}
			catch( IOException e )
			{
				throw new DispatchException( e );
			}
		}
	}

	protected void dispatchWsdlRequest(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		if( request.getQueryString().equalsIgnoreCase("WSDL"))
		{
			printWsdl( response );
			return;
		}
		
		String ifaceName = request.getParameter( "interface" );
		WsdlInterface iface = (WsdlInterface) mockService.getProject().getInterfaceByName( ifaceName );
		if( iface == null )
		{
			printInterfaceList( response );
			return;
		}

		StringToStringMap parts = wsdlCache.get( iface.getName() );
		String part = request.getParameter( "part" );
		if( part == null )
			part = "#root#";
		
		String content = parts.get( part );

		if( content == null )
		{
			printPartList( iface, parts, response );
			return;
		}

		if( content != null )
		{
			printOkXmlResult(response, content);
		}
	}

	private void printOkXmlResult(HttpServletResponse response, String content) throws IOException
	{
		response.setStatus( HttpServletResponse.SC_OK );
		response.setContentType( "text/xml" );
		response.setCharacterEncoding( "UTF-8" );
		response.getWriter().print( content );
	}

	private void printWsdl(HttpServletResponse response) throws IOException
	{
		WsdlInterface[] mockedInterfaces = mockService.getMockedInterfaces();
		if( mockedInterfaces.length == 1 )
		{
			StringToStringMap parts = wsdlCache.get( mockedInterfaces[0].getName() );
			printOkXmlResult(response, parts.get( "#root#" ));
		}
		else
		{
			try
			{
				WSDLFactory wsdlFactory = WSDLFactory.newInstance();
				Definition def = wsdlFactory.newDefinition();
				for( WsdlInterface iface : mockedInterfaces )
				{
					Import wsdlImport = def.createImport();
					wsdlImport.setLocationURI(getInterfacePrefix( iface ));
					wsdlImport.setNamespaceURI( iface.getWsdlContext().getDefinition().getTargetNamespace());
					
					def.addImport(wsdlImport);
				}
				
				response.setStatus( HttpServletResponse.SC_OK );
				response.setContentType( "text/xml" );
				response.setCharacterEncoding( "UTF-8" );
				
				WSDLWriter writer = wsdlFactory.newWSDLWriter();
				writer.writeWSDL(def, response.getWriter());
			}
			catch (Exception e)
			{
			   SoapUI.logError(e);
				throw new IOException( "Failed to create combined WSDL" );
			}
		}
	}

	private void printPartList( WsdlInterface iface, StringToStringMap parts, HttpServletResponse response )
				throws IOException
	{
		response.setStatus( HttpServletResponse.SC_OK );
		response.setContentType( "text/html" );

		PrintWriter out = response.getWriter();
		out.print( "<html><body><p>Parts in interface [" + iface.getName() + "]</p><ul>" );

		for( String key : parts.keySet() )
		{
			out.print( "<li><a href=\"" );
			out.print( getInterfacePrefix( iface ) + "&part=" + key );
			out.print( "\">" + key + "</a></li>" );
		}

		out.print( "</ul></p></body></html>" );
	}

	private void printInterfaceList( HttpServletResponse response ) throws IOException
	{
		response.setStatus( HttpServletResponse.SC_OK );
		response.setContentType( "text/html" );

		PrintWriter out = response.getWriter();
		out.print( "<html><body><p>Mocked Interfaces in project [" + mockService.getProject().getName() + "]</p><ul>" );

		for( Interface iface : mockService.getProject().getInterfaceList() )
		{
			out.print( "<li><a href=\"" );
			out.print( getInterfacePrefix( iface ) );
			out.print( "\">" + iface.getName() + "</a></li>" );
		}

		out.print( "</ul></p></body></html>" );
	}

	public String getOverviewUrl()
	{
		return mockService.getPath() + "?WSDL";
	}

	public void setLogEnabled( boolean logEnabled )
	{
		this.logEnabled = logEnabled;
	}
}
