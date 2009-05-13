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

package com.eviware.soapui.ui.desktop.standalone;

import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.ui.desktop.DesktopFactory;
import com.eviware.soapui.ui.desktop.SoapUIDesktop;

/**
 * Creates a StandaloneDesktop
 * 
 * @author ole.matzura
 */

public class StandaloneDesktopFactory implements DesktopFactory
{
	public SoapUIDesktop createDesktop( Workspace workspace )
	{
		return new StandaloneDesktop( workspace );
	}
}
