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

package com.eviware.soapui.support.components;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;

import com.eviware.soapui.support.UISupport;

public class ShowPopupAction extends AbstractAction
{
	private final JComponent popupContainer;
	private final JComponent container;

	public ShowPopupAction( JComponent popupContainer, JComponent container )
	{
		this.popupContainer = popupContainer;
		this.container = container;

		putValue( SMALL_ICON, UISupport.createImageIcon( "/get_data_button.gif" ) );
	}

	public void actionPerformed( ActionEvent e )
	{
		popupContainer.getComponentPopupMenu().show( container, container.getWidth() / 2, container.getHeight() / 2 );
	}
}