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

package com.eviware.soapui.settings.impl;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.settings.ToolLocator;
import com.eviware.soapui.settings.ToolsSettings;
import com.eviware.soapui.support.UISupport;

/**
 * Uses the soapUI Settings to locate the specified tools
 * 
 * @author ole.matzura
 */

public class SettingsToolLocatorImpl implements ToolLocator
{
	public String getAntDir( boolean mandatory )
	{
		String antDir = SoapUI.getSettings().getString( ToolsSettings.ANT_LOCATION, null );
		if( mandatory && antDir == null )
		{
			UISupport.showErrorMessage( "ANT 1.6.5 (or later) directory must be set in global preferences" );
		}
		return antDir;
	}

	public String getJavacLocation( boolean mandatory )
	{
		String javac = SoapUI.getSettings().getString( ToolsSettings.JAVAC_LOCATION, null );
		if( mandatory && javac == null )
		{
			UISupport.showErrorMessage( "javac location must be set in global preferences" );
		}
		return javac;
	}
}
