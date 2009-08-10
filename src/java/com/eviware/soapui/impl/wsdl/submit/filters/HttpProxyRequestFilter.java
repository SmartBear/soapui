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

package com.eviware.soapui.impl.wsdl.submit.filters;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpState;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.impl.wsdl.support.http.ProxyUtils;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
import com.eviware.soapui.model.settings.Settings;

/**
 * RequestFilter for setting proxy-specific values
 * 
 * @author Ole.Matzura
 */

public class HttpProxyRequestFilter extends AbstractRequestFilter
{
	public void filterAbstractHttpRequest( SubmitContext context, AbstractHttpRequest<?> wsdlRequest )
	{
		// init proxy settings
		Settings settings = wsdlRequest.getSettings();
		HostConfiguration hostConfiguration = ( HostConfiguration )context
				.getProperty( BaseHttpRequestTransport.HOST_CONFIGURATION );
		HttpState httpState = ( HttpState )context.getProperty( SubmitContext.HTTP_STATE_PROPERTY );

		String endpoint = PropertyExpander.expandProperties( context, wsdlRequest.getEndpoint() );
		ProxyUtils.initProxySettings( settings, httpState, hostConfiguration, endpoint, context );
	}
}
