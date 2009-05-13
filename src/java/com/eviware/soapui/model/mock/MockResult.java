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

package com.eviware.soapui.model.mock;

import com.eviware.soapui.support.action.swing.ActionList;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Resulting MessageExchange for a request to a MockService
 * 
 * @author ole.matzura
 */

public interface MockResult
{
	public MockRequest getMockRequest();

	public StringToStringMap getResponseHeaders();

	public String getResponseContent();

	public MockResponse getMockResponse();

	public ActionList getActions();

	public long getTimeTaken();

	public long getTimestamp();

	public void finish();
}
