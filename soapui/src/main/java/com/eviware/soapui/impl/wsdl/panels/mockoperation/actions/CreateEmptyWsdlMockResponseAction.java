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

package com.eviware.soapui.impl.wsdl.panels.mockoperation.actions;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.support.UISupport;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Creates an empty SOAP response message for WsdlMockResponse
 * 
 * @author ole.matzura
 */

public class CreateEmptyWsdlMockResponseAction extends AbstractAction
{
	private final MockResponse mockResponse;

	public CreateEmptyWsdlMockResponseAction( MockResponse mockResponse )
	{
		super( "Create Empty" );
		this.mockResponse = mockResponse;
		putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/create_empty_request.gif" ) );
		putValue( Action.SHORT_DESCRIPTION, "Creates an empty SOAP response" );
	}

	public void actionPerformed( ActionEvent e )
	{
		//FIXME for rest mocking action
		WsdlOperation operation = ( WsdlOperation )mockResponse.getMockOperation().getOperation();

		if( operation == null )
		{
			UISupport.showErrorMessage( "Missing operation for this mock response" );
			return;
		}

		if( UISupport.confirm( "Overwrite current response with empty one?", "Create Empty" ) )
		{
			WsdlInterface iface = operation.getInterface();
			mockResponse.setResponseContent( iface.getMessageBuilder().buildEmptyMessage() );
		}
	}
}
