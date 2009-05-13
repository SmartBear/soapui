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

package com.eviware.soapui.model.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunListener;
import com.eviware.soapui.model.mock.MockRunner;

/**
 * Adapter for MockRunListeners
 * 
 * @author ole.matzura
 */

public class MockRunListenerAdapter implements MockRunListener
{
	public void onMockRunnerStart( MockRunner mockRunner )
	{
	}

	public void onMockRunnerStop( MockRunner mockRunner )
	{
	}

	public void onMockResult( MockResult result )
	{
	}

	public MockResult onMockRequest( MockRunner runner, HttpServletRequest request, HttpServletResponse response )
	{
		return null;
	}
}
