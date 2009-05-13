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

import javax.swing.JComponent;

import com.eviware.soapui.model.ModelItem;
import com.eviware.soapui.support.action.swing.ActionList;

/**
 * Behaviour for a soapUI Desktop implementation
 * 
 * @author ole.matzura
 */

public interface SoapUIDesktop
{
	public boolean closeDesktopPanel( DesktopPanel desktopPanel );

	public boolean hasDesktopPanel( ModelItem modelItem );

	public void addDesktopListener( DesktopListener listener );

	public void removeDesktopListener( DesktopListener listener );

	public DesktopPanel showDesktopPanel( ModelItem modelItem );

	public boolean closeDesktopPanel( ModelItem modelItem );

	public ActionList getActions();

	public DesktopPanel[] getDesktopPanels();

	public DesktopPanel getDesktopPanel( ModelItem modelItem );

	public DesktopPanel showDesktopPanel( DesktopPanel desktopPanel );

	public JComponent getDesktopComponent();

	public void transferTo( SoapUIDesktop newDesktop );

	public boolean closeAll();

	public void release();

	public void init();

	public void minimize( DesktopPanel desktopPanel );

	public void maximize( DesktopPanel dp );
}