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

import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.project.EndpointStrategy;

/**
 * RequestFilter for stripping whitespaces
 * 
 * @author Ole.Matzura
 */

public class EndpointStrategyRequestFilter extends AbstractRequestFilter
{
	public void filterRequest( SubmitContext context, Request wsdlRequest )
	{
		Operation operation = wsdlRequest.getOperation();
		if( operation != null )
		{
			EndpointStrategy endpointStrategy = operation.getInterface().getProject().getEndpointStrategy();
			if( endpointStrategy != null )
				endpointStrategy.filterRequest( context, wsdlRequest );
		}
	}
}
