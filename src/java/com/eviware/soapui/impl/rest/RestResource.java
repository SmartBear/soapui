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

package com.eviware.soapui.impl.rest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eviware.soapui.config.RestMethodConfig;
import com.eviware.soapui.config.RestResourceConfig;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder.RestParamProperty;
import com.eviware.soapui.impl.support.AbstractHttpOperation;
import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Attachment.AttachmentEncoding;
import com.eviware.soapui.model.propertyexpansion.PropertyExpansionContext;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.model.testsuite.TestPropertyListener;
import com.eviware.soapui.support.StringUtils;

/**
 * WSDL implementation of Operation, maps to a WSDL BindingOperation
 * 
 * @author Ole.Matzura
 */

public class RestResource extends AbstractWsdlModelItem<RestResourceConfig> implements AbstractHttpOperation,
		MutableTestPropertyHolder, RestResourceContainer, PropertyChangeListener
{
	public static final String PATH_PROPERTY = "path";
	private List<RestRequest> requests = new ArrayList<RestRequest>();
	private List<RestResource> resources = new ArrayList<RestResource>();
	private RestResource parentResource;
	private XmlBeansRestParamsTestPropertyHolder params;

	public RestResource( RestService service, RestResourceConfig resourceConfig )
	{
		super( resourceConfig, service, "/rest_resource.gif" );

		for( RestMethodConfig config : resourceConfig.getRequestList() )
		{
			requests.add( new RestRequest( this, config, false ) );
		}

		for( RestResourceConfig config : resourceConfig.getResourceList() )
		{
			resources.add( new RestResource( this, config ) );
		}

		if( resourceConfig.getParameters() == null )
			resourceConfig.addNewParameters();

		params = new XmlBeansRestParamsTestPropertyHolder( this, resourceConfig.getParameters() );

		service.addPropertyChangeListener( this );
	}

	public RestResource( RestResource restResource, RestResourceConfig config )
	{
		this( restResource.getInterface(), config );
		this.parentResource = restResource;

		parentResource.addPropertyChangeListener( this );
	}

	public RestResource getParentResource()
	{
		return parentResource;
	}

	public RestResourceContainer getResourceContainer()
	{
		return parentResource == null ? getInterface() : parentResource;
	}

	public List<? extends ModelItem> getChildren()
	{
		List<ModelItem> result = new ArrayList<ModelItem>();

		result.addAll( getRequestList() );
		result.addAll( getChildResourceList() );

		return result;
	}

	public MessagePart[] getDefaultRequestParts()
	{
		return new MessagePart[0];
	}

	public MessagePart[] getDefaultResponseParts()
	{
		return new MessagePart[0];
	}

	public RestService getInterface()
	{
		return ( RestService )getParent();
	}

	public String[] getRequestMediaTypes()
	{
		return new String[0];
	}

	public String[] getResponseMediaTypes()
	{
		return new String[0];
	}

	public RestResource getChildResourcetAt( int index )
	{
		return resources.get( index );
	}

	public RestResource getChildResourceByName( String name )
	{
		return ( RestResource )getWsdlModelItemByName( resources, name );
	}

	public RestResource addNewChildResource( String name, String path )
	{
		RestResourceConfig resourceConfig = getConfig().addNewResource();
		resourceConfig.setName( name );
		resourceConfig.setPath( path );

		RestResource resource = new RestResource( this, resourceConfig );
		resources.add( resource );

		getInterface().fireOperationAdded( resource );

		notifyPropertyChanged( "childResources", null, resource );

		return resource;
	}

	public int getChildResourceCount()
	{
		return resources.size();
	}

	public List<RestResource> getChildResourceList()
	{
		return new ArrayList<RestResource>( resources );
	}

	public RestRequest getRequestAt( int index )
	{
		return requests.get( index );
	}

	public RestRequest getRequestByName( String name )
	{
		return ( RestRequest )getWsdlModelItemByName( requests, name );
	}

	public RestRequest addNewRequest( String name )
	{
		RestMethodConfig resourceConfig = getConfig().addNewRequest();
		resourceConfig.setName( name );

		RestRequest request = new RestRequest( this, resourceConfig, false );
		requests.add( request );

		for( RestParamProperty prop : getDefaultParams() )
		{
			if( !request.hasProperty( prop.getName() ) )
				request.addProperty( prop );
		}

		String[] endpoints = getInterface().getEndpoints();
		if( endpoints.length > 0 )
			request.setEndpoint( endpoints[0] );

		getInterface().fireRequestAdded( request );
		return request;
	}

	public int getRequestCount()
	{
		return requests.size();
	}

	public List<Request> getRequestList()
	{
		return new ArrayList<Request>( requests );
	}

	public String getPath()
	{
		return getConfig().getPath();
	}

	public void setPath( String path )
	{
		String old = getPath();
		getConfig().setPath( path );
		notifyPropertyChanged( "path", old, path );

		for( String param : RestUtils.extractTemplateParams( path ) )
		{
			if( !hasProperty( param ) )
				addProperty( param );
		}
	}

	public boolean isBidirectional()
	{
		return false;
	}

	public boolean isNotification()
	{
		return false;
	}

	public boolean isOneWay()
	{
		return false;
	}

	public boolean isRequestResponse()
	{
		return true;
	}

	public boolean isSolicitResponse()
	{
		return false;
	}

	public boolean isUnidirectional()
	{
		return false;
	}

	public AttachmentEncoding getAttachmentEncoding( String part, boolean isRequest )
	{
		return AttachmentEncoding.NONE;
	}

	public RestParamProperty[] getDefaultParams()
	{
		List<RestParamProperty> result = new ArrayList<RestParamProperty>();
		Set<String> names = new HashSet<String>();

		if( parentResource != null )
			result.addAll( Arrays.asList( parentResource.getDefaultParams() ) );

		for( int c = 0; c < getPropertyCount(); c++ )
		{
			if( names.contains( getPropertyAt( c ).getName() ) )
				continue;

			result.add( getPropertyAt( c ) );
			names.add( getPropertyAt( c ).getName() );
		}

		return result.toArray( new RestParamProperty[result.size()] );
	}

	public String getFullPath()
	{
		return getFullPath( true );
	}

	public String getFullPath( boolean includeBasePath )
	{
		String base = parentResource == null ? ( includeBasePath ? getInterface().getBasePath() : "" ) : parentResource
				.getFullPath( includeBasePath );

		String path = getPath();
		if( StringUtils.hasContent( path ) && base != null && !base.endsWith( "/" ) && !path.startsWith( "/" ) )
			base += "/";

		return path == null ? base : base + path;
	}

	public RestParamProperty addProperty( String name )
	{
		return params.addProperty( name );
	}

	public void moveProperty( String propertyName, int targetIndex )
	{
		params.moveProperty( propertyName, targetIndex );
	}

	public RestParamProperty removeProperty( String propertyName )
	{
		return params.removeProperty( propertyName );
	}

	public boolean renameProperty( String name, String newName )
	{
		return params.renameProperty( name, newName );
	}

	public void addTestPropertyListener( TestPropertyListener listener )
	{
		params.addTestPropertyListener( listener );
	}

	public XmlBeansRestParamsTestPropertyHolder getParams()
	{
		return params;
	}

	public ModelItem getModelItem()
	{
		return this;
	}

	public Map<String, TestProperty> getProperties()
	{
		return params.getProperties();
	}

	public RestParamProperty getProperty( String name )
	{
		return params.getProperty( name );
	}

	public RestParamProperty getPropertyAt( int index )
	{
		return params.getPropertyAt( index );
	}

	public int getPropertyCount()
	{
		return params.getPropertyCount();
	}

	public List<TestProperty> getPropertyList()
	{
		return params.getPropertyList();
	}

	public String[] getPropertyNames()
	{
		return params.getPropertyNames();
	}

	public String getPropertyValue( String name )
	{
		return params.getPropertyValue( name );
	}

	public boolean hasProperty( String name )
	{
		return params.hasProperty( name );
	}

	public void removeTestPropertyListener( TestPropertyListener listener )
	{
		params.removeTestPropertyListener( listener );
	}

	public void setPropertyValue( String name, String value )
	{
		params.setPropertyValue( name, value );
	}

	public String getPropertiesLabel()
	{
		return "Resource Params";
	}

	public String buildPath( PropertyExpansionContext context )
	{
		return getFullPath( true );
	}

	public void removeRequest( RestRequest request )
	{
		int ix = requests.indexOf( request );
		requests.remove( ix );

		try
		{
			( getInterface() ).fireRequestRemoved( request );
		}
		finally
		{
			request.release();
			getConfig().removeRequest( ix );
		}
	}

	public RestRequest cloneRequest( RestRequest request, String name )
	{
		RestMethodConfig requestConfig = ( RestMethodConfig )getConfig().addNewRequest().set( request.getConfig() );
		requestConfig.setName( name );

		RestRequest newRequest = new RestRequest( this, requestConfig, false );
		requests.add( newRequest );

		getInterface().fireRequestAdded( newRequest );
		return newRequest;
	}

	public RestResource cloneChildResource( RestResource resource, String name )
	{
		return cloneResource( resource, name );
	}

	public RestResource cloneResource( RestResource resource, String name )
	{
		RestResourceConfig resourceConfig = ( RestResourceConfig )getConfig().addNewResource().set( resource.getConfig() );
		resourceConfig.setName( name );

		RestResource newResource = new RestResource( this, resourceConfig );
		resources.add( newResource );

		getInterface().fireOperationAdded( newResource );
		return newResource;
	}

	@Override
	public void release()
	{
		super.release();
		params.release();
		getService().removePropertyChangeListener( this );
		if( parentResource != null )
			parentResource.removePropertyChangeListener( this );

		for( RestResource resource : resources )
		{
			resource.release();
		}

		for( RestRequest request : requests )
		{
			request.release();
		}
	}

	public void deleteChildResource( RestResource resource )
	{
		deleteResource( resource );
	}

	public void deleteResource( RestResource resource )
	{
		int ix = resources.indexOf( resource );
		if( !resources.remove( resource ) )
			return;

		getInterface().fireOperationRemoved( resource );

		notifyPropertyChanged( "childResources", resource, null );

		getConfig().removeResource( ix );
		resource.release();
	}

	public String createRequest( boolean b )
	{
		return null;
	}

	public String createResponse( boolean b )
	{
		return null;
	}

	public RestResource getChildResourceAt( int c )
	{
		return resources.get( c );
	}

	public RestService getService()
	{
		return ( RestService )( getParentResource() == null ? getParent() : getParentResource().getService() );
	}

	public void propertyChange( PropertyChangeEvent evt )
	{
		if( evt.getPropertyName().equals( "path" ) || evt.getPropertyName().equals( "basePath" ) )
		{
			notifyPropertyChanged( "path", null, getPath() );
		}
	}

	public RestResource[] getAllChildResources()
	{
		List<RestResource> result = new ArrayList<RestResource>();
		for( RestResource resource : resources )
		{
			addResourcesToResult( resource, result );
		}

		return result.toArray( new RestResource[result.size()] );
	}

	private void addResourcesToResult( RestResource resource, List<RestResource> result )
	{
		result.add( resource );

		for( RestResource res : resource.getChildResourceList() )
		{
			addResourcesToResult( res, result );
		}
	}

	public Map<String, RestRequest> getRequests()
	{
		Map<String, RestRequest> result = new HashMap<String, RestRequest>();

		for( RestRequest request : requests )
		{
			result.put( request.getName(), request );
		}

		return result;
	}
}
