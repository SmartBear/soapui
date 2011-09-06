package com.eviware.soapui.model.environment;

public interface Service
{

	public void setEnvironment( Environment environment );

	public Environment getEnvironment();

	public void setEndpoint( Endpoint endpoint );

	public Endpoint getEndpoint();

	public void release();

}
