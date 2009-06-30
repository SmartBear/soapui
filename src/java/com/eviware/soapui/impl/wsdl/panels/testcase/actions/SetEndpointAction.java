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

package com.eviware.soapui.impl.wsdl.panels.testcase.actions;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;

import com.eviware.soapui.impl.support.AbstractHttpRequest;
import com.eviware.soapui.impl.support.http.HttpRequestTestStep;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.support.UISupport;

/**
 * Action for setting the endpoint for all requests in a testcase
 * 
 * @author Ole.Matzura
 */

public class SetEndpointAction extends AbstractAction
{
	private static final String USE_CURRENT = "- use current -";
	private final WsdlTestCase testCase;

	public SetEndpointAction( WsdlTestCase testCase )
	{
		this.testCase = testCase;
		putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/set_endpoint.gif" ) );
		putValue( Action.SHORT_DESCRIPTION, "Sets the endpoint for all requests in this testcase" );
	}

	public void actionPerformed( ActionEvent e )
	{
		Set<String> endpointSet = new HashSet<String>();
		Set<String> currentEndpointSet = new HashSet<String>();

		endpointSet.add( USE_CURRENT );

		for( int c = 0; c < testCase.getTestStepCount(); c++ )
		{
			TestStep step = testCase.getTestStepAt( c );
			if( step instanceof HttpRequestTestStep )
			{
				HttpRequestTestStep requestStep = ( HttpRequestTestStep )step;
				Operation operation = requestStep.getHttpRequest().getOperation();
				if( operation != null )
				{
					String[] endpoints = operation.getInterface().getEndpoints();
					for( int i = 0; i < endpoints.length; i++ )
					{
						endpointSet.add( endpoints[i] );
					}
				}
				currentEndpointSet.add( requestStep.getHttpRequest().getEndpoint() );
			}
		}

		String selected = ( String )UISupport.prompt( "Select endpoint to set for all requests", "Set Endpoint",
				endpointSet.toArray(), currentEndpointSet.size() == 1 ? currentEndpointSet.iterator().next() : USE_CURRENT );

		if( selected == null || selected.equals( USE_CURRENT ) )
			return;

		int cnt = 0;

		for( int c = 0; c < testCase.getTestStepCount(); c++ )
		{
			TestStep step = testCase.getTestStepAt( c );
			if( step instanceof HttpRequestTestStep )
			{
				HttpRequestTestStep requestStep = ( HttpRequestTestStep )step;
				AbstractHttpRequest testRequest = requestStep.getHttpRequest();

				if( testRequest.getEndpoint() == null || !testRequest.getEndpoint().equals( selected ) )
				{
					testRequest.setEndpoint( selected );
					cnt++ ;
				}
			}
		}

		UISupport.showInfoMessage( "Changed endpoint to [" + selected + "] for " + cnt + " requests" );
	}
}