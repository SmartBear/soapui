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

package com.eviware.soapui.impl.wsdl.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.eviware.soapui.config.MockServiceConfig;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.testsuite.TestCase;

public class MockRunnerManagerImpl implements MockRunnerManager
{
	private final static Logger log = Logger.getLogger( MockRunnerManagerImpl.class );

	private static Map<String, MockRunnerManager> managers = new HashMap<String, MockRunnerManager>();

	private Map<String, WsdlMockService> mockServices = new HashMap<String, WsdlMockService>();

	private Vector<WsdlMockRunner> mockRunners = new Vector<WsdlMockRunner>();

	private Project project;

	private MockRunnerManagerImpl( Project project )
	{
		this.project = project;
	}

	public static MockRunnerManager getInstance( TestCase testCase )
	{
		if( managers.containsKey( testCase.getId() ) )
		{
			return managers.get( testCase.getId() );
		}
		else
		{
			MockRunnerManager manager = new MockRunnerManagerImpl( testCase.getTestSuite().getProject() );
			managers.put( testCase.getId(), manager );

			return manager;
		}
	}

	public WsdlMockService getMockService( int port, String path )
	{
		String key = port + path;

		WsdlMockService service = mockServices.get( key );
		if( service == null )
		{
			MockServiceConfig mockServiceConfig = MockServiceConfig.Factory.newInstance();
			mockServiceConfig.setPath( path );
			mockServiceConfig.setPort( port );
			mockServiceConfig.setName( port + ":" + path );
			service = new WsdlMockService( project, mockServiceConfig );
			mockServices.put( key, service );
		}

		return service;
	}

	public void start() throws MockRunnerManagerException
	{
		if( log.isDebugEnabled() )
		{
			log.debug( "Starting MockRunnerManager" );
		}

		for( WsdlMockService mockService : mockServices.values() )
		{
			try
			{
				mockRunners.add( mockService.start() );
			}
			catch( Exception e )
			{
				throw new MockRunnerManagerException( "Failed to create a WsdlMockRunner", e );
			}
		}
	}

	public void stop()
	{
		if( log.isDebugEnabled() )
		{
			log.debug( "Stopping MockRunnerManager" );
		}

		for( WsdlMockRunner runner : mockRunners )
		{
			try
			{
				runner.stop();
			}
			catch( Exception e )
			{
				log.error( e );
			}
		}

		mockServices.clear();
		mockRunners.clear();
	}

	public boolean isStarted()
	{
		for( WsdlMockRunner runner : mockRunners )
		{
			if( runner.isRunning() )
			{
				return true;
			}
		}

		return false;
	}
}
