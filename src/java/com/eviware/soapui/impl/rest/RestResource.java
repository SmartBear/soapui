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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.eviware.soapui.config.RestRequestConfig;
import com.eviware.soapui.config.RestResourceConfig;
import com.eviware.soapui.config.RestResourceRepresentationConfig;
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
	MutableTestPropertyHolder, RestResourceContainer
{
	public static final String PATH_PROPERTY = "path";
	private List<RestRequest> requests = new ArrayList<RestRequest>();
	private List<RestResource> resources = new ArrayList<RestResource>();
	private List<RestRepresentation> representations = new ArrayList<RestRepresentation>();
	private RestResource parentResource;
	private XmlBeansRestParamsTestPropertyHolder params;
	
   public RestResource( RestService service, RestResourceConfig resourceConfig )
   {
   	super( resourceConfig, service, "/rest_resource.gif" );
   	
   	for( RestRequestConfig config : resourceConfig.getRequestList())
   	{
   		requests.add( new RestRequest( this, config, false ));
   	}
   	
   	for( RestResourceConfig config : resourceConfig.getResourceList())
   	{
   		resources.add( new RestResource( this, config ));
   	}
   	
   	for( RestResourceRepresentationConfig config : resourceConfig.getRepresentationList() )
   	{
   		representations.add( new RestRepresentation( this, config ));
   	}
   	
   	if( resourceConfig.getParameters() == null )
   		resourceConfig.addNewParameters();
   	
   	params = new XmlBeansRestParamsTestPropertyHolder( this, resourceConfig.getParameters());
   }

	public RestResource(RestResource restResource, RestResourceConfig config)
	{
		this( restResource.getInterface(), config );
		this.parentResource = restResource;
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
		
		result.addAll( getRequestList());
		result.addAll( getResourceList());
		
		return result;
	}

	public RestRepresentation [] getRequestRepresentations()
	{
		List<RestRepresentation> result = new ArrayList<RestRepresentation>();
		
		for( RestRepresentation representation : representations )
		{
			if( !representation.isResponse())
				result.add( representation );
		}
		
		return result.toArray( new RestRepresentation[result.size()] );
	}

	public RestRepresentation [] getResponseRepresentations()
	{
		List<RestRepresentation> result = new ArrayList<RestRepresentation>();
		
		for( RestRepresentation representation : representations )
		{
			if( representation.isResponse())
				result.add( representation );
		}
		
		return result.toArray( new RestRepresentation[result.size()] );
	}
	
	public RestRepresentation getRepresentationById( String id )
	{
		for( RestRepresentation representation : representations )
		{
			if( id.equals(representation.getId()))
				return representation;
		}
		
		return null;
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
		return (RestService) getParent();
	}
	
	public String [] getRequestMediaTypes()
	{
		return new String[0];
	}
	
	public String [] getResponseMediaTypes()
	{
		return new String[0];
	}

	public RestResource getResourcetAt(int index)
	{
		return resources.get(index);
	}

	public RestResource getResourceByName(String name)
	{
		return ( RestResource ) getWsdlModelItemByName( resources, name );
	}
	
	public RestResource addNewResource(String name, String path)
	{
		RestResourceConfig resourceConfig = getConfig().addNewResource();
		resourceConfig.setName(name);
		resourceConfig.setPath(path);
		
		RestResource resource = new RestResource( this, resourceConfig);
		resources.add( resource );
		
		getInterface().fireOperationAdded( resource );
		
		return resource;
	}

	public int getResourceCount()
	{
		return resources.size();
	}

	public List<RestResource> getResourceList()
	{
		return new ArrayList<RestResource>( resources );
	}

	public RestRequest getRequestAt(int index)
	{
		return requests.get(index);
	}

	public RestRequest getRequestByName(String name)
	{
		return ( RestRequest ) getWsdlModelItemByName( requests, name );
	}
	
	public RestRequest addNewRequest(String name)
	{
		RestRequestConfig resourceConfig = getConfig().addNewRequest();
		resourceConfig.setName(name);
		
		RestRequest request = new RestRequest( this, resourceConfig, false);
		requests.add( request );
		
		for( RestParamProperty prop : getDefaultParams())
		{
			RestParamProperty p = request.addProperty(prop.getName());
			p.setValue(prop.getValue());
			p.setStyle(prop.getStyle());
		}
		
		 String[] endpoints = getInterface().getEndpoints();
		 if( endpoints.length > 0 )
			 request.setEndpoint(endpoints[0]);
		
		getInterface().fireRequestAdded(request);
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
		getConfig().setPath(path);
		notifyPropertyChanged("path", old, path);
		
		for( String param : RestUtils.extractTemplateParams(path))
		{
			if( !hasProperty(param))
				addProperty(param);
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

	public AttachmentEncoding getAttachmentEncoding(String part, boolean isRequest)
	{
		return AttachmentEncoding.NONE;
	}

	public RestParamProperty[] getDefaultParams()
	{
		List<RestParamProperty> result = new ArrayList<RestParamProperty>();
		
		if( parentResource != null )
			result.addAll( Arrays.asList( parentResource.getDefaultParams() ));
		
		for( int c = 0; c < getPropertyCount(); c++ )
			result.add( getPropertyAt(c));
		
		return result.toArray( new RestParamProperty[result.size()]);
	}

	public String getFullPath()
	{
		String base = parentResource == null ? getInterface().getBasePath() : parentResource.getFullPath();
		String path = getPath();
		if( StringUtils.hasContent(path) && base != null && !base.endsWith("/") && !path.startsWith("/"))
			base += "/";
		
		return path == null ? base : base + path;
	}
	
	public RestParamProperty addProperty(String name)
	{
		return params.addProperty(name);
	}

	public void moveProperty(String propertyName, int targetIndex)
	{
		params.moveProperty(propertyName, targetIndex);
	}

	public RestParamProperty removeProperty(String propertyName)
	{
		return params.removeProperty(propertyName);
	}

	public boolean renameProperty(String name, String newName)
	{
		return params.renameProperty(name, newName);
	}

	public void addTestPropertyListener(TestPropertyListener listener)
	{
		params.addTestPropertyListener(listener);
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

	public RestParamProperty getProperty(String name)
	{
		return params.getProperty(name);
	}

	public RestParamProperty getPropertyAt(int index)
	{
		return params.getPropertyAt(index);
	}

	public int getPropertyCount()
	{
		return params.getPropertyCount();
	}

	public String[] getPropertyNames()
	{
		return params.getPropertyNames();
	}

	public String getPropertyValue(String name)
	{
		return params.getPropertyValue(name);
	}

	public boolean hasProperty(String name)
	{
		return params.hasProperty(name);
	}

	public void removeTestPropertyListener(TestPropertyListener listener)
	{
		params.removeTestPropertyListener(listener);
	}

	public void setPropertyValue(String name, String value)
	{
		params.setPropertyValue(name, value);
	}

	public String getPropertiesLabel()
	{
		return "Resource Params";
	}

	public String buildPath(PropertyExpansionContext context)
	{
		return getFullPath();
	}

	public void removeRequest(RestRequest request)
	{
	}
	
	public RestRequest cloneRequest( RestRequest request, String name )
	{
		RestRequestConfig requestConfig = (RestRequestConfig) getConfig().addNewRequest().set(request.getConfig());
		requestConfig.setName(name);
		
		RestRequest newRequest = new RestRequest( this, requestConfig, false);
		requests.add( newRequest );
		
		getInterface().fireRequestAdded(newRequest);
		return newRequest;
	}

	public RestResource cloneResource(RestResource resource, String name)
	{
		RestResourceConfig resourceConfig = (RestResourceConfig) getConfig().addNewResource().set(resource.getConfig());
		resourceConfig.setName(name);
		
		RestResource newResource = new RestResource( this, resourceConfig);
		resources.add( newResource );
		
		getInterface().fireOperationAdded( newResource );
		return newResource;
	}

	@Override
	public void release()
	{
		super.release();
		params.release();
		
		for( RestResource resource : resources )
		{
			resource.release();
		}
		
		for( RestRequest request : requests )
		{
			request.release();
		}
		
		for( RestRepresentation representation : representations )
		{
			representation.release();
		}
	}

	public void deleteResource(RestResource resource)
	{
		if( !resources.remove(resource))
			return;

		getInterface().fireOperationRemoved(resource);
		
		resource.release();
	}

	public String createRequest(boolean b)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String createResponse(boolean b)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
