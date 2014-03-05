package com.eviware.soapui.impl.wsdl.submit.transports.http;

/**
 * Defines an object that encapsulate HTTP response status information.
 */
public interface HttpStatusHolder
{

	int getResponseStatusCode();

	String getResponseStatusLine();
}
