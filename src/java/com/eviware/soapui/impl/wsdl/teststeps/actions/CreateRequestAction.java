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

package com.eviware.soapui.impl.wsdl.teststeps.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlRequest;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStepResult;
import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestStepResult;
import com.eviware.soapui.support.UISupport;

/**
 * Creates a request from the specified TestStepResult
 * 
 * @author Ole.Matzura
 */

public class CreateRequestAction extends AbstractAction
{
	private final WsdlTestRequestStepResult result;

	public CreateRequestAction( WsdlTestStepResult result )
	{
		this.result = ( WsdlTestRequestStepResult )result;

		putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/create_request_from_result.gif" ) );
		putValue( Action.SHORT_DESCRIPTION, "Creates a new request from this result" );
	}

	public void actionPerformed( ActionEvent e )
	{
		WsdlTestRequestStep step = ( WsdlTestRequestStep )result.getTestStep();
		String name = UISupport.prompt( "Specify name of request", "Create Request", "Result from " + step.getName() );

		if( name != null )
		{
			WsdlOperation operation = ( WsdlOperation )step.getTestRequest().getOperation();
			WsdlRequest request = operation.addNewRequest( name );
			request.setRequestContent( result.getRequestContent() );
			request.setDomain( result.getDomain() );
			request.setEncoding( result.getEncoding() );
			request.setEndpoint( result.getEndpoint() );
			request.setPassword( result.getPassword() );
			request.setUsername( result.getUsername() );

			UISupport.showDesktopPanel( request );
		}
	}
}
