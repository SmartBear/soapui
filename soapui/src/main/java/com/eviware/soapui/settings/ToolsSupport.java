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

import com.eviware.soapui.settings.impl.SettingsToolLocatorImpl;

/**
 * Utilities for working with Tools and their locations
 * 
 * @author Lars Höidahl
 */

public class ToolsSupport
{
	private static ToolLocator toolLocations = new SettingsToolLocatorImpl();

	public static void setToolLocator( ToolLocator locations )
	{
		toolLocations = locations;
	}

	public static ToolLocator getToolLocator()
	{
		return toolLocations;
	}
}
