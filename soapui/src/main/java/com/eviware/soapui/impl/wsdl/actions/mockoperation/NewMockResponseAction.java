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

package com.eviware.soapui.impl.wsdl.actions.mockoperation;

import com.eviware.soapui.impl.rest.mock.RestMockAction;
import com.eviware.soapui.impl.support.AbstractMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.model.mock.MockOperation;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Creates a new WsdlMockResponse in the specified WsdlMockOperation
 * 
 * @author ole.matzura
 */

public class NewMockResponseAction extends AbstractSoapUIAction<AbstractMockOperation>
{
	public static final String SOAPUI_ACTION_ID = "NewMockResponseAction";

	public NewMockResponseAction()
	{
		super( "New MockResponse", "Creates a new MockResponse for this MockOperation" );
	}

	public void perform( AbstractMockOperation mockOperation, Object param )
	{
		String name = UISupport.prompt( "Enter name of new MockResponse", getName(),
				"Response " + ( mockOperation.getMockResponseCount() + 1 ) );

		if( name != null )
		{
			if( mockOperation instanceof WsdlMockOperation )
			{
				boolean shouldCreateResponse = true;
				UISupport.showDesktopPanel( ((WsdlMockOperation)mockOperation).addNewMockResponse( name, shouldCreateResponse ) );
			}
			else if( mockOperation instanceof RestMockAction )
			{
				UISupport.showDesktopPanel( ((RestMockAction)mockOperation).addNewMockResponse( name ) );
			}
		}
	}
}
