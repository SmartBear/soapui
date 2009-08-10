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

package com.eviware.soapui.impl.support.wsa;

import com.eviware.soapui.config.HttpRequestConfig;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.support.http.HttpRequest;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaConfig;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaContainer;
import com.eviware.soapui.impl.wsdl.support.wsrm.WsrmConfig;
import com.eviware.soapui.impl.wsdl.support.wsrm.WsrmContainer;

public class WsaRequest extends HttpRequest implements WsaContainer, WsrmContainer
{

	private WsaConfig wsaConfig;
	private WsrmConfig wsrmConfig;
	private WsdlOperation operation;
	private boolean wsrmEnabled;

	public WsaRequest( HttpRequestConfig httpRequestConfig, WsaConfig wsaConfig, WsrmConfig wsrmConfig,
			boolean forLoadTest )
	{
		super( httpRequestConfig, forLoadTest );
		this.setWsaConfig( wsaConfig );
		this.setWsrmConfig( wsrmConfig );
	}

	public void setWsaConfig( WsaConfig wsaConfig )
	{
		this.wsaConfig = wsaConfig;
	}

	public WsaConfig getWsaConfig()
	{
		return wsaConfig;
	}

	public boolean isWsaEnabled()
	{
		return wsaConfig.isWsaEnabled();
	}

	public void setWsaEnabled( boolean arg0 )
	{
		wsaConfig.setWsaEnabled( arg0 );

	}

	public WsdlOperation getOperation()
	{
		return operation;
	}

	public RestRequestInterface.RequestMethod getMethod()
	{
		return RestRequestInterface.RequestMethod.POST;
	}

	public void setOperation( WsdlOperation operation )
	{
		this.operation = operation;

	}

	public WsrmConfig getWsrmConfig()
	{
		return wsrmConfig;
	}

	public boolean isWsrmEnabled()
	{
		return wsrmEnabled;
	}

	public void setWsrmEnabled( boolean arg0 )
	{
		wsrmEnabled = arg0;

	}

	public void setWsrmConfig( WsrmConfig wsrmConfig )
	{
		this.wsrmConfig = wsrmConfig;
	}
}
