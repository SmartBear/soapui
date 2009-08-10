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

import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
import com.eviware.soapui.impl.support.http.HttpRequestInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.impl.wsdl.submit.transports.http.BaseHttpRequestTransport;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public abstract class AbstractRequestFilter implements RequestFilter
{
	public void filterRequest( SubmitContext context, Request request )
	{
		if( request instanceof AbstractHttpRequestInterface<?> )
			filterAbstractHttpRequest( context, ( AbstractHttpRequest<?> )request );
	}

	public void filterAbstractHttpRequest( SubmitContext context, AbstractHttpRequest<?> request )
	{
		if( request instanceof WsdlRequest )
			filterWsdlRequest( context, ( WsdlRequest )request );
		else if( request instanceof RestRequestInterface )
			filterRestRequest( context, ( RestRequestInterface )request );
		else if( request instanceof HttpRequestInterface<?> )
			filterHttpRequest( context, ( HttpRequestInterface<?> )request );
	}

	public void filterWsdlRequest( SubmitContext context, WsdlRequest request )
	{
	}

	public void filterRestRequest( SubmitContext context, RestRequestInterface request )
	{
	}

	public void filterHttpRequest( SubmitContext context, HttpRequestInterface<?> request )
	{
	}

	public void afterRequest( SubmitContext context, Request request )
	{
		// do this for backwards compatibility
		Response response = ( Response )context.getProperty( BaseHttpRequestTransport.RESPONSE );
		if( response != null )
			afterRequest( context, response );

		if( request instanceof AbstractHttpRequestInterface<?> )
			afterAbstractHttpResponse( context, ( AbstractHttpRequestInterface<?> )request );
	}

	public void afterAbstractHttpResponse( SubmitContext context, AbstractHttpRequestInterface<?> request )
	{
		if( request instanceof WsdlRequest )
			afterWsdlRequest( context, ( WsdlRequest )request );
		else if( request instanceof RestRequestInterface )
			afterRestRequest( context, ( RestRequestInterface )request );
	}

	public void afterWsdlRequest( SubmitContext context, WsdlRequest request )
	{
	}

	public void afterRestRequest( SubmitContext context, RestRequestInterface request )
	{
	}

	public void afterRequest( SubmitContext context, Response response )
	{
	}
}
