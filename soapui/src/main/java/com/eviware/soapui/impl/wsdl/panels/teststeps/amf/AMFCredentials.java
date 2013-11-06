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

package com.eviware.soapui.impl.wsdl.panels.teststeps.amf;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.model.iface.SubmitContext;

import flex.messaging.io.amf.client.exceptions.ClientStatusException;
import flex.messaging.io.amf.client.exceptions.ServerStatusException;
import flex.messaging.messages.CommandMessage;
import flex.messaging.util.Base64.Encoder;

public class AMFCredentials
{

	public static final String DESTINATION = "auth";
	private String endpoint;
	private String username;
	private String password;
	private SubmitContext context;
	private boolean logedIn;

	public AMFCredentials( String endpoint, String username, String password, SubmitContext context )
	{
		this.endpoint = endpoint;
		this.username = username;
		this.password = password;
		this.context = context;
	}

	public SoapUIAMFConnection login() throws ClientStatusException, ServerStatusException
	{
		CommandMessage commandMessage = createLoginCommandMessage();

		SoapUIAMFConnection amfConnection = new SoapUIAMFConnection();
		amfConnection.connect( endpoint );
		amfConnection.call( ( SubmitContext )context, null, commandMessage );
		logedIn = true;
		return amfConnection;
	}

	public static void logout( SubmitContext context )
	{
		SoapUIAMFConnection connection = ( SoapUIAMFConnection )context.getProperty( AMFSubmit.AMF_CONNECTION );
		CommandMessage commandMessage = createLogoutCommandMessage();
		try
		{
			connection.call( ( SubmitContext )context, null, commandMessage );
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

	public void logout()
	{
		SoapUIAMFConnection connection = ( SoapUIAMFConnection )context.getProperty( AMFSubmit.AMF_CONNECTION );
		CommandMessage commandMessage = createLogoutCommandMessage();
		try
		{
			connection.call( ( SubmitContext )context, null, commandMessage );
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

	private CommandMessage createLoginCommandMessage()
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

	private static CommandMessage createLogoutCommandMessage()
	{
		CommandMessage commandMessage = new CommandMessage();
		commandMessage.setOperation( CommandMessage.LOGOUT_OPERATION );
		commandMessage.setDestination( DESTINATION );
		return commandMessage;
	}

	public String getEndpoint()
	{
		return endpoint;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

	public SubmitContext getContext()
	{
		return context;
	}

	public void setLogedIn( boolean logedIn )
	{
		this.logedIn = logedIn;
	}

	public boolean isLoggedIn()
	{
		return logedIn;
	}

}
