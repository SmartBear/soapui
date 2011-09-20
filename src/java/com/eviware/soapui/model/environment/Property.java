package com.eviware.soapui.model.environment;

public interface Property
{

	public void setEnvironment( Environment environment );

	public Environment getEnvironment();

	public String getName();

	public String getValue();

	public void release();

}
