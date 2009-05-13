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

package com.eviware.soapui.impl.wsdl.actions.operation;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Creates a WsdlMockOperation for the specified WsdlOperation
 * 
 * @author ole.matzura
 */

public class AddOperationToMockServiceAction extends AbstractSoapUIAction<WsdlOperation>
{
	private static final String CREATE_MOCKSUITE_OPTION = "Create new..";
	public static final String SOAPUI_ACTION_ID = "AddOperationToMockServiceAction";

	public AddOperationToMockServiceAction()
	{
		super( "Add to MockService", "Add this operation to a MockService" );
	}

	public void perform( WsdlOperation operation, Object param )
	{
		String title = getName();

		WsdlMockService mockService = null;
		WsdlProject project = operation.getInterface().getProject();

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

			if( mockService.hasMockOperation( operation ) )
			{
				UISupport.showErrorMessage( "MockService [" + mockService.getName() + "] already has a MockOperation for ["
						+ operation.getName() + "], please select another MockService" );
				mockService = null;
			}
		}

		// add mockoperation
		addOperationToMockService( operation, mockService );
	}

	public boolean addOperationToMockService( WsdlOperation operation, WsdlMockService mockService )
	{
		if( mockService.hasMockOperation( operation ) )
		{
			UISupport.showErrorMessage( "MockService [" + mockService.getName() + "] already has a MockOperation for ["
					+ operation.getName() + "]" );
			return false;
		}

		WsdlMockOperation mockOperation = mockService.addNewMockOperation( operation );
		WsdlMockResponse mockResponse = mockOperation.addNewMockResponse( "Response 1", false );

		if( operation.isBidirectional() )
			mockResponse.setResponseContent( operation.createResponse( true ) );

		if( UISupport.confirm( "Open MockResponse editor?", getName() ) )
		{
			SoapUI.getDesktop().showDesktopPanel( mockResponse );
		}

		return true;
	}
}
