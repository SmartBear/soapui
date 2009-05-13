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
import javax.swing.Action;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.support.UISupport;

/**
 * Creates an empty WsdlRequest containing a SOAP Envelope and empty Body
 * 
 * @author Ole.Matzura
 */

public class CreateEmptyRequestAction extends AbstractAction
{
	private final WsdlRequest request;

	public CreateEmptyRequestAction( WsdlRequest request )
	{
		super( "Create empty" );
		this.request = request;
		putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/create_empty_request.gif" ) );
		putValue( Action.SHORT_DESCRIPTION, "Creates an empty SOAP request" );
	}

	public void actionPerformed( ActionEvent e )
	{
		if( UISupport.confirm( "Overwrite existing request?", "Create Empty" ) )
		{
			WsdlInterface iface = ( WsdlInterface )request.getOperation().getInterface();
			request.setRequestContent( iface.getMessageBuilder().buildEmptyMessage() );
		}
	}
}