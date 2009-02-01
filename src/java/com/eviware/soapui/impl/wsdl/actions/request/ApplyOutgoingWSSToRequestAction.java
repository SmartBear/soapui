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
import java.io.StringWriter;

import javax.swing.AbstractAction;

import org.w3c.dom.Document;

import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.wss.OutgoingWss;
import com.eviware.soapui.model.propertyexpansion.DefaultPropertyExpansionContext;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Prompts to add a WSS Username Token to the specified WsdlRequests
 * requestContent
 * 
 * @author Ole.Matzura
 */

public class ApplyOutgoingWSSToRequestAction extends AbstractAction
{
	private final WsdlRequest request;
	private final OutgoingWss outgoing;

	public ApplyOutgoingWSSToRequestAction( WsdlRequest request, OutgoingWss outgoing )
	{
		super( "Apply \" " + outgoing.getName() + " \"" );
		this.request = request;
		this.outgoing = outgoing;
	}

	public void actionPerformed( ActionEvent e )
	{
		String req = request.getRequestContent();

		try
		{
			UISupport.setHourglassCursor();
			Document dom = XmlUtils.parseXml( req );
			outgoing.processOutgoing( dom, new DefaultPropertyExpansionContext( request ) );
			StringWriter writer = new StringWriter();
			XmlUtils.serialize( dom, writer );
			request.setRequestContent( writer.toString() );
		}
		catch( Exception e1 )
		{
			UISupport.showErrorMessage( e1 );
		}
		finally
		{
			UISupport.resetCursor();
		}
	}
}