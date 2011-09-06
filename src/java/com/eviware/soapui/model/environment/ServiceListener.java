package com.eviware.soapui.model.environment;

public interface ServiceListener
{

	public void endpointChanged( Endpoint oldEndpoint, Endpoint newEndpoint );

}
