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

package com.eviware.soapui.impl.wsdl.mock.dispatch;

import com.eviware.soapui.model.mock.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SequenceMockOperationDispatcher extends AbstractMockOperationDispatcher implements MockRunListener
{
	private int currentDispatchIndex;

	public SequenceMockOperationDispatcher( MockOperation mockOperation )
	{
		super( mockOperation );

		mockOperation.getMockService().addMockRunListener( this );
	}

	public MockResponse selectMockResponse( MockRequest request, MockResult result )
	{
		if( currentDispatchIndex >= getMockOperation().getMockResponseCount() )
			currentDispatchIndex = 0;

		MockResponse mockResponse = getMockOperation().getMockResponseAt( currentDispatchIndex );

		currentDispatchIndex++ ;
		return mockResponse;
	}

	@Override
	public void release()
	{
		getMockOperation().getMockService().removeMockRunListener( this );
		super.release();
	}

	public void onMockRunnerStart( MockRunner mockRunner )
	{
		currentDispatchIndex = 0;
	}

	public void onMockResult( MockResult result )
	{
	}

	public void onMockRunnerStop( MockRunner mockRunner )
	{
	}

	public MockResult onMockRequest( MockRunner runner, HttpServletRequest request, HttpServletResponse response )
	{
		return null;
	}

	public static class Factory implements MockOperationDispatchFactory
	{
		public MockOperationDispatcher build( MockOperation mockOperation )
		{
			return new SequenceMockOperationDispatcher( mockOperation );
		}
	}
}
