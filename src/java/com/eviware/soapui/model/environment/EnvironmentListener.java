package com.eviware.soapui.model.environment;

public interface EnvironmentListener
{

	public void serviceAdded( Service service );

	public void serviceRemoved( Service service );

}
