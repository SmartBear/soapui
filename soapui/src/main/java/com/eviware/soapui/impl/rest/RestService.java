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

package com.eviware.soapui.impl.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eviware.soapui.config.RestResourceConfig;
import com.eviware.soapui.config.RestServiceConfig;
import com.eviware.soapui.impl.rest.panels.request.inspectors.schema.InferredSchemaManager;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wadl.WadlDefinitionContext;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.Constants;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.StringUtils;

/**
 * WSDL implementation of Interface, maps to a WSDL Binding
 * 
 * @author Ole.Matzura
 */

public class RestService extends AbstractInterface<RestServiceConfig> implements RestResourceContainer
{
	private List<RestResource> resources = new ArrayList<RestResource>();
	private WadlDefinitionContext wadlContext;
   private boolean exportChanges = false;

	public RestService( WsdlProject project, RestServiceConfig serviceConfig )
	{
		super( serviceConfig, project, "/rest_service.gif" );

		for( RestResourceConfig resourceConfig : serviceConfig.getResourceList() )
		{
			resources.add( new RestResource( this, resourceConfig ) );
		}

		if( !serviceConfig.isSetWadlVersion() )
		{
			serviceConfig.setWadlVersion( serviceConfig.isSetDefinitionUrl() ? Constants.WADL10_NS : Constants.WADL11_NS );
		}
	}

	public String getWadlVersion()
	{
		return getConfig().getWadlVersion();
	}

	public String getInterfaceType()
	{
		return RestServiceFactory.REST_TYPE;
	}

	public RestResource getOperationAt( int index )
	{
		return resources.get( index );
	}

	public RestResource getOperationByName( String name )
	{
		return ( RestResource )getWsdlModelItemByName( resources, name );
	}

	public int getOperationCount()
	{
		return resources.size();
	}

	public List<Operation> getOperationList()
	{
		return new ArrayList<Operation>( resources );
	}

	public String getBasePath()
	{
		return getConfig().isSetBasePath() ? getConfig().getBasePath() : "";
	}

	public void setBasePath( String basePath )
	{
		String old = getBasePath();
		getConfig().setBasePath( basePath );

		notifyPropertyChanged( "basePath", old, basePath );
	}

	public String getInferredSchema()
	{
		return getConfig().getInferredSchema();
	}

	public void setInferredSchema( String inferredSchema )
	{
		String old = getInferredSchema();
		getConfig().setInferredSchema( inferredSchema );

		notifyPropertyChanged( "inferredSchema", old, inferredSchema );
	}

	public boolean isGenerated()
	{
		return StringUtils.isNullOrEmpty( getConfig().getDefinitionUrl() );
	}

	public String getWadlUrl()
	{
		return isGenerated() ? generateWadlUrl() : getConfig().getDefinitionUrl();
	}

	public String generateWadlUrl()
	{
		return getName() + ".wadl";
	}

	public void setWadlUrl( String wadlUrl )
	{
		String old = getWadlUrl();
		getConfig().setDefinitionUrl( wadlUrl );

		notifyPropertyChanged( "wadlUrl", old, wadlUrl );
	}

	public String getTechnicalId()
	{
		return getConfig().getBasePath();
	}

	public RestResource addNewResource( String name, String path )
	{
		RestResourceConfig resourceConfig = getConfig().addNewResource();
		resourceConfig.setName( name );
		resourceConfig.setPath( path );

		RestResource resource = new RestResource( this, resourceConfig );
		resources.add( resource );

		fireOperationAdded( resource );
		return resource;
	}

	public RestResource cloneResource( RestResource resource, String name )
	{
		RestResourceConfig resourceConfig = ( RestResourceConfig )getConfig().addNewResource().set( resource.getConfig() );
		resourceConfig.setName( name );

		RestResource newResource = new RestResource( this, resourceConfig );
		resources.add( newResource );

		fireOperationAdded( newResource );
		return newResource;
	}

	public void deleteResource( RestResource resource )
	{
		resource.deleteAllChildResources( resource );

		int ix = resources.indexOf( resource );
		if( !resources.remove( resource ) )
			return;

		fireOperationRemoved( resource );

		getConfig().removeResource( ix );
		resource.release();
	}

	public List<RestResource> getAllResources()
	{
		List<RestResource> result = new ArrayList<RestResource>();
		for( RestResource resource : resources )
		{
			addResourcesToResult( resource, result );
		}

		return result;
	}

	public Map<String, RestResource> getResources()
	{
		Map<String, RestResource> result = new HashMap<String, RestResource>();

		for( RestResource resource : getAllResources() )
		{
			result.put( resource.getFullPath( false ), resource );
		}

		return result;
	}

	private void addResourcesToResult( RestResource resource, List<RestResource> result )
	{
		result.add( resource );

		for( RestResource res : resource.getChildResourceList() )
		{
			addResourcesToResult( res, result );
		}
	}

	public RestResource getResourceByFullPath( String resourcePath )
	{
		for( RestResource resource : getAllResources() )
		{
			if( resource.getFullPath().equals( resourcePath ) )
				return resource;
		}

		return null;
	}

	public RestResource[] getResourcesByFullPath( String resourcePath )
	{
		List<RestResource> result = new ArrayList<RestResource>();

		for( RestResource resource : getAllResources() )
		{
			if( resource.getFullPath().equals( resourcePath ) )
			{
				result.add( resource );
			}
		}

		return result.toArray( new RestResource[result.size()] );
	}

	@Override
	public WadlDefinitionContext getDefinitionContext()
	{
		return getWadlContext();
	}

	public WadlDefinitionContext getWadlContext()
	{
		if( wadlContext == null )
			wadlContext = new WadlDefinitionContext( getWadlUrl(), this );

		return wadlContext;
	}

	@Override
	public String getDefinition()
	{
		return getWadlUrl();
	}

	public String getType()
	{
		return RestServiceFactory.REST_TYPE;
	}

	public boolean isDefinitionShareble()
	{
		return !isGenerated();
	}

	@Override
	public Operation[] getAllOperations()
	{
		List<RestResource> restResources = getAllResources();
		return restResources.toArray( new Operation[restResources.size()] );
	}

	public void beforeSave()
	{
		super.beforeSave();

		if( isGenerated() && wadlContext != null )
		{
			try
			{
				wadlContext.getDefinitionCache().clear();
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}

	}

	@Override
	public void release()
	{
		InferredSchemaManager.release( this );
		super.release();
	}

	public List<RestResource> getResourceList()
	{
		return new ArrayList<RestResource>( resources );
	}

	public boolean exportChanges()
	{
		return exportChanges;
	}

	public void setExportChanges( boolean exportChanges )
	{
		this.exportChanges = exportChanges;
	}

}
