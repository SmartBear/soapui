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

package com.eviware.soapui.ui.support;

import com.eviware.soapui.ui.desktop.DesktopListener;
import com.eviware.soapui.ui.desktop.DesktopPanel;

/**
 * Adapter for DesktopListener implementations
 * 
 * @author Ole.Matzura
 */

public class DesktopListenerAdapter implements DesktopListener
{
	public void desktopPanelSelected( DesktopPanel desktopPanel )
	{
	}

	public void desktopPanelCreated( DesktopPanel desktopPanel )
	{
	}

	public void desktopPanelClosed( DesktopPanel desktopPanel )
	{
	}
}
