/*
 *  soapUI, copyright (C) 2004-2008 eviware.com 
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

import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

public abstract class AbstractRequestFilter implements RequestFilter
{
	public void filterRequest(SubmitContext context, Request request)
	{
		if( request instanceof AbstractHttpRequest )
			filterAbstractHttpRequest( context, (AbstractHttpRequest<?>)request );
	}

	public void filterAbstractHttpRequest( SubmitContext context, AbstractHttpRequest<?> request )
	{
		if( request instanceof WsdlRequest )
			filterWsdlRequest(context, (WsdlRequest) request );
		else if( request instanceof RestRequest )
			filterRestRequest(context, (RestRequest) request );
	}
	
	public void filterWsdlRequest(SubmitContext context, WsdlRequest request)
	{
	}
	
	public void filterRestRequest(SubmitContext context, RestRequest request)
	{
	}

	public void afterRequest( SubmitContext context, Response response )
	{
	}
}
