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

import java.io.IOException;
import java.rmi.ConnectException;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.integration.loadui.IntegrationUtils;
import com.eviware.soapui.settings.LoadUISettings;

public class CajoClient
{

	public static final String DEFAULT_LOADUI_CAJO_PORT = "1199";

	private String server = "localhost";
	private String port = DEFAULT_LOADUI_CAJO_PORT;
	private String itemName = "loaduiIntegration";

	private static CajoClient instance;

	public static CajoClient getInstance()
	{
		if( instance == null )
		{
			instance = new CajoClient();
			instance.port = IntegrationUtils.getIntegrationPort( "loadUI", LoadUISettings.LOADUI_CAJO_PORT,
					DEFAULT_LOADUI_CAJO_PORT );
			return instance;
		}
		else
			return instance;
	}

	private CajoClient()
	{
	}

	public Object getItem() throws Exception
	{
		return gnu.cajo.invoke.Remote.getItem( "//" + server + ":" + port + "/" + itemName );
	}

	public Object invoke( String method, Object object ) throws Exception
	{
		try
		{
			return gnu.cajo.invoke.Remote.invoke( getItem(), method, object );
		}
		catch( ConnectException e )
		{
			SoapUI.log.info( "Could not connect to SoapUI cajo server on " + getConnectionString() );
			return null;
		}
		catch( IOException e )
		{
			// case of loadUI project opening failure
			throw e;
		}
		catch( Exception e )
		{
			SoapUI.log.info( "Connected SoapUI cajo server, but with exception: " );
			e.printStackTrace();
			return null;
		}
	}

	public boolean testConnection()
	{
		try
		{
			gnu.cajo.invoke.Remote.invoke( getItem(), "test", null );
			setLoadUIPath();
			return true;
		}
		catch( Exception e )
		{
			return false;
		}
	}

	/**
	 * If loadUI bat folder is not specified in SoapUI and there is an running
	 * instance of loadUI, takes the path of that instance and sets it to SoapUI.
	 */
	public void setLoadUIPath()
	{
		String loadUIPath = SoapUI.getSettings().getString( LoadUISettings.LOADUI_PATH, "" );
		if( loadUIPath == null || loadUIPath.trim().length() == 0 )
		{
			try
			{
				loadUIPath = ( String )invoke( "getLoadUIPath", null );
				if( loadUIPath != null )
				{
					SoapUI.getSettings().setString( LoadUISettings.LOADUI_PATH, loadUIPath );
				}
			}
			catch( Exception e )
			{
				// do nothing
			}
		}
	}

	public String getConnectionString()
	{
		return "//" + server + ":" + port + "/" + itemName;
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
