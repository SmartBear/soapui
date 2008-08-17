/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.config.RestResourceConfig;
import com.eviware.soapui.config.RestServiceConfig;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.iface.Operation;

/**
 * WSDL implementation of Interface, maps to a WSDL Binding
 * 
 * @author Ole.Matzura
 */

public class RestService extends AbstractInterface<RestServiceConfig> implements RestResourceContainer
{
	private List<RestResource> resources = new ArrayList<RestResource>();
	
	public RestService( WsdlProject project, RestServiceConfig serviceConfig )
	{
		super( serviceConfig, project, "/rest_service.gif" );
		
		List<RestResourceConfig> resourceConfigs = serviceConfig.getResourceList();
		for( int i = 0; i < resourceConfigs.size(); i++ )
		{
			resources.add( new RestResource( this, resourceConfigs.get( i ) ) );
		}
	}

	public String getInterfaceType()
	{
		return RestServiceFactory.REST_TYPE;
	}

	public RestResource getOperationAt(int index)
	{
		return resources.get(index);
	}

	public RestResource getOperationByName( String name )
	{
		return ( RestResource ) getWsdlModelItemByName( resources, name );
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
		getConfig().setBasePath(basePath);
		
		notifyPropertyChanged("basePath", old, basePath);
	}
	
	public String getWadlUrl()
	{
		return getConfig().getDefinitionUrl();
	}
	
	public void setWadlUrl( String wadlUrl )
	{
		String old = getWadlUrl();
		getConfig().setDefinitionUrl(wadlUrl);
		
		notifyPropertyChanged("wadlUrl", old, wadlUrl);
	}
	
	public String getTechnicalId()
	{
		return getConfig().getBasePath();
	}

	public RestResource addNewResource(String name, String path)
	{
		RestResourceConfig resourceConfig = getConfig().addNewResource();
		resourceConfig.setName(name);
		resourceConfig.setPath(path);
		
		RestResource resource = new RestResource( this, resourceConfig);
		resources.add( resource );
		
		fireOperationAdded(resource);
		return resource;
	}

	public RestResource cloneResource(RestResource resource, String name)
	{
		RestResourceConfig resourceConfig = (RestResourceConfig) getConfig().addNewResource().set(resource.getConfig());
		resourceConfig.setName(name);
		
		RestResource newResource = new RestResource( this, resourceConfig);
		resources.add( newResource );
		
		fireOperationAdded(newResource);
		return newResource;
	}

	public void deleteResource(RestResource resource)
	{
		if( !resources.remove(resource))
			return;

		fireOperationRemoved(resource);
		
		resource.release();
	}

	public RestResource[] getAllResources()
	{
		return resources.toArray(new RestResource[resources.size()]);
	}
}
