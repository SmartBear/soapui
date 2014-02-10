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

package com.eviware.soapui.settings;

/**
 * Tools/Integration-related settings constants
 *
 * @author Ole.Matzura
 */

public interface LoadUISettings
{
	public final static String LOADUI_PATH = LoadUISettings.class.getSimpleName() + "@" + "loadui_path";
	public final static String LOADUI_CAJO_SERVER = LoadUISettings.class.getSimpleName() + "@" + "cajo_server_name";
	public final static String LOADUI_CAJO_PORT = LoadUISettings.class.getSimpleName() + "@" + "cajo_port";
	public final static String LOADUI_CAJO_ITEM_NAME = LoadUISettings.class.getSimpleName() + "@" + "cajo_item_name";
	public final static String SOAPUI_CAJO_PORT = LoadUISettings.class.getSimpleName() + "@" + "cajo_soapui_port";

	public final static String START_CAJO_SERVER_AT_STARTUP = LoadUISettings.class.getSimpleName() + "@" + "start_cajo_server_at_startup";

}
