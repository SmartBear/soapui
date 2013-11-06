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

package com.eviware.soapui.impl.support;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.EndpointsConfig;
import com.eviware.soapui.config.InterfaceConfig;
import com.eviware.soapui.impl.support.definition.support.AbstractDefinitionContext;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.iface.InterfaceListener;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;

public abstract class AbstractInterface<T extends InterfaceConfig> extends AbstractWsdlModelItem<T> implements
		Interface
{
	private Set<InterfaceListener> interfaceListeners = new HashSet<InterfaceListener>();

	protected AbstractInterface( T config, ModelItem parent, String icon )
	{
		super( config, parent, icon );

		if( config.getEndpoints() == null )
			config.addNewEndpoints();

		for( InterfaceListener listener : SoapUI.getListenerRegistry().getListeners( InterfaceListener.class ) )
		{
			addInterfaceListener( listener );
		}

		if( !config.isSetDefinitionCache() )
			config.addNewDefinitionCache();
	}

	public WsdlProject getProject()
	{
		return ( WsdlProject )getParent();
	}

	public T getConfig()
	{
		return super.getConfig();
	}

	public List<? extends ModelItem> getChildren()
	{
		return getOperationList();
	}

	public String[] getEndpoints()
	{
		EndpointsConfig endpoints = getConfig().getEndpoints();
		List<String> endpointArray = endpoints.getEndpointList();
		return endpointArray.toArray( new String[endpointArray.size()] );
	}

	public void addEndpoint( String endpoint )
	{
		if( endpoint == null || endpoint.trim().length() == 0 )
			return;

		endpoint = endpoint.trim();
		String[] endpoints = getEndpoints();

		// dont add the same endpoint twice
		if( Arrays.asList( endpoints ).contains( endpoint ) )
			return;

		getConfig().getEndpoints().addNewEndpoint().setStringValue( endpoint );

		notifyPropertyChanged( ENDPOINT_PROPERTY, null, endpoint );
	}

	public void changeEndpoint( String oldEndpoint, String newEndpoint )
	{
		if( oldEndpoint == null || oldEndpoint.trim().length() == 0 )
			return;
		if( newEndpoint == null || newEndpoint.trim().length() == 0 )
			return;

		EndpointsConfig endpoints = getConfig().getEndpoints();

		for( int c = 0; c < endpoints.sizeOfEndpointArray(); c++ )
		{
			if( endpoints.getEndpointArray( c ).equals( oldEndpoint ) )
			{
				endpoints.setEndpointArray( c, newEndpoint );
				notifyPropertyChanged( ENDPOINT_PROPERTY, oldEndpoint, newEndpoint );
				break;
			}
		}
	}

	public void removeEndpoint( String endpoint )
	{
		EndpointsConfig endpoints = getConfig().getEndpoints();

		for( int c = 0; c < endpoints.sizeOfEndpointArray(); c++ )
		{
			if( endpoints.getEndpointArray( c ).equals( endpoint ) )
			{
				endpoints.removeEndpoint( c );
				notifyPropertyChanged( ENDPOINT_PROPERTY, endpoint, null );
				break;
			}
		}
	}

	public void fireOperationAdded( Operation operation )
	{
		InterfaceListener[] a = interfaceListeners.toArray( new InterfaceListener[interfaceListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].operationAdded( operation );
		}
	}

	public void fireOperationUpdated( Operation operation )
	{
		InterfaceListener[] a = interfaceListeners.toArray( new InterfaceListener[interfaceListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].operationUpdated( operation );
		}
	}

	public void fireOperationRemoved( Operation operation )
	{
		InterfaceListener[] a = interfaceListeners.toArray( new InterfaceListener[interfaceListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].operationRemoved( operation );
		}
	}

	public void fireRequestAdded( Request request )
	{
		InterfaceListener[] a = interfaceListeners.toArray( new InterfaceListener[interfaceListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].requestAdded( request );
		}
	}

	public void fireRequestRemoved( Request request )
	{
		InterfaceListener[] a = interfaceListeners.toArray( new InterfaceListener[interfaceListeners.size()] );

		for( int c = 0; c < a.length; c++ )
		{
			a[c].requestRemoved( request );
		}
	}

	public void addInterfaceListener( InterfaceListener listener )
	{
		interfaceListeners.add( listener );
	}

	public void removeInterfaceListener( InterfaceListener listener )
	{
		interfaceListeners.remove( listener );
	}

	@Override
	public void release()
	{
		super.release();

		interfaceListeners.clear();
	}

	@SuppressWarnings( "unchecked" )
	public abstract AbstractDefinitionContext getDefinitionContext();

	/**
	 * Return the URL for the current definition (ie a WSDL or WADL url)
	 */

	public abstract String getDefinition();

	public abstract String getType();

	public abstract boolean isDefinitionShareble();

	public Operation[] getAllOperations()
	{
		return getOperationList().toArray( new Operation[getOperationCount()] );
	}
}
