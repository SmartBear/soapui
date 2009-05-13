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

/**
 * Listener for MockRunner events
 * 
 * @author ole.matzura
 */

public interface MockRunListener
{
	public void onMockRunnerStart( MockRunner mockRunner );

	public void onMockResult( MockResult result );

	public void onMockRunnerStop( MockRunner mockRunner );

	/**
	 * Called before dispatching a request. If a MockResult is returned, further
	 * dispatching is aborted and the returned result is used.
	 * 
	 * @param runner
	 * @param request
	 * @param response
	 * @return an optional MockResult, null if dispatching should move on as
	 *         usual
	 */

	public MockResult onMockRequest( MockRunner runner, HttpServletRequest request, HttpServletResponse response );
}
