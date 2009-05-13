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

package com.eviware.soapui.impl.wsdl.submit.transports.http;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.RequestEntity;

import com.eviware.soapui.impl.support.AbstractHttpRequest;

public interface ExtendedHttpMethod extends HttpMethod
{
	public long getMaxSize();

	public void setMaxSize( long maxSize );

	public long getResponseReadTime();

	public void initStartTime();

	public long getTimeTaken();

	public long getStartTime();

	public SSLInfo getSSLInfo();

	public String getResponseCharSet();

	public String getResponseContentType();

	public RequestEntity getRequestEntity();

	public void setDumpFile( String dumpFile );

	public AbstractHttpRequest.RequestMethod getMethod();

	public void setFailed( Throwable t );

	public boolean isFailed();

	public Throwable getFailureCause();
}