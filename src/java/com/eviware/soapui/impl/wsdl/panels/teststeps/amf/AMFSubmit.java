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

package com.eviware.soapui.impl.wsdl.panels.teststeps.amf;

import java.util.List;
import java.util.concurrent.Future;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.support.UISupport;

import flex.messaging.io.amf.client.AMFConnection;
import flex.messaging.io.amf.client.exceptions.ClientStatusException;
import flex.messaging.io.amf.client.exceptions.ServerStatusException;

public class AMFSubmit implements Submit, Runnable
{
	private volatile Future<?> future;
	private SubmitContext context;
	private Status status;
	private SubmitListener[] listeners;
	private Exception error;
	private long timestamp;
	private long timeTaken;
	private final AMFRequest request;
	private AMFResponse response;

	public AMFSubmit( AMFRequest request, SubmitContext submitContext, boolean async )
	{
		this.request = request;
		this.context = submitContext;

		List<SubmitListener> regListeners = SoapUI.getListenerRegistry().getListeners( SubmitListener.class );

		SubmitListener[] submitListeners = request.getSubmitListeners();
		this.listeners = new SubmitListener[submitListeners.length + regListeners.size()];
		for( int c = 0; c < submitListeners.length; c++ )
			this.listeners[c] = submitListeners[c];

		for( int c = 0; c < regListeners.size(); c++ )
			this.listeners[submitListeners.length + c] = regListeners.get( c );

		error = null;
		status = Status.INITIALIZED;
		timestamp = System.currentTimeMillis();

		if( async )
			future = SoapUI.getThreadPool().submit( this );
		else
			run();
	}

	public void cancel()
	{
		if( status == Status.CANCELED )
			return;

		AMFRequest.logger.info( "Canceling request.." );

		status = Status.CANCELED;

		for( int i = 0; i < listeners.length; i++ )
		{
			try
			{
				listeners[i].afterSubmit( this, context );
			}
			catch( Throwable e )
			{
				SoapUI.logError( e );
			}
		}
	}

	public Exception getError()
	{
		return error;
	}

	public AMFRequest getRequest()
	{
		return request;
	}

	public AMFResponse getResponse()
	{
		return response;
	}

	public Status getStatus()
	{
		return status;
	}

	public Status waitUntilFinished()
	{
		if( future != null )
		{
			if( !future.isDone() )
			{
				try
				{
					future.get();
				}
				catch( Exception e )
				{
					SoapUI.logError( e );
				}
			}
		}
		else
			throw new RuntimeException( "cannot wait on null future" );

		return getStatus();
	}

	public void run()
	{
		try
		{
			for( int i = 0; i < listeners.length; i++ )
			{
				if( !listeners[i].beforeSubmit( this, context ) )
				{
					status = Status.CANCELED;
					System.err.println( "listener cancelled submit.." );
					return;
				}
			}

			status = Status.RUNNING;
			Object responseContent = executeAmfCall( getRequest() );
			context.setProperty( AMFRequest.AMF_RESPONSE_CONTENT, responseContent );

			timeTaken = System.currentTimeMillis() - timestamp;
			createResponse();
			if( status != Status.CANCELED )
			{
				status = Status.FINISHED;
			}
		}
		catch( Exception e )
		{
			UISupport.showErrorMessage( "There's been an error in executing query " + e.toString() );
			error = e;
		}
		finally
		{

			if( status != Status.CANCELED )
			{
				for( int i = 0; i < listeners.length; i++ )
				{
					try
					{
						listeners[i].afterSubmit( this, context );
					}
					catch( Throwable e )
					{
						SoapUI.logError( e );
					}
				}
			}
		}
	}

	protected String createResponse()
	{
		try
		{
			response = new AMFResponse( request, context );
			response.setTimestamp( timestamp );
			response.setTimeTaken( timeTaken );

		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		return null;
	}

	public Object executeAmfCall( AMFRequest amfRequest ) throws ClientStatusException, ServerStatusException
	{
		AMFConnection amfConnection = new AMFConnection();
		amfConnection.setInstantiateTypes( false );

		try
		{
			amfConnection.connect( amfRequest.getEndpoint() );
			Object result = amfConnection.call( amfRequest.getAmfCall(), amfRequest.argumentsToArray() );

			return result;
		}
		finally
		{
			amfRequest.clearArguments();
			amfConnection.close();
		}

	}
}