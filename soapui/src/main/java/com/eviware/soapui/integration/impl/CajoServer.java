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

package com.eviware.soapui.integration.impl;

import gnu.cajo.invoke.Remote;
import gnu.cajo.utils.ItemServer;

import java.io.IOException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.integration.loadui.IntegrationUtils;
import com.eviware.soapui.settings.LoadUISettings;

public class CajoServer
{

	public static final String DEFAULT_SOAPUI_CAJO_PORT = "1198";

	private String server = null;
	private String port = DEFAULT_SOAPUI_CAJO_PORT;
	private String itemName = "soapuiIntegration";

	public static CajoServer getInstance()
	{
		return SingletonHolder.instance;
	}

	static class SingletonHolder
	{
		static CajoServer instance = new CajoServer();
	}

	private CajoServer()
	{
	}

	public void start()
	{
		String cajoPort = IntegrationUtils.getIntegrationPort( "SoapUI", LoadUISettings.SOAPUI_CAJO_PORT,
				DEFAULT_SOAPUI_CAJO_PORT );
		Remote.config( server, Integer.valueOf( cajoPort ), null, 0 );
		try
		{
			ItemServer.bind( new TestCaseEditIntegrationImpl(), itemName );
			SoapUI.log( "The cajo server is running on localhost:" + cajoPort + "/" + itemName );
		}
		catch( IOException e )
		{
			SoapUI.log( e.getMessage() );
		}

		CajoClient.getInstance().testConnection();
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
