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

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.support.soap.SoapUtils;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.xml.XmlUtils;

/**
 * Recreates a WsdlRequest from its WsdlOperations schema definition
 * 
 * @author Ole.Matzura
 */

public class RecreateRequestAction extends AbstractAction
{
	private final WsdlRequest request;

	public RecreateRequestAction( WsdlRequest request )
	{
		super( "Recreate request" );
		this.request = request;
		putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/recreate_request.gif" ) );
		putValue( Action.SHORT_DESCRIPTION, "Recreates a default request from the schema" );
	}

	public void actionPerformed( ActionEvent e )
	{
		boolean createOptional = request.getSettings().getBoolean(
				WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS );
		if( !createOptional )
		{
			Boolean create = UISupport.confirmOrCancel( "Create optional elements in schema?", "Create Request" );
			if( create == null )
				return;

			createOptional = create.booleanValue();
		}

		WsdlOperation wsdlOperation = ( WsdlOperation )request.getOperation();
		String req = wsdlOperation.createRequest( createOptional );
		if( req == null )
		{
			UISupport.showErrorMessage( "Request creation failed" );
			return;
		}

		if( request.getRequestContent() != null && request.getRequestContent().trim().length() > 0 )
		{
			if( UISupport.confirm( "Keep existing values", "Recreate Request" ) )
			{
				req = SoapUtils.transferSoapHeaders( request.getRequestContent(), req, wsdlOperation.getInterface()
						.getSoapVersion() );

				req = XmlUtils.transferValues( request.getRequestContent(), req );
			}
		}

		request.setRequestContent( req );
	}
}