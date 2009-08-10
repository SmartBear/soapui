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

package com.eviware.soapui.impl.wsdl.monitor;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;

public interface MonitorListener
{
	void onRequest(SoapMonitor soapMonitor, ServletRequest request, ServletResponse response );
	
	void onMessageExchange( WsdlMonitorMessageExchange messageExchange );

	void beforeProxy( SoapMonitor soapMonitor, ServletRequest request, ServletResponse response, HttpMethod method,
			HostConfiguration hostConfiguration );

	void afterProxy( SoapMonitor soapMonitor, ServletRequest request, ServletResponse response, HttpMethod method,
			WsdlMonitorMessageExchange capturedData );
}
