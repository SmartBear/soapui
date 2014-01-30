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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.mock.MockResponse;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.UISupport;

/**
 * Recreates an SOAP response message for WsdlMockResponse from its WSDL/XSD
 * definition
 * 
 * @author ole.matzura
 */

public class RecreateMockResponseAction extends AbstractAction
{
	private final MockResponse mockResponse;

	public RecreateMockResponseAction( MockResponse mockResponse )
	{
		super( "Recreate response" );
		this.mockResponse = mockResponse;
		putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/recreate_request.gif" ) );
		putValue( Action.SHORT_DESCRIPTION, "Recreates a default response from the schema" );
	}

	public void actionPerformed( ActionEvent arg0 )
	{
		Operation operation = mockResponse.getMockOperation().getOperation();
		if( operation == null )
		{
			UISupport.showErrorMessage( "Missing operation for this mock response" );
			return;
		}

		String response = mockResponse.getResponseContent();
		if( response != null && response.trim().length() > 0
				&& !UISupport.confirm( "Overwrite current response?", "Recreate response" ) )
			return;

		boolean createOptional = mockResponse.getSettings().getBoolean(
				WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS );
		if( !createOptional )
			createOptional = UISupport.confirm( "Create optional elements in schema?", "Create Request" );

		String req = operation.createResponse( createOptional );
		if( req == null )
		{
			UISupport.showErrorMessage( "Response creation failed" );
			return;
		}

		mockResponse.setResponseContent( req );
	}

}
