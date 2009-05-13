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

package com.eviware.soapui.impl.wsdl.actions.mockresponse;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.support.wss.WssUtils;
import com.eviware.soapui.support.UISupport;

/**
 * Removes all WSS outgoing Tokens from the specified MockResponse
 * requestContent
 * 
 * @author dragica.soldo
 */

public class RemoveAllOutgoingWSSFromMockResponseAction extends AbstractAction
{
	private final WsdlMockResponse response;

	public RemoveAllOutgoingWSSFromMockResponseAction( WsdlMockResponse response )
	{
		super( "Remove all outgoing wss" );
		this.response = response;
	}

	public void actionPerformed( ActionEvent e )
	{
		try
		{
			if( UISupport.confirm( "Remove all outgoing wss", "Remove all outgoing wss" ) )
			{
				String content = response.getResponseContent();
				response.setResponseContent( WssUtils.removeWSSOutgoing( content, response ) );
			}
		}
		catch( Exception e1 )
		{
			SoapUI.logError( e1 );
		}

	}
}