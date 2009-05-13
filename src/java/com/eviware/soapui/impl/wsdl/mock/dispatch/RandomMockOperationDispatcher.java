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

import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResult;

public class RandomMockOperationDispatcher extends AbstractMockOperationDispatcher
{
	public RandomMockOperationDispatcher( WsdlMockOperation mockOperation )
	{
		super( mockOperation );
	}

	public WsdlMockResponse selectMockResponse( WsdlMockRequest request, WsdlMockResult result )
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
		public MockOperationDispatcher build( WsdlMockOperation mockOperation )
		{
			return new RandomMockOperationDispatcher( mockOperation );
		}
	}
}