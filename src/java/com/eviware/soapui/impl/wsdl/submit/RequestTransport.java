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

package com.eviware.soapui.impl.wsdl.submit;

import com.eviware.soapui.model.iface.Request;
import com.eviware.soapui.model.iface.Response;
import com.eviware.soapui.model.iface.SubmitContext;

/**
 * Defines protocol-specific behaviour
 * 
 * @author Ole.Matzura
 */

public interface RequestTransport
{
	public final static String WSDL_REQUEST = "wsdlRequest";
	public static final String REQUEST_TRANSPORT = "requestTransport";

	public void addRequestFilter( RequestFilter filter );

	public void removeRequestFilter( RequestFilter filter );

	public void abortRequest( SubmitContext submitContext );

	public Response sendRequest( SubmitContext submitContext, Request request ) throws Exception;
}
