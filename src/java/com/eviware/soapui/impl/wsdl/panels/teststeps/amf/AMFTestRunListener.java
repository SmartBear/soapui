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
package com.eviware.soapui.impl.wsdl.panels.teststeps.amf;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.impl.wsdl.testcase.WsdlTestCase;
import com.eviware.soapui.model.iface.SubmitContext;
import com.eviware.soapui.model.testsuite.TestCaseRunContext;
import com.eviware.soapui.model.testsuite.TestCaseRunner;
import com.eviware.soapui.model.testsuite.TestRunListener;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.model.testsuite.TestStepResult;

import flex.messaging.io.amf.client.exceptions.ClientStatusException;
import flex.messaging.io.amf.client.exceptions.ServerStatusException;
import flex.messaging.messages.CommandMessage;
import flex.messaging.util.Base64.Encoder;

public class AMFTestRunListener implements TestRunListener
{

	private static final String DESTINATION = "auth";

	public void afterRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
		if( runContext.getProperty( AMFSubmit.AMF_CONNECTION ) != null
				&& runContext.getProperty( AMFSubmit.AMF_CONNECTION ) instanceof SoapUIAMFConnection )
		{
			SoapUIAMFConnection connection = ( SoapUIAMFConnection )runContext.getProperty( AMFSubmit.AMF_CONNECTION );
			CommandMessage commandMessage = createLogoutCommandMessage();
			try
			{
				connection.call( ( SubmitContext )runContext, null, commandMessage );
			}
			catch( ClientStatusException e )
			{
				SoapUI.logError( e );
			}
			catch( ServerStatusException e )
			{
				SoapUI.logError( e );
			}
			finally
			{
				connection.close();
			}
		}
	}

	public void beforeRun( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
		if( testRunner.getTestCase() instanceof WsdlTestCase )
		{
			try
			{
				WsdlTestCase wsdlTestCase = ( WsdlTestCase )testRunner.getTestCase();
				if( wsdlTestCase.getConfig().getAmfAuthorisation() )
				{
					String endpoint = runContext.expand( wsdlTestCase.getConfig().getAmfEndpoint() );
					String username = runContext.expand( wsdlTestCase.getConfig().getAmfLogin() );
					String password = runContext.expand( wsdlTestCase.getConfig().getAmfPassword() );

					CommandMessage commandMessage = createLoginCommandMessage( username, password );

					SoapUIAMFConnection amfConnection = new SoapUIAMFConnection();
					amfConnection.connect( endpoint );
					amfConnection.call( ( SubmitContext )runContext, null, commandMessage );

					runContext.setProperty( AMFSubmit.AMF_CONNECTION, amfConnection );
				}
			}
			catch( ClientStatusException e )
			{
				SoapUI.logError( e );
			}
			catch( ServerStatusException e )
			{
				SoapUI.logError( e );
			}
		}
	}

	private CommandMessage createLoginCommandMessage( String username, String password )
	{
		CommandMessage commandMessage = new CommandMessage();
		commandMessage.setOperation( CommandMessage.LOGIN_OPERATION );

		String credString = username + ":" + password;
		Encoder encoder = new Encoder( credString.length() );
		encoder.encode( credString.getBytes() );

		commandMessage.setBody( encoder.drain() );
		commandMessage.setDestination( DESTINATION );
		return commandMessage;
	}

	private CommandMessage createLogoutCommandMessage()
	{
		CommandMessage commandMessage = new CommandMessage();
		commandMessage.setOperation( CommandMessage.LOGOUT_OPERATION );
		commandMessage.setDestination( DESTINATION );
		return commandMessage;
	}

	public void beforeStep( TestCaseRunner testRunner, TestCaseRunContext runContext )
	{
	}

	public void beforeStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStep testStep )
	{
	}

	public void afterStep( TestCaseRunner testRunner, TestCaseRunContext runContext, TestStepResult result )
	{
	}
}
