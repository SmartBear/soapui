/*
 *  SoapUI, copyright (C) 2004-2012 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.monitor;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import org.apache.http.HttpRequest;

public class MonitorListenerAdapter implements MonitorListener
{
	public void afterProxy( WsdlProject project, ServletRequest request, ServletResponse response, HttpRequest method,
									WsdlMonitorMessageExchange capturedData )
	{
	}

	public void beforeProxy( WsdlProject project, ServletRequest request, ServletResponse response, HttpRequest method )
	{
	}

	public void onMessageExchange( WsdlMonitorMessageExchange messageExchange )
	{
	}

	public void onRequest( WsdlProject project, ServletRequest request, ServletResponse response )
	{
	}
}
