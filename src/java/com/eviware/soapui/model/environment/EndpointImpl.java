package com.eviware.soapui.model.environment;

import com.eviware.soapui.config.EndpointConfig;

public class EndpointImpl implements Endpoint
{

	private EndpointConfig config;
	private Service service;

	public EndpointImpl( EndpointConfig config, Service service )
	{
		this.setConfig( config );
		this.service = service;
	}

	public Service getService()
	{
		return service;
	}

	public void setService( Service service )
	{
		this.service = service;
	}

	public void setConfig( EndpointConfig config )
	{
		this.config = config;
	}

	public EndpointConfig getConfig()
	{
		return config;
	}

}
