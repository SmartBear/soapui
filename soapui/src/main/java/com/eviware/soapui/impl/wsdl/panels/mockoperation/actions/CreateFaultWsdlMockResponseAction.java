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
import com.eviware.soapui.model.iface.MessagePart;
import com.eviware.soapui.model.iface.MessagePart.FaultPart;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.support.UISupport;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates an SOAP Fault response message for WsdlMockResponse
 * 
 * @author ole.matzura
 */

public class CreateFaultWsdlMockResponseAction extends AbstractAction
{
	private final MockResponse mockResponse;

	public CreateFaultWsdlMockResponseAction( MockResponse mockResponse )
	{
		super( "Create Fault" );
		this.mockResponse = mockResponse;
		putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/create_empty_fault.gif" ) );
		putValue( Action.SHORT_DESCRIPTION, "Creates an SOAP Fault response" );
	}

	public void actionPerformed( ActionEvent e )
	{
		WsdlOperation operation = ( WsdlOperation )mockResponse.getMockOperation().getOperation();
		if( operation == null )
		{
			UISupport.showErrorMessage( "Missing operation for this mock response" );
			return;
		}

		if( UISupport.confirm( "Overwrite current response with a fault message?", "Create Fault" ) )
		{
			WsdlInterface iface = operation.getInterface();
			MessagePart[] faultParts = operation.getFaultParts();

			if( faultParts != null && faultParts.length > 0 )
			{
				List<String> names = new ArrayList<String>();
				for( int c = 0; c < faultParts.length; c++ )
					names.add( faultParts[c].getName() );

				String faultName = UISupport.prompt( "Select fault detail to generate", "Create Fault", names );
				if( faultName != null )
				{
					FaultPart faultPart = ( FaultPart )faultParts[names.indexOf( faultName )];
					mockResponse.setResponseContent( iface.getMessageBuilder().buildFault( faultPart ) );
				}
			}
			else
			{
				mockResponse.setResponseContent( iface.getMessageBuilder().buildEmptyFault() );
			}
		}
	}
}
