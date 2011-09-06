package com.eviware.soapui.model.environment;

import java.util.HashSet;
import java.util.Set;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.EndpointConfig;
import com.eviware.soapui.config.ServiceConfig;
import com.eviware.soapui.config.TestSuiteConfig;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;

public class ServiceImpl extends AbstractWsdlModelItem<ServiceConfig> implements Service
{

	private Environment environment;
	private Endpoint endpoint;
	private Set<ServiceListener> serviceListeners = new HashSet<ServiceListener>();

	public ServiceImpl( ServiceConfig config, EnvironmentImpl environment, String icon )
	{
		super( config, environment, icon );
		this.environment = environment;

		this.setEndpoint( buildEndpoint( config.getEndpoint() ) );

		// how these listeners get added to registry ??
		for( ServiceListener listener : SoapUI.getListenerRegistry().getListeners( ServiceListener.class ) )
		{
			addServiceListener( listener );
		}
	}

	public void addServiceListener( ServiceListener listener )
	{
		serviceListeners.add( listener );
	}

	public EndpointImpl buildEndpoint( EndpointConfig config )
	{
		return new EndpointImpl( config, this );
	}

	public Environment getEnvironment()
	{
		return environment;
	}

	public void setEnvironment( Environment environment )
	{
		this.environment = environment;
	}

	public void setEndpoint( Endpoint endpoint )
	{
		this.endpoint = endpoint;
	}

	public Endpoint getEndpoint()
	{
		return endpoint;
	}

	public void resetConfigOnMove( TestSuiteConfig testSuiteConfig )
	{
	}

	public void fireEndpointChanged( Endpoint oldEndpoint, Endpoint newEndpoint )
	{
		ServiceListener[] a = serviceListeners.toArray( new ServiceListener[serviceListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].endpointChanged( oldEndpoint, newEndpoint );
		}
	}

}
