package com.eviware.soapui.impl.wsdl.monitor;

import java.net.URL;

import com.eviware.soapui.impl.wsdl.submit.AbstractWsdlMessageExchange;
import com.eviware.soapui.impl.wsdl.support.wss.IncomingWss;

public abstract class WsdlMonitorMessageExchange extends AbstractWsdlMessageExchange
{
	public abstract URL getTargetUrl();

	public abstract void discard();

	public abstract String getRequestHost();

	public abstract long getRequestContentLength();

	public abstract long getResponseContentLength();

	public abstract void prepare(IncomingWss incomingRequestWss, IncomingWss incomingResponseWss);
}
