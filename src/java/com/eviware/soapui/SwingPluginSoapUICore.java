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

package com.eviware.soapui;

public class SwingPluginSoapUICore extends SwingSoapUICore
{
	public SwingPluginSoapUICore()
	{
		this( System.getProperty( "user.home", "." ), SoapUICore.DEFAULT_SETTINGS_FILE );
	}

	public SwingPluginSoapUICore( String root )
	{
		this( root, SoapUICore.DEFAULT_SETTINGS_FILE );
	}

	public SwingPluginSoapUICore( String root, String settingsFile )
	{
		super( root, settingsFile );

		prepareUI();
		SoapUI.setSoapUICore( this );
	}

	@Override
	protected String importSettingsOnStartup( String fileName ) throws Exception
	{
		return fileName;
	}
}
