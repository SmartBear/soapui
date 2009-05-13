/*
 * soapUI, copyright (C) 2004-2009 eviware.com
 *
 * soapUI is free software; you can redistribute it and/or modify it under the
 * terms of version 2.1 of the GNU Lesser General Public License as published by
 * the Free Software Foundation.
 *
 * soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.mock.dispatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;
import com.eviware.soapui.model.mock.MockResult;
import com.eviware.soapui.model.mock.MockRunListener;
import com.eviware.soapui.model.mock.MockRunner;

public class SequenceMockOperationDispatcher extends AbstractMockOperationDispatcher implements MockRunListener
{
	private int currentDispatchIndex;

	public SequenceMockOperationDispatcher( WsdlMockOperation mockOperation )
	{
		super( mockOperation );

		mockOperation.getMockService().addMockRunListener( this );
	}

	public WsdlMockResponse selectMockResponse( WsdlMockRequest request, WsdlMockResult result )
	{
		synchronized( result.getMockOperation() )
		{
			if( currentDispatchIndex >= getMockOperation().getMockResponseCount() )
				currentDispatchIndex = 0;

			WsdlMockResponse mockResponse = getMockOperation().getMockResponseAt( currentDispatchIndex );

			currentDispatchIndex++ ;
			return mockResponse;
		}
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
		public MockOperationDispatcher build( WsdlMockOperation mockOperation )
		{
			return new SequenceMockOperationDispatcher( mockOperation );
		}
	}
}
