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

package com.eviware.soapui.impl.wsdl.monitor.jettyproxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.io.Buffer;
import org.mortbay.jetty.client.HttpExchange;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;

import com.eviware.soapui.impl.wsdl.monitor.JProxyServletWsdlMonitorMessageExchange;
import com.eviware.soapui.impl.wsdl.monitor.SoapMonitor;

public class SoapUIHttpExchange extends HttpExchange
{

	private Continuation continuation;
	private InputStream in;
	private OutputStream out;
	private HttpServletResponse httpResponse;
	@SuppressWarnings("unused")
	private Logger log = Logger.getLogger(SoapUIHttpExchange.class);
	private JProxyServletWsdlMonitorMessageExchange capturedMessage;
	@SuppressWarnings("unused")
	private SoapMonitor monitor;

	public SoapUIHttpExchange(SoapMonitor monitor, HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			CaptureInputStream capture, JProxyServletWsdlMonitorMessageExchange capturedData) throws IOException
	{
		this.httpResponse = httpResponse;
		continuation = ContinuationSupport.getContinuation(httpRequest, httpRequest);
		in = capture;
		out = httpResponse.getOutputStream();
		this.capturedMessage = capturedData;
		this.monitor = monitor;
	}

	@Override
	protected void onConnectionFailed(Throwable ex)
	{
//		log.warn("onConnectionFailed");
		try
		{
			httpResponse.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, ex.getMessage());
			httpResponse.flushBuffer();
			this.capturedMessage.stopCapture();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onException(Throwable ex)
	{
//		log.error("onException" + ex.getMessage());
		try
		{
			httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
			httpResponse.flushBuffer();
			this.capturedMessage.stopCapture();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onRequestCommitted() throws IOException
	{
//		log.info("onRequestCommitted()");
		this.capturedMessage.setRequest(((CaptureInputStream) in).getCapturedData());
	}

	@Override
	protected void onRequestComplete() throws IOException
	{
//		log.info("onRequestComplete()");
		this.capturedMessage.setRequest(((CaptureInputStream) in).getCapturedData());
	}

	@Override
	protected void onResponseComplete() throws IOException
	{
//		log.info("onResponseComplete()");
		continuation.resume();
		this.capturedMessage.stopCapture();
	}

	@Override
	protected void onResponseContent(Buffer content) throws IOException
	{
//		log.info("onResponseContent()");
		byte[] buffer = new byte[content.length()];
		while (content.hasContent())
		{
			int len = content.get(buffer, 0, buffer.length);
			this.capturedMessage.setResponse(buffer);
			out.write(buffer, 0, len); // May block here for a little bit!
		}
	}

	@Override
	protected void onResponseHeaderComplete() throws IOException
	{
//		log.info("onResponseCompleteHeader()");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException
	{
		if (reason != null && reason.length() > 0)
			httpResponse.setStatus(status, reason.toString());
		else
			httpResponse.setStatus(status);

	}

	@Override
	protected void onResponseHeader(Buffer name, Buffer value) throws IOException
	{
		httpResponse.addHeader(name.toString(), value.toString());
		capturedMessage.setResponseHeader(name.toString(), value.toString());
	}

}
