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

import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.model.mock.MockRequest;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.model.mock.MockResult;

public class RandomMockOperationDispatcher extends AbstractMockOperationDispatcher
{
	public RandomMockOperationDispatcher( MockOperation mockOperation )
	{
		super( mockOperation );
	}

	public MockResponse selectMockResponse( MockRequest request, MockResult result )
	{
		synchronized( result.getMockOperation() )
		{
			synchronized( this )
			{
				int currentDispatchIndex = ( int )( ( Math.random() * getMockOperation().getMockResponseCount() ) + 0.5F );

				if( currentDispatchIndex >= getMockOperation().getMockResponseCount() )
					currentDispatchIndex = 0;

				return getMockOperation().getMockResponseAt( currentDispatchIndex );
			}
		}
	}

	public static class Factory implements MockOperationDispatchFactory
	{
		public MockOperationDispatcher build( MockOperation mockOperation )
		{
			return new RandomMockOperationDispatcher( mockOperation );
		}
	}
}
