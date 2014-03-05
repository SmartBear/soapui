/*
 *  SoapUI, copyright (C) 2004-2013 smartbear.com
 *
 *  SoapUI is free software; you can redistribute it and/or modify it under the
 *  terms of version 2.1 of the GNU Lesser General Public License as published by
 *  the Free Software Foundation.
 *
 *  SoapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.wsdl.panels.teststeps.support;

import com.eviware.soapui.impl.wsdl.MutableTestPropertyHolder;
import com.eviware.soapui.support.UISupport;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTable;
import java.awt.event.ActionEvent;

/**
 * Action that moves a property down in the display order of a table.
 */
public class MovePropertyDownAction extends AbstractAction
{
    public static final String MOVE_PROPERTY_DOWN_ACTION_NAME = "Move Property Down";
    private final JTable propertyTable;
	private final MutableTestPropertyHolder propertyHolder;

	public MovePropertyDownAction( JTable propertyTable, MutableTestPropertyHolder propertyHolder, String description )
	{
        super(MOVE_PROPERTY_DOWN_ACTION_NAME);
		this.propertyTable = propertyTable;
		this.propertyHolder = propertyHolder;
		putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/down_arrow.gif" ) );
		putValue( Action.SHORT_DESCRIPTION, description );
		setEnabled( false );
	}

	public void actionPerformed( ActionEvent e )
	{
		int ix = propertyTable.getSelectedRow();
		if( ix != -1 )
		{
			String propName = ( String )propertyTable.getValueAt( ix, 0 );
			( ( PropertyHolderTableModel )propertyTable.getModel() ).moveProperty( propName, ix, ix + 1 );
			propertyTable.setRowSelectionInterval( ix + 1, ix + 1 );
		}
	}
}
