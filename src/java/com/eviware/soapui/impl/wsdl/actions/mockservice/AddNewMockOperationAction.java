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

package com.eviware.soapui.impl.wsdl.actions.mockservice;

import java.util.ArrayList;
import java.util.List;

import com.eviware.soapui.impl.WsdlInterfaceFactory;
import com.eviware.soapui.impl.support.AbstractInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockOperation;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockResponse;
import com.eviware.soapui.impl.wsdl.mock.WsdlMockService;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;

/**
 * Adds a new WsdlMockOperation to a WsdlMockService
 * 
 * @author Ole.Matzura
 */

public class AddNewMockOperationAction extends AbstractSoapUIAction<WsdlMockService>
{
	public final static String SOAPUI_ACTION_ID = "AddNewMockOperationAction";

	public AddNewMockOperationAction()
	{
		super( "New MockOperation", "Creates a new MockOperation for this MockService" );
	}

	public void perform( WsdlMockService mockService, Object param )
	{
		List<OperationWrapper> operations = new ArrayList<OperationWrapper>();

		List<AbstractInterface<?>> interfaces = mockService.getProject().getInterfaces( WsdlInterfaceFactory.WSDL_TYPE );

		for( Interface iface : interfaces )
		{
			for( int i = 0; i < iface.getOperationCount(); i++ )
			{
				if( !mockService.hasMockOperation( iface.getOperationAt( i ) ) )
					operations.add( new OperationWrapper( ( WsdlOperation )iface.getOperationAt( i ) ) );
			}
		}

		if( operations.isEmpty() )
		{
			UISupport.showErrorMessage( "No unique operations to mock in project!" );
			return;
		}

		Object result = UISupport.prompt( "Select Operation to Mock", "New MockOperation", operations.toArray() );
		if( result != null )
		{
			WsdlMockOperation mockOperation = mockService.addNewMockOperation( ( ( OperationWrapper )result )
					.getOperation() );
			WsdlMockResponse mockResponse = mockOperation.addNewMockResponse( "Response 1", true );
			UISupport.selectAndShow( mockResponse );
		}
	}

	public class OperationWrapper
	{
		private final WsdlOperation operation;

		public OperationWrapper( WsdlOperation operation )
		{
			this.operation = operation;
		}

		public WsdlOperation getOperation()
		{
			return operation;
		}

		public String toString()
		{
			return operation.getInterface().getName() + " - " + operation.getName();
		}
	}
}
