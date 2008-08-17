package com.eviware.soapui.impl.rest;

import com.eviware.soapui.config.RestResourceRepresentationConfig;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;

public class RestRepresentation
{
	private final RestResource restResource;
	private RestResourceRepresentationConfig config;
	private XmlBeansRestParamsTestPropertyHolder params;

	public RestRepresentation(RestResource restResource, RestResourceRepresentationConfig config)
	{
		this.restResource = restResource;
		this.config = config;
		
		params = new XmlBeansRestParamsTestPropertyHolder( restResource, config.getParams() );
	}

	public RestResource getRestResource()
	{
		return restResource;
	}

	public RestResourceRepresentationConfig getConfig()
	{
		return config;
	}
	
	public XmlBeansRestParamsTestPropertyHolder getParams()
	{
		return params;
	}

	public void setConfig(RestResourceRepresentationConfig config)
	{
		this.config = config;
	}

	public String getId()
	{
		return config.getId();
	}

	public boolean isFault()
	{
		return config.getIsFault();
	}

	public boolean isResponse()
	{
		return config.getIsResponse();
	}

	public String getMediaType()
	{
		return config.getMediaType();
	}

	public String getStatus()
	{
		return config.getStatus();
	}

	public void setId(String arg0)
	{
		config.setId(arg0);
	}

	public void setFault(boolean arg0)
	{
		config.setIsFault(arg0);
	}

	public void setResponse(boolean arg0)
	{
		config.setIsResponse(arg0);
	}

	public void setMediaType(String arg0)
	{
		config.setMediaType(arg0);
	}

	public void setStatus(String arg0)
	{
		config.setStatus(arg0);
	}

	public void release()
	{
		
	}
	
	
}
