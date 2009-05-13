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
import com.eviware.soapui.impl.wsdl.support.soap.SoapVersion;
import com.eviware.soapui.impl.wsdl.support.wsa.WsaUtils;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;

/**
 * Adds WS-A headers to the specified WsdlRequests requestContent
 * 
 * @author dragica.soldo
 */

public class AddWsaHeadersToRequestAction extends AbstractAction
{
	private final WsdlRequest request;

	public AddWsaHeadersToRequestAction( WsdlRequest request )
	{
		super( "Add WS-A headers" );
		this.request = request;
	}

	public void actionPerformed( ActionEvent e )
	{
		try
		{
			SoapVersion soapVersion = request.getOperation().getInterface().getSoapVersion();
			String content = request.getRequestContent();
			WsaUtils wsaUtils = new WsaUtils( content, soapVersion, request.getOperation(),
					new DefaultPropertyExpansionContext( request ) );
			content = wsaUtils.addWSAddressingRequest( request );
			request.setRequestContent( content );
		}
		catch( Exception e1 )
		{
			SoapUI.logError( e1 );
		}
	}
}
