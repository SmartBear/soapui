/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.soapui.impl.wsdl.submit.transports.jms;

import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;

public class JMSEndpoint
{
	public static final String JMS_ENDPOINT_SEPARATOR = "/";
	public static final String QUEUE_ENDPOINT_PREFIX = "queue_";
	public static final String TOPIC_ENDPOINT_PREFIX = "topic_";
	public static final String JMS_EMPTY_DESTIONATION = "-";
	public static final String JMS_ENDPIONT_PREFIX = "jms://";
	Request request;
	SubmitContext submitContext;
	String[] parameters;
	String sessionName;
	String send;
	String receive;

	public JMSEndpoint( Request request, SubmitContext submitContext )
	{
		this.request = request;
		this.submitContext = submitContext;
		parameters = extractEndpointParameters( request );
		sessionName = getEndpointParameter( 0 );
		send = getEndpointParameter( 1 );
		receive = getEndpointParameter( 2 );
	}

	
	public static String[] extractEndpointParameters( Request request )
	{
		String[] parameters = request.getEndpoint().replaceFirst( JMS_ENDPIONT_PREFIX, "" )
				.split( JMS_ENDPOINT_SEPARATOR );
		return parameters;
	}

	private String getEndpointParameter( int i )
	{
		if( i > parameters.length - 1 )
			return null;
		String stripParameter = PropertyExpander.expandProperties( submitContext, parameters[i] ).replaceFirst(
				QUEUE_ENDPOINT_PREFIX, "" ).replaceFirst( TOPIC_ENDPOINT_PREFIX, "" );
		return stripParameter;
	}
	
	public String getSessionName()
	{
		return sessionName;
	}

	public String getSend()
	{
		return send;
	}

	public String getReceive()
	{
		return receive;
	}
}
