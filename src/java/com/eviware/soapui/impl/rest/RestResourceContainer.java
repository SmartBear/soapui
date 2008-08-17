package com.eviware.soapui.impl.rest;

public interface RestResourceContainer
{
	public String getName();

	public void deleteResource(RestResource resource);

	public RestResource cloneResource(RestResource resource, String name);

}
