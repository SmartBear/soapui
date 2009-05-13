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

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.impl.wsdl.submit.transports.http.WsdlResponse;
import com.eviware.soapui.model.iface.Attachment;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Adds a WsdlRequest to a WsdlMockService, will create required
 * WsdlMockOperation if neccessary
 * 
 * @author ole.matzura
 */

public class AddRequestToMockServiceAction extends AbstractSoapUIAction<WsdlRequest>
{
	private static final String CREATE_MOCKSUITE_OPTION = "Create new..";
	public static final String SOAPUI_ACTION_ID = "AddRequestToMockServiceAction";

	public AddRequestToMockServiceAction()
	{
		super( "Add to MockService", "Adds the current response to a MockService" );
	}

	public void perform( WsdlRequest request, Object param )
	{
		String title = getName();

		if( request != null && request.getResponse() == null )
		{
			if( !UISupport.confirm( "Request is missing response, create default mock response instead?", title ) )
			{
				return;
			}
		}

		WsdlMockService mockService = null;
		WsdlMockOperation mockOperation = ( WsdlMockOperation )param;
		if( mockOperation != null )
			mockService = mockOperation.getMockService();

		WsdlProject project = request.getOperation().getInterface().getProject();

		while( mockService == null )
		{
			if( project.getMockServiceCount() > 0 )
			{
				String[] mockServices = ModelSupport.getNames( project.getMockServiceList(),
						new String[] { CREATE_MOCKSUITE_OPTION } );

				// prompt
				String option = UISupport.prompt( "Select MockService for MockOperation", title, mockServices );
				if( option == null )
					return;

				mockService = project.getMockServiceByName( option );
			}

			// create new mocksuite?
			if( mockService == null )
			{
				String mockServiceName = UISupport.prompt( "Enter name of new MockService", title, "MockService "
						+ ( project.getMockServiceCount() + 1 ) );
				if( mockServiceName == null || mockServiceName.trim().length() == 0 )
					return;

				mockService = project.addNewMockService( mockServiceName );
			}

			mockOperation = mockService.getMockOperation( request.getOperation() );
			if( mockOperation != null )
			{
				Boolean retval = UISupport.confirmOrCancel( "MockService [" + mockService.getName()
						+ "] already has a MockOperation for [" + request.getOperation().getName()
						+ "],\r\nShould MockResponse be added to this MockOperation instead", "Add Request to MockService" );

				if( retval == null )
					return;

				if( !retval.booleanValue() )
					mockService = null;
			}
		}

		// add mockoperation
		if( mockOperation == null )
			mockOperation = mockService.addNewMockOperation( request.getOperation() );

		WsdlMockResponse mockResponse = mockOperation.addNewMockResponse( "Response "
				+ ( 1 + mockOperation.getMockResponseCount() ), false );

		// add expected response if available
		if( request != null && request.getResponse() != null )
		{
			WsdlResponse response = request.getResponse();
			mockResponse.setResponseContent( response.getContentAsString() );

			Attachment[] attachments = response.getAttachments();
			for( Attachment attachment : attachments )
			{
				mockResponse.addAttachment( attachment );
			}

			if( mockResponse.getResponseHeaders() != null && mockResponse.getResponseHeaders().size() > 0
					&& UISupport.confirm( "Add current Response HTTP Headers to MockResponse", title ) )
				mockResponse.setResponseHeaders( response.getResponseHeaders() );
		}
		else
		{
			mockResponse.setResponseContent( request.getOperation().createResponse( true ) );
		}

		if( UISupport.confirm( "Open MockResponse editor?", title ) )
		{
			SoapUI.getDesktop().showDesktopPanel( mockResponse );
		}
	}
}
