package com.eviware.soapui.model.environment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.EnvironmentConfig;
import com.eviware.soapui.config.ServiceConfig;
import com.eviware.soapui.config.TestSuiteConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.model.project.Project;
import com.eviware.soapui.model.support.ProjectListenerAdapter;

public class EnvironmentImpl extends AbstractWsdlModelItem<EnvironmentConfig> implements Environment
{

	private final static Logger log = Logger.getLogger( EnvironmentImpl.class );
	private List<ServiceImpl> services;
	private Project project;
	private Set<EnvironmentListener> environmentListeners = new HashSet<EnvironmentListener>();

	public EnvironmentImpl( EnvironmentConfig config, Project project, String icon )
	{
		super( config, project, icon );
		this.project = project;

		List<ServiceConfig> serviceList = config.getServiceList();
		for( int i = 0; i < serviceList.size(); i++ )
		{
			services.add( buildService( serviceList.get( i ) ) );
		}

		// how these listeners get added to registry ??
		for( EnvironmentListener listener : SoapUI.getListenerRegistry().getListeners( EnvironmentListener.class ) )
		{
			addEnvironmentListener( listener );
		}
	}

	public ServiceImpl buildService( ServiceConfig config )
	{
		return new ServiceImpl( config, this, null );
	}

	public void setProject( Project project )
	{
		this.project = project;
	}

	public Project getProject()
	{
		return project;
	}

	public void addEnvironmentListener( EnvironmentListener listener )
	{
		environmentListeners.add( listener );
	}

	public void release()
	{
		super.release();

		for( Service service : services )
			service.release();

		environmentListeners.clear();
	}

	public void resetConfigOnMove( TestSuiteConfig testSuiteConfig )
	{
	}

	public int getServiceCount()
	{
		return services.size();
	}

	public Service getServiceAt( int index )
	{
		return services.get( index );
	}

	public Service getServiceByName( String serviceName )
	{
		return ( Service )getWsdlModelItemByName( services, serviceName );
	}

	public Service addNewService( String name )
	{
		ServiceImpl service = buildService( getConfig().addNewService() );
		service.setEnvironment( this );
		service.setName( name );
		String[] endpoints = project.getInterfaceByName( name ).getEndpoints();
		if( endpoints != null && endpoints.length > 0 )
		{
			EndpointImpl newEndpoint = new EndpointImpl( service.getConfig().addNewEndpoint(), service );
			newEndpoint.getConfig().setStringValue( endpoints[0] );
			service.setEndpoint( newEndpoint );
		}
		services.add( service );
		fireServiceAdded( service );
		return service;
	}

	public void removeService( Service service )
	{
		int ix = services.indexOf( service );
		services.remove( ix );

		try
		{
			fireServiceRemoved( service );
		}
		finally
		{
			service.release();
			getConfig().removeService( ix );
		}
	}

	public void fireServiceAdded( Service service )
	{
		EnvironmentListener[] a = environmentListeners.toArray( new EnvironmentListener[environmentListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].serviceAdded( service );
		}
	}

	public void fireServiceRemoved( Service service )
	{
		EnvironmentListener[] a = environmentListeners.toArray( new EnvironmentListener[environmentListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].serviceRemoved( service );
		}
	}

	private class InternalProjectListener extends ProjectListenerAdapter
	{

		public void environmentAdded( Environment environment )
		{
		}

		public void environmentRemoved( Environment environment )
		{
		}

		public void environmentSwitched( Environment oldEnvironment, Environment newEnvironment )
		{
		}

	}

}
