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
package com.eviware.soapui.integration.impl;

import gnu.cajo.invoke.Remote;
import gnu.cajo.utils.ItemServer;

import java.io.IOException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.settings.LoadUISettings;

public class CajoServer
{
	private String server = null;
	private String port = "1198";
	private String itemName = "soapuiIntegration";
	private static CajoServer instance;

	public static CajoServer getInstance()
	{
		if( instance == null )
		{
			return instance = new CajoServer();
		}
		return instance;
	}

	private CajoServer()
	{
	}

	public void start()
	{
		Remote.config( server, Integer
				.valueOf( SoapUI.getSettings().getString( LoadUISettings.SOAPUI_CAJO_PORT, "1198" ) ), null, 0 );
		try
		{
			ItemServer.bind( new TestCaseEditIntegrationImpl(), itemName );
			SoapUI.log( "The cajo server is running on localhost:"
					+ SoapUI.getSettings().getString( LoadUISettings.SOAPUI_CAJO_PORT, "1198" ) + "/" + itemName );
		}
		catch( IOException e )
		{
			SoapUI.log( e.getMessage() );
		}

	}

	public void restart()
	{
		Remote.shutdown();
		Remote.config( server, Integer
				.valueOf( SoapUI.getSettings().getString( LoadUISettings.SOAPUI_CAJO_PORT, "1198" ) ), null, 0 );
		try
		{
			ItemServer.bind( new TestCaseEditIntegrationImpl(), itemName );
			SoapUI.log( "The cajo server is running on localhost:"
					+ SoapUI.getSettings().getString( LoadUISettings.SOAPUI_CAJO_PORT, "1198" ) + "/" + itemName );
		}
		catch( IOException e )
		{
			SoapUI.log( e.getMessage() );
		}

	}

	public String getServer()
	{
		return server;
	}

	public String getPort()
	{
		return port;
	}

	public String getItemName()
	{
		return itemName;
	}

	public void setServer( String server )
	{
		this.server = server;
	}

	public void setPort( String port )
	{
		this.port = port;
	}

	public void setItemName( String itemName )
	{
		this.itemName = itemName;
	}
}
