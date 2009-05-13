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

package com.eviware.soapui.support.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JSplitPane;

import com.eviware.soapui.support.UISupport;

/**
 * Changes the orientation of a JSplitPane
 * 
 * @author Ole.Matzura
 */

public class ChangeSplitPaneOrientationAction extends AbstractAction
{
	private final JSplitPane splitPane;

	public ChangeSplitPaneOrientationAction( JSplitPane splitPane )
	{
		super();
		this.splitPane = splitPane;

		putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/split_request_pane.gif" ) );
		putValue( Action.SHORT_DESCRIPTION, "Changes the orientation of the request pane split" );
		putValue( Action.ACCELERATOR_KEY, UISupport.getKeyStroke( "alt O" ) );
	}

	public void actionPerformed( ActionEvent e )
	{
		int orientation = splitPane.getOrientation();
		splitPane.setOrientation( orientation == JSplitPane.HORIZONTAL_SPLIT ? JSplitPane.VERTICAL_SPLIT
				: JSplitPane.HORIZONTAL_SPLIT );
		splitPane.resetToPreferredSizes();
	}
}
