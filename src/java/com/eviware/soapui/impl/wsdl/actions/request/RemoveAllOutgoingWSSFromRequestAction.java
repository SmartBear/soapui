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

package com.eviware.soapui.impl.wsdl.actions.request;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.wss.WssUtils;
import com.eviware.soapui.support.UISupport;

/**
 * Removes all WSS outgoing Tokens from the specified WsdlRequests
 * requestContent
 * 
 * @author dragica.soldo
 */

public class RemoveAllOutgoingWSSFromRequestAction extends AbstractAction
{
	private final WsdlRequest request;

	public RemoveAllOutgoingWSSFromRequestAction( WsdlRequest request )
	{
		super( "Remove all outgoing wss" );
		this.request = request;
	}

	public void actionPerformed( ActionEvent e )
	{
		try
		{
			if( UISupport.confirm( "Remove all outgoing wss", "Remove all outgoing wss" ) )
			{
				String content = request.getRequestContent();
				request.setRequestContent( WssUtils.removeWSSOutgoing( content, request ) );
			}
		}
		catch( Exception e1 )
		{
			SoapUI.logError( e1 );
		}

	}
}