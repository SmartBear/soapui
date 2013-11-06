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

package com.eviware.soapui.ui.desktop;

import javax.swing.JComponent;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.swing.ActionList;

/**
 * null-desktop used when running from command-line, etc
 * 
 * @author Ole
 */

public class NullDesktop implements SoapUIDesktop
{
	public void addDesktopListener( DesktopListener listener )
	{
	}

	public boolean closeAll()
	{
		return false;
	}

	public boolean closeDesktopPanel( DesktopPanel desktopPanel )
	{
		return false;
	}

	public boolean closeDesktopPanel( ModelItem modelItem )
	{
		return false;
	}

	public ActionList getActions()
	{
		return null;
	}

	public JComponent getDesktopComponent()
	{
		return null;
	}

	public DesktopPanel getDesktopPanel( ModelItem modelItem )
	{
		return null;
	}

	public DesktopPanel[] getDesktopPanels()
	{
		return null;
	}

	public boolean hasDesktopPanel( ModelItem modelItem )
	{
		return false;
	}

	public void init()
	{
	}

	public void maximize( DesktopPanel dp )
	{
	}

	public void minimize( DesktopPanel desktopPanel )
	{
	}

	public void release()
	{
	}

	public void removeDesktopListener( DesktopListener listener )
	{
	}

	public DesktopPanel showDesktopPanel( ModelItem modelItem )
	{
		return null;
	}

	public DesktopPanel showDesktopPanel( DesktopPanel desktopPanel )
	{
		return null;
	}

	public void transferTo( SoapUIDesktop newDesktop )
	{
	}
}
