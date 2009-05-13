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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eviware.soapui.impl.wsdl.mock.DispatchException;

/**
 * Runner for a MockService
 * 
 * @author ole.matzura
 */

public interface MockRunner
{
	public void stop();

	public int getMockResultCount();

	public MockResult getMockResultAt( int c );

	public MockService getMockService();

	public MockResult dispatchRequest( HttpServletRequest request, HttpServletResponse response )
			throws DispatchException;

	public boolean isRunning();
}
