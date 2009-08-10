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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.eviware.soapui.config.OldRestRequestConfig;
import com.eviware.soapui.config.RestMethodConfig;
import com.eviware.soapui.config.RestResourceConfig;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.RestRequestConverter;
import com.eviware.soapui.impl.rest.support.RestUtils;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
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
	private List<RestMethod> methods = new ArrayList<RestMethod>();
	private List<RestResource> resources = new ArrayList<RestResource>();
	private RestResource parentResource;
	private XmlBeansRestParamsTestPropertyHolder params;

	public RestResource( RestService service, RestResourceConfig resourceConfig )
	{
		super( resourceConfig, service, "/rest_resource.gif" );

		if( resourceConfig.getParameters() == null )
			resourceConfig.addNewParameters();

		params = new XmlBeansRestParamsTestPropertyHolder( this, resourceConfig.getParameters() );

		for( RestMethodConfig config : resourceConfig.getMethodList() )
		{
			methods.add( new RestMethod( this, config ) );
		}

		for( RestResourceConfig config : resourceConfig.getResourceList() )
		{
			resources.add( new RestResource( this, config ) );
		}

		for( OldRestRequestConfig config : resourceConfig.getRequestList() )
		{
			RestRequestConverter.convert( this, config );
		}
		resourceConfig.setRequestArray( new OldRestRequestConfig[] {} );

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

		result.addAll( getRestMethodList() );
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
		for( RestMethod m : methods )
		{
			if( index < m.getRequestCount() )
				return m.getRequestAt( index );
			else
				index -= m.getRequestCount();
		}
		throw new IndexOutOfBoundsException();
	}

	public RestRequest getRequestByName( String name )
	{
		for( RestMethod m : methods )
		{
			RestRequest r = m.getRequestByName( name );
			if( r != null )
				return r;
		}
		return null;
	}

	public RestMethod addNewMethod( String name )
	{
		RestMethodConfig methodConfig = getConfig().addNewMethod();
		methodConfig.setName( name );

		RestMethod method = new RestMethod( this, methodConfig );
		/*
		 * for (RestParamProperty prop : getDefaultParams()) { if
		 * (!method.hasProperty(prop.getName()))
		 * method.addProperty(prop.getName()).setValue(prop.getDefaultValue()); }
		 */
		methods.add( method );

		notifyPropertyChanged( "childMethods", null, method );
		return method;
	}

	public int getRestMethodCount()
	{
		return methods.size();
	}

	public List<RestMethod> getRestMethodList()
	{
		return new ArrayList<RestMethod>( methods );
	}

	public RestMethod getRestMethodByName( String name )
	{
		return ( RestMethod )getWsdlModelItemByName( methods, name );
	}

	public int getRequestCount()
	{
		int size = 0;
		for( RestMethod m : methods )
			size += m.getRequestCount();
		return size;
	}

	public List<Request> getRequestList()
	{
		List<Request> rs = new ArrayList<Request>();
		for( RestMethod m : methods )
			rs.addAll( m.getRequestList() );
		return rs;
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

	public RestParamsPropertyHolder getParams()
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

	public RestMethod cloneMethod( RestMethod method, String name )
	{
		RestMethodConfig methodConfig = ( RestMethodConfig )getConfig().addNewMethod().set( method.getConfig() );
		methodConfig.setName( name );

		RestMethod newMethod = new RestMethod( this, methodConfig );
		methods.add( newMethod );

		notifyPropertyChanged( "childMethods", null, newMethod );
		return newMethod;
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
		for( RestMethod method : methods )
		{
			method.release();
		}
	}

	public void deleteMethod( RestMethod method )
	{
		int ix = methods.indexOf( method );
		if( !methods.remove( method ) )
			return;
		notifyPropertyChanged( "childMethods", method, null );
		method.release();
		getConfig().removeMethod( ix );
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

	public RestMethod getRestMethodAt( int c )
	{
		return methods.get( c );
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

	public List<TestProperty> getPropertyList()
	{
		return params.getPropertyList();
	}

}
