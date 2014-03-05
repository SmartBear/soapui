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
 * @author Prakash
 */
public class RemovePropertyAction extends AbstractAction
{
	private final JTable propertyTable;
	private final MutableTestPropertyHolder propertyHolder;
    public static final String DELETE_PARAM_ACTON_NAME = "Delete Param";

	public RemovePropertyAction( JTable propertyTable, MutableTestPropertyHolder propertyHolder, String description )
	{
        super(DELETE_PARAM_ACTON_NAME);
		this.propertyTable = propertyTable;
		this.propertyHolder = propertyHolder;
		putValue( Action.SMALL_ICON, UISupport.createImageIcon( "/remove_property.gif" ) );
		putValue( Action.SHORT_DESCRIPTION, description );
		setEnabled( false );
	}

	public void actionPerformed( ActionEvent e )
	{
		int row = propertyTable.getSelectedRow();
		if( row == -1 )
			return;

		UISupport.stopCellEditing( propertyTable );

		String propertyName = propertyTable.getValueAt( row, 0 ).toString();
		if( UISupport.confirm( "Remove parameter [" + propertyName + "]?", "Remove Parameter" ) )
		{
			propertyTable.clearSelection();
			propertyHolder.removeProperty( propertyName );
		}
	}
}