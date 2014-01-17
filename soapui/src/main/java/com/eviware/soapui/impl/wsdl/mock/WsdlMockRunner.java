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

package com.eviware.soapui.impl.wsdl.mock;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.support.AbstractMockService;
import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestRunContext;
import com.eviware.soapui.model.mock.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * MockRunner that dispatches Http Requests to their designated
 * WsdlMockOperation if possible
 * 
 * @author ole.matzura
 */

@SuppressWarnings( "unchecked" )
public class WsdlMockRunner implements MockRunner
{
	private AbstractMockService mockService;
	private final WsdlMockRunContext mockContext;
	private boolean running;
	private MockDispatcher dispatcher;

	public WsdlMockRunner( AbstractMockService mockService, WsdlTestRunContext context ) throws Exception
	{
		this.mockService = mockService;

		Set<WsdlInterface> interfaces = new HashSet<WsdlInterface>();

		// TODO: move this code elsewhere when the rest counterpoint is in place
		if( mockService instanceof WsdlMockService )
		{
			WsdlMockService wsdlMockService = (WsdlMockService)mockService;

			for( int i = 0; i < mockService.getMockOperationCount(); i++ )
			{
				WsdlOperation operation = wsdlMockService.getMockOperationAt( i ).getOperation();
				if( operation != null )
					interfaces.add( operation.getInterface() );
			}
		}

		for( WsdlInterface iface : interfaces )
			iface.getWsdlContext().loadIfNecessary();

		mockContext = new WsdlMockRunContext( this.mockService, context );
		dispatcher = mockService.createDispatcher(mockContext);

		start();
	}

	public WsdlMockRunContext getMockContext()
	{
		return mockContext;
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

	public void release()
	{
		mockService = null;
		mockContext.clear();
		dispatcher = null;

	}

	@Override
	public int getMockResultCount()
	{
		return dispatcher.getMockResultCount();
	}

	@Override
	public MockResult getMockResultAt( int index )
	{
		return dispatcher.getMockResultAt( index );
	}

	public MockService getMockService()
	{
		return mockService;
	}

	@Override
	public MockResult dispatchRequest( HttpServletRequest request, HttpServletResponse response )
			throws DispatchException
	{
		for( MockRunListener listener : mockService.getMockRunListeners() )
		{
			Object result = listener.onMockRequest( this, request, response );
			if( result instanceof MockResult )
				return ( MockResult )result;
		}

		String qs = request.getQueryString();
		if( qs != null && qs.startsWith( "cmd=" ) )
		{
			try
			{
				dispatchCommand( request.getParameter( "cmd" ), request, response );
			}
			catch( IOException e )
			{
				throw new DispatchException( e );
			}
		}

		return dispatcher.dispatchRequest( request, response );
	}

	private void dispatchCommand( String cmd, HttpServletRequest request, HttpServletResponse response )
			throws IOException
	{
		if( "stop".equals( cmd ) )
		{
			response.setStatus( HttpServletResponse.SC_OK );
			response.flushBuffer();

			SoapUI.getThreadPool().execute( new Runnable()
			{

				public void run()
				{
					try
					{
						Thread.sleep( 500 );
					}
					catch( InterruptedException e )
					{
						e.printStackTrace();
					}
					stop();
				}
			} );
		}
		else if( "restart".equals( cmd ) )
		{
			response.setStatus( HttpServletResponse.SC_OK );
			response.flushBuffer();

			SoapUI.getThreadPool().execute( new Runnable()
			{

				public void run()
				{
					try
					{
						Thread.sleep( 500 );
					}
					catch( InterruptedException e )
					{
						e.printStackTrace();
					}

					stop();

					try
					{
						mockService.start();
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}

				}
			} );
		}
	}

	// TODO remove this duplication. Look at WsdlMockDispatcher
	public String getOverviewUrl()
	{
		return mockService.getPath() + "?WSDL";
	}


	public void start() throws Exception
	{
		if( running )
			return;

		mockContext.reset();
		mockService.runStartScript( mockContext, this );

		SoapUI.getMockEngine().startMockService( this );
		running = true;

		MockRunListener[] mockRunListeners = mockService.getMockRunListeners();

		for( MockRunListener listener : mockRunListeners )
		{
			listener.onMockRunnerStart( this );
		}
	}

	public void setLogEnabled( boolean logEnabled )
	{
		dispatcher.setLogEnabled( logEnabled );
	}

	@Override
	public void clearResults()
	{
		dispatcher.clearResults();
	}


	public void setMaxResults( long maxNumberOfResults )
	{
		dispatcher.setMaxResults( maxNumberOfResults );
	}
}
