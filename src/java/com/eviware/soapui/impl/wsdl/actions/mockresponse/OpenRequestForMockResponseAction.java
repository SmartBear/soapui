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

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.settings.WsdlSettings;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Prompts to open an existing request for the specified WsdlMockResponse
 * 
 * @author ole.matzura
 */

public class OpenRequestForMockResponseAction extends AbstractSoapUIAction<WsdlMockResponse>
{
	public static final String SOAPUI_ACTION_ID = "OpenRequestForMockResponseAction";

	public OpenRequestForMockResponseAction()
	{
		super( "Open Request", "Opens/Creates a request for this MockOperation with correct endpoint" );
	}

	public void perform( WsdlMockResponse mockResponse, Object param )
	{
		WsdlOperation operation = mockResponse.getMockOperation().getOperation();
		if( operation == null )
		{
			UISupport.showErrorMessage( "Missing operation for this mock response" );
			return;
		}

		String[] names = ModelSupport.getNames( operation.getRequestList(), new String[] { "-> Create New" } );

		String name = ( String )UISupport.prompt( "Select Request for Operation [" + operation.getName() + "] "
				+ "to open or create", "Open Request", names );
		if( name != null )
		{
			WsdlRequest request = operation.getRequestByName( name );
			if( request == null )
			{
				name = UISupport.prompt( "Specify name of new request", "Open Request", "Request "
						+ ( operation.getRequestCount() + 1 ) );
				if( name == null )
					return;

				boolean createOptional = operation.getSettings().getBoolean(
						WsdlSettings.XML_GENERATION_ALWAYS_INCLUDE_OPTIONAL_ELEMENTS );
				if( !createOptional )
					createOptional = UISupport.confirm( "Create optional elements from schema?", "Create Request" );

				request = operation.addNewRequest( name );
				String requestContent = operation.createRequest( createOptional );
				if( requestContent != null )
				{
					request.setRequestContent( requestContent );
				}
			}

			request.setEndpoint( mockResponse.getMockOperation().getMockService().getLocalEndpoint() );
			UISupport.selectAndShow( request );
		}
	}
}
