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

package com.eviware.soapui.impl.wsdl.panels.teststeps;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.Future;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.teststeps.JdbcRequestTestStep;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Submit;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.iface.SubmitListener;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.SoapUIException;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;

public class JdbcSubmit implements Submit, Runnable
{
	private volatile Future<?> future;
	private SubmitContext context;
	private Status status;
	private SubmitListener[] listeners;
	private Exception error;
	private long timestamp;
	protected ResultSet resultSet;
	protected Statement statement;
	private Connection connection;
	private long timeTaken;
	private final JdbcRequest request;
	private JdbcResponse response;

	public JdbcSubmit( JdbcRequest request, SubmitContext submitContext, boolean async )
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

		if( async )
			future = SoapUI.getThreadPool().submit( this );
		else
			run();
	}

	public void cancel()
	{
		if( status == Status.CANCELED )
			return;

		JdbcRequest.logger.info( "Canceling request.." );
		if( status == Status.RUNNING )
		{
			cancelQuery();
		}

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

	public Request getRequest()
	{
		return request;
	}

	public JdbcResponse getResponse()
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
			runQuery();

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

	protected void runQuery() throws Exception
	{
		prepare();
		load();
		createResponse();
	}

	public void cancelQuery()
	{
		try
		{
			if( statement != null )
				statement.cancel();
		}
		catch( SQLFeatureNotSupportedException e )
		{
			UISupport.showErrorMessage( e );
		}
		catch( SQLException ex )
		{
			UISupport.showErrorMessage( ex );
		}
	}

	protected void getDatabaseConnection() throws Exception, SQLException
	{
		String drvr = "";
		String connStr = "";
		String pass = "";
		JdbcRequestTestStep testStep = request.getTestStep();

		if( !StringUtils.isNullOrEmpty( testStep.getDriver() )
				&& !StringUtils.isNullOrEmpty( testStep.getConnectionString() ) )
		{
			drvr = PropertyExpander.expandProperties( context, testStep.getDriver() ).trim();
			connStr = PropertyExpander.expandProperties( context, testStep.getConnectionString() ).trim();
			pass = PropertyExpander.expandProperties( context, testStep.getPassword() ).trim();
		}
		else
		{
			UISupport.showErrorMessage( "Please supply connection settings for all DataSources" );
			throw new SoapUIException( "Please supply connection settings" );
		}
		connStr = connStr.replaceFirst( JdbcRequestTestStep.PASS_TEMPLATE, pass );
		try
		{
			DriverManager.getDriver( connStr );
		}
		catch( SQLException e )
		{
			try
			{
				Class.forName( drvr ).newInstance();
			}
			catch( Exception e1 )
			{
				throw new Exception( "Failed to init connection for drvr [" + drvr + "], connectionString ["
						+ testStep.getConnectionString() + "]" );
			}
		}

		resultSet = null;
		connection = DriverManager.getConnection( connStr );
	}

	protected void load() throws Exception
	{
		try
		{
			JdbcRequestTestStep testStep = request.getTestStep();

			if( testStep.isStoredProcedure() )
			{
				timestamp = System.currentTimeMillis();
				( ( CallableStatement )statement ).execute();
			}
			else
			{
				List<TestProperty> props = testStep.getPropertyList();
				for( int j = 0; j < props.size(); j++ )
				{
					TestProperty property = props.get( j );
					( ( PreparedStatement )statement ).setString( j + 1, property.getValue() );
				}
				timestamp = System.currentTimeMillis();
				( ( PreparedStatement )statement ).execute();
			}
		}
		finally
		{
			timeTaken = System.currentTimeMillis() - timestamp;
		}
	}

	protected void prepare() throws Exception
	{
		JdbcRequestTestStep testStep = request.getTestStep();
		getDatabaseConnection();
		if( testStep.isStoredProcedure() )
		{
			String sql = PropertyExpander.expandProperties( context, testStep.getQuery() );

			if( !sql.startsWith( "{call " ) && !sql.endsWith( "}" ) )
				sql = "{call " + sql + "}";

			statement = connection.prepareCall( sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY );
		}
		else
		{
			String sql = PropertyExpander.expandProperties( context, testStep.getQuery() );
			statement = connection.prepareStatement( sql );
		}

		try
		{
			String queryTimeout = PropertyExpander.expandProperties( testStep, testStep.getQueryTimeout() );
			statement.setQueryTimeout( Integer.parseInt( queryTimeout ) );
		}
		catch( NumberFormatException e )
		{
		}

		try
		{
			String maxRows = PropertyExpander.expandProperties( testStep, testStep.getMaxRows() );
			statement.setMaxRows( Integer.parseInt( maxRows ) );
		}
		catch( NumberFormatException e )
		{
		}
	}

	protected String createResponse()
	{
		try
		{
			response = new JdbcResponse( request, statement );
			response.setTimestamp( timestamp );
			response.setTimeTaken( timeTaken );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if( connection != null )
					connection.close();
				if( statement != null )
					statement.close();
				if( resultSet != null )
					resultSet.close();
			}
			catch( Exception e )
			{
			}
		}
		return null;
	}
}