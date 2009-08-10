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

package com.eviware.soapui.impl.wsdl.support.wsa;

import com.eviware.soapui.impl.wsdl.WsdlOperation;

public class WsaContainerImpl implements WsaContainer
{

	WsdlOperation operation;
	WsaConfig wsaConfig;
	boolean enabled = false;

	public WsdlOperation getOperation()
	{
		return operation;
	}

	public WsaConfig getWsaConfig()
	{
		return wsaConfig;
	}

	public boolean isWsaEnabled()
	{
		return enabled;
	}

	public void setWsaEnabled( boolean arg0 )
	{
		enabled = arg0;

	}

	public void setOperation( WsdlOperation operation )
	{
		this.operation = operation;

	}

}
