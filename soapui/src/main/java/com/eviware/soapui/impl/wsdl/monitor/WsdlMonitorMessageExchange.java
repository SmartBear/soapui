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

import com.eviware.soapui.impl.wsdl.submit.AbstractWsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;
import com.eviware.soapui.model.iface.Operation;

import java.net.URL;
import java.util.Map;

public abstract class WsdlMonitorMessageExchange extends AbstractWsdlMessageExchange<Operation>
{
	public WsdlMonitorMessageExchange( Operation modelItem )
	{
		super( modelItem );
	}

	public abstract URL getTargetUrl();

	public abstract void discard();

	public abstract String getRequestHost();

	public abstract long getRequestContentLength();

	public abstract long getResponseContentLength();

	public abstract void prepare( IncomingWss incomingRequestWss, IncomingWss incomingResponseWss );

	public abstract String getRequestMethod();

	public abstract Map<String, String> getHttpRequestParameters();

	public abstract String getQueryParameters();
}
