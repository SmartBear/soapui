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

package com.eviware.soapui.model.mock;

import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.types.StringToStringsMap;

import java.io.IOException;

/**
 * Resulting MessageExchange for a request to a MockService
 * 
 * @author ole.matzura
 */

public interface MockResult
{
	public MockRequest getMockRequest();

	public StringToStringsMap getResponseHeaders();

	public String getResponseContent();

	public MockResponse getMockResponse();

	public MockOperation getMockOperation();

	public ActionList getActions();

	public long getTimeTaken();

	public long getTimestamp();

	public void finish();

	public byte[] getRawResponseData();

	public void addHeader( String name, String value );

	public boolean isCommitted();

	public void setResponseContent( String responseContent );

	public void setContentType( String contentTypeHttpHeader );

	public void writeRawResponseData( byte[] data ) throws IOException;
}
