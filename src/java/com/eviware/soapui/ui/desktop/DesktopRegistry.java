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

package com.eviware.soapui.ui.desktop;

import java.util.HashMap;
import java.util.Map;

import com.eviware.soapui.model.workspace.Workspace;

/**
 * Registry of available desktops
 * 
 * @author ole.matzura
 */

public class DesktopRegistry
{
	private static DesktopRegistry instance;
	private Map<String, DesktopFactory> factories = new HashMap<String, DesktopFactory>();

	public static DesktopRegistry getInstance()
	{
		if( instance == null )
			instance = new DesktopRegistry();

		return instance;
	}

	public void addDesktop( String name, DesktopFactory factory )
	{
		factories.put( name, factory );
	}

	public String[] getNames()
	{
		return factories.keySet().toArray( new String[factories.size()] );
	}

	public SoapUIDesktop createDesktop( String desktopType, Workspace workspace )
	{
		if( factories.containsKey( desktopType ) )
			return factories.get( desktopType ).createDesktop( workspace );

		return null;
	}
}
